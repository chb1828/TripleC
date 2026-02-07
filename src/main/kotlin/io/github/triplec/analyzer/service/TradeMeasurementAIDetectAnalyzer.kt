package io.github.triplec.analyzer.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.triplec.analyzer.domain.AnalyzerResult
import io.github.triplec.analyzer.domain.SpikeDirection
import io.github.triplec.common.domain.measurement.TradeMeasurement
import io.github.triplec.common.service.RedisService
import io.github.triplec.common.type.OrderSideType
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Trade(체결) 기반 급등/급락 감지 Analyzer
 * 
 * ## 분석 방식
 * - 최근 50개 체결 내역을 분석하여 **고래 거래** 감지
 * - 평균 거래량 대비 대량 체결 감지 (Whale Activity)
 * - 매수/매도 체결량 비대칭성 분석
 * - 10분 전 기준점과 비교하여 거래 패턴 변화 감지
 * 
 * ## Redis 사용
 * - 키: `trade:baseline:{코인코드}`
 * - TTL: 10분
 * - 저장 내용: TradeBaseline (통계 정보)
 *   - avgVolume: 평균 체결량
 *   - maxVolume: 최대 체결량
 *   - buyVolume: 총 매수 체결량
 *   - sellVolume: 총 매도 체결량
 * 
 * ## 고래 거래(Whale Activity)의 의미
 * - **고래**: 큰 자본으로 시장에 영향을 미치는 대형 투자자
 * - **고래 매수**: 대량 매수 → 가격 상승 압력
 * - **고래 매도**: 대량 매도 → 가격 하락 압력
 * 
 * ## Trigger 조건 (4가지)
 * 
 * ### 1) 대량 체결 감지
 * - maxVolume > avgVolume * 5: 평균의 5배 이상 체결 (고래 거래)
 * 
 * ### 2) 거래량 급증
 * - volumeIncrease > 100%: 10분 전 대비 거래량 2배 이상 증가
 * 
 * ### 3) 고래 매수 감지 (2가지 조건 중 하나)
 * - currentBuyVolume > currentSellVolume * 2: 현재 매수가 매도의 2배
 * - (currentBuyVolume - baseline.buyVolume) > baseline.buyVolume: 10분 전 대비 매수량 2배 증가
 * 
 * ### 4) 고래 매도 감지 (2가지 조건 중 하나)
 * - currentSellVolume > currentBuyVolume * 2: 현재 매도가 매수의 2배
 * - (currentSellVolume - baseline.sellVolume) > baseline.sellVolume: 10분 전 대비 매도량 2배 증가
 * 
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 8. 24.
 */

private val log = KotlinLogging.logger {}

@Component
class TradeMeasurementAIDetectAnalyzer(
    private val localAIService: LocalAIService,
    private val redisService: RedisService
) : MeasurementDetectAnalyzer<TradeMeasurement> {

    override fun detect(list: Collection<TradeMeasurement>): Collection<AnalyzerResult> {
        if (list.isEmpty()) return emptyList()

        val results = mutableListOf<AnalyzerResult>()
        val grouped = list.groupBy { it.code }

        grouped.forEach { (code, measurements) ->
            // 최근 50개 체결만 분석 (성능 최적화)
            val sorted = measurements.sortedByDescending { it.time }.take(50)
            if (sorted.isEmpty()) return@forEach

            // === 체결 통계 계산 ===
            val totalVolume = sorted.sumOf { it.tradeVolume }
            val avgVolume = totalVolume / sorted.size
            val maxVolume = sorted.maxOf { it.tradeVolume }

            // === Baseline 데이터 구조 (Redis 저장용) ===
            // 체결 raw 데이터 대신 통계 정보만 저장하여 메모리 절약
            data class TradeBaseline(
                val avgVolume: Double,
                val maxVolume: Double,
                val buyVolume: Double,
                val sellVolume: Double
            )

            // 현재 매수/매도 체결량 계산
            val currentBuyVolume = sorted.filter { it.askBid == OrderSideType.BID }.sumOf { it.tradeVolume }
            val currentSellVolume = sorted.filter { it.askBid == OrderSideType.ASK }.sumOf { it.tradeVolume }
            val currentBaseline = TradeBaseline(avgVolume, maxVolume, currentBuyVolume, currentSellVolume)

            // Redis에서 10분 전 기준점 조회
            val redisKey = "trade:baseline:$code"
            val baseline = redisService.getObject(redisKey, TradeBaseline::class.java)

            if (baseline == null) {
                // 기준점이 없으면 현재 통계를 저장하고 종료
                redisService.setObject(redisKey, currentBaseline, Duration.ofMinutes(10))
                return@forEach
            }

            // === 거래량 변화율 계산 (Zero Division 방어) ===
            val volumeIncrease = if (baseline.avgVolume > 0) {
                (avgVolume - baseline.avgVolume) / baseline.avgVolume * 100
            } else {
                0.0  // baseline이 0이면 변화 없음으로 간주
            }

            // === 고래 매수/매도 조건 ===
            
            // 고래 매수: 매수 체결량이 압도적이거나, 10분 전 대비 2배 증가
            val whaleBuy = currentBuyVolume > currentSellVolume * 2 ||
                    (currentBuyVolume - baseline.buyVolume) > baseline.buyVolume

            // 고래 매도: 매도 체결량이 압도적이거나, 10분 전 대비 2배 증가
            val whaleSell = currentSellVolume > currentBuyVolume * 2 ||
                    (currentSellVolume - baseline.sellVolume) > baseline.sellVolume

            // === Trigger 조건 (4가지) ===
            val trigger =
                maxVolume > avgVolume * 5 ||    // 1) 평균의 5배 이상 대량 체결 (고래 거래)
                volumeIncrease > 100.0 ||       // 2) 거래량 2배 증가
                whaleBuy ||                     // 3) 고래 매수 감지
                whaleSell                       // 4) 고래 매도 감지

            if (trigger) {
                val prompt = """
                    Determine whale activity (spike/drop/none).
                    
                    Coin: $code
                    Volume Increase: ${"%.2f".format(volumeIncrease)}%
                    Whale Buy: $whaleBuy
                    Whale Sell: $whaleSell
                    Max Volume Ratio: ${"%.1f".format(if (avgVolume > 0) maxVolume / avgVolume else 0.0)}x (vs Avg)
                """.trimIndent()

                val direction = localAIService.askDirection(prompt)

                if (direction != SpikeDirection.UNCHANGED) {
                    results.add(AnalyzerResult(code, direction))
                    val emoji = if (direction == SpikeDirection.UP) "🚀" else "📉"
                    val whaleType = when {
                        whaleBuy -> "고래 매수"
                        whaleSell -> "고래 매도"
                        else -> "대량 체결"
                    }
                    log.info { "$emoji AI Whale Trade Detected: $code ($direction) - $whaleType" }
                }
            }
        }

        return results
    }
}