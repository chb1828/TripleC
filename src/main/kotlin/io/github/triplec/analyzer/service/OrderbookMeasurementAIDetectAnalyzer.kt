package io.github.triplec.analyzer.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.triplec.analyzer.domain.AnalyzerResult
import io.github.triplec.analyzer.domain.SpikeDirection
import io.github.triplec.common.domain.measurement.OrderbookMeasurement
import io.github.triplec.common.service.RedisService
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Orderbook(호가창) 기반 급등/급락 감지 Analyzer
 * 
 * ## 분석 방식
 * - 매수/매도 호가 잔량의 **불균형**을 측정하여 시장 압력 판단
 * - 10분 전 기준점과 비교하여 호가 구조의 변화 감지
 * - Ask(매도) vs Bid(매수) 비율을 다각도로 분석
 * 
 * ## Redis 사용
 * - 키: `orderbook:baseline:{코인코드}`
 * - TTL: 10분
 * - 저장 내용: OrderbookMeasurement 객체 전체
 * 
 * ## Ratio 의미
 * - **askRatio**: 전체 잔량 중 매도 호가 비율 (높을수록 매도 우위 → 하락 압력)
 * - **bidRatio**: 전체 잔량 중 매수 호가 비율 (높을수록 매수 우위 → 상승 압력)
 * - **imbalanceRatio**: 플랫폼에서 제공하는 불균형 지표
 * 
 * ## Trigger 조건 (7가지) - 다층 감지 시스템
 * 
 * ### 1) 절대적 불균형
 * - askRatio > 65%: 매도 우위 (하락 압력)
 * - bidRatio > 65%: 매수 우위 (상승 압력)
 * 
 * ### 2) 상대적 변화 (10분 전 대비)
 * - ask/bid 비율이 12% 이상 변화: 호가 구조 급변
 * 
 * ### 3) 플랫폼 지표
 * - imbalanceRatio > 25%: 심한 불균형 상태
 * 
 * ### 4) 방향성 증가 (10분 전 대비 20% 이상 증가)
 * - askIncrease: 매도 물량 급증 → 하락 시그널
 * - bidIncrease: 매수 물량 급증 → 상승 시그널
 * 
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 8. 24.
 */

private val log = KotlinLogging.logger {}

@Component
class OrderbookMeasurementAIDetectAnalyzer(
    private val localAIService: LocalAIService,
    private val redisService: RedisService
) : MeasurementDetectAnalyzer<OrderbookMeasurement> {

    override fun detect(list: Collection<OrderbookMeasurement>): Collection<AnalyzerResult> {
        if (list.isEmpty()) return emptyList()

        val results = mutableListOf<AnalyzerResult>()
        val grouped = list.groupBy { it.code }

        grouped.forEach { (code, measurements) ->
            val latest = measurements.maxByOrNull { it.time } ?: return@forEach

            // Redis에서 10분 전 기준점 조회
            val redisKey = "orderbook:baseline:$code"
            val baseline = redisService.getObject(redisKey, OrderbookMeasurement::class.java)

            if (baseline == null) {
                // 기준점이 없으면 현재 값을 저장하고 종료
                redisService.setObject(redisKey, latest, Duration.ofMinutes(10))
                return@forEach
            }

            // === 현재 호가 비율 계산 ===
            val totalSize = latest.totalAskSize + latest.totalBidSize
            if (totalSize == 0.0) return@forEach  // 호가 데이터 없음

            val askRatio = latest.totalAskSize / totalSize  // 매도 비율
            val bidRatio = latest.totalBidSize / totalSize  // 매수 비율

            // === 10분 전 호가 비율 계산 ===
            val baselineTotalSize = baseline.totalAskSize + baseline.totalBidSize
            val baselineAskRatio = if (baselineTotalSize > 0) {
                baseline.totalAskSize / baselineTotalSize
            } else {
                0.5  // 기본값: 50:50
            }
            val baselineBidRatio = 1 - baselineAskRatio

            // === 방향성 증가 여부 (20% 이상 증가) ===
            // askIncrease: 매도 물량 급증 → 하락 시그널
            // bidIncrease: 매수 물량 급증 → 상승 시그널
            val askIncrease = latest.totalAskSize > baseline.totalAskSize * 1.2
            val bidIncrease = latest.totalBidSize > baseline.totalBidSize * 1.2

            // === Trigger 조건 (7가지 - 다층 감지 시스템) ===
            val trigger =
                askRatio > 0.65 ||              // 1) 매도 우위 65% (하락 압력)
                bidRatio > 0.65 ||              // 2) 매수 우위 65% (상승 압력)
                kotlin.math.abs(askRatio - baselineAskRatio) > 0.12 ||  // 3) ask 비율 12% 이상 변화
                kotlin.math.abs(bidRatio - baselineBidRatio) > 0.12 ||  // 4) bid 비율 12% 이상 변화
                kotlin.math.abs(latest.imbalanceRatio) > 0.25 ||        // 5) 불균형 25% 이상
                askIncrease ||                  // 6) 매도 물량 20% 이상 급증
                bidIncrease                     // 7) 매수 물량 20% 이상 급증

            if (trigger) {
                val prompt = """
                    Determine market pressure (spike/drop/none).
                    
                    Coin: $code
                    Bid Ratio: ${"%.2f".format(bidRatio)} (vs 10m ago: ${"%.2f".format(baselineBidRatio)})
                    Ask Ratio: ${"%.2f".format(askRatio)}
                    Imbalance: ${"%.2f".format(latest.imbalanceRatio)}
                    Surge: ${if (bidIncrease) "Bid" else if (askIncrease) "Ask" else "None"}
                """.trimIndent()

                val direction = localAIService.askDirection(prompt)

                if (direction != SpikeDirection.UNCHANGED) {
                    results.add(AnalyzerResult(code, direction))
                    val emoji = if (direction == SpikeDirection.UP) "🚀" else "📉"
                    val pressureType = if (askRatio > bidRatio) "매도 압력" else "매수 압력"
                    log.info { "$emoji AI Orderbook Pressure Detected: $code ($direction) - $pressureType" }
                }
            }
        }

        return results
    }
}
