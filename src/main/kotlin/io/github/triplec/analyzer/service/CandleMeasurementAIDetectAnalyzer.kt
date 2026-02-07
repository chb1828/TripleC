package io.github.triplec.analyzer.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.triplec.analyzer.domain.AnalyzerResult
import io.github.triplec.analyzer.domain.SpikeDirection
import io.github.triplec.common.domain.measurement.CandleMeasurement
import io.github.triplec.common.service.RedisService
import org.springframework.stereotype.Component
import java.time.Duration
import kotlin.math.abs

/**
 * Candle(캔들스틱) 기반 급등/급락 감지 Analyzer
 * 
 * ## 분석 방식
 * - OHLCV (Open, High, Low, Close, Volume) 데이터를 활용한 **기술적 분석**
 * - Wick(꼬리) 분석을 통해 매수/매도 압력 판단
 * - 10분 전 기준점과 비교하여 가격 및 거래량 변화 측정
 * 
 * ## Redis 사용
 * - 키: `candle:baseline:{코인코드}`
 * - TTL: 10분
 * - 저장 내용: CandleMeasurement 객체 전체
 * 
 * ## Wick(꼬리) 분석의 의미
 * - **Upper Wick (위꼬리)**: 고점에서 마감가까지의 거리
 *   → 큰 위꼬리 = 매도 압력 (위로 찔렀다가 하락)
 * - **Lower Wick (아래꼬리)**: 저점에서 시가/종가까지의 거리
 *   → 큰 아래꼬리 = 매수 압력 (아래로 찔렀다가 상승)
 * 
 * ## Trigger 조건 (5가지)
 * 1. **가격 급변**: 10분 전 대비 1% 이상 변동
 * 2. **거래량 급증**: 10분 전 대비 50% 이상 증가
 * 3. **거래량 급감**: 10분 전 대비 30% 이상 감소 (매물 대기 상태)
 * 4. **강한 위꼬리**: 전체 범위의 30% 이상 (매도 압력)
 * 5. **강한 아래꼬리**: 전체 범위의 30% 이상 (매수 압력)
 * 
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 8. 24.
 */

private val log = KotlinLogging.logger {}

@Component
class CandleMeasurementAIDetectAnalyzer(
    private val localAIService: LocalAIService,
    private val redisService: RedisService
) : MeasurementDetectAnalyzer<CandleMeasurement> {

    override fun detect(list: Collection<CandleMeasurement>): Collection<AnalyzerResult> {
        if (list.isEmpty()) return emptyList()

        val results = mutableListOf<AnalyzerResult>()
        val grouped = list.groupBy { it.code }

        grouped.forEach { (code, measurements) ->
            val sorted = measurements.sortedBy { it.time }
            val latest = sorted.last()

            // Redis에서 10분 전 기준점 조회
            val redisKey = "candle:baseline:$code"
            val baseline = redisService.getObject(redisKey, CandleMeasurement::class.java)

            if (baseline == null) {
                // 기준점이 없으면 현재 값을 저장하고 종료
                redisService.setObject(redisKey, latest, Duration.ofMinutes(10))
                return@forEach
            }

            // === 변동률 계산 ===
            val volumeChange = (latest.volume - baseline.volume) / baseline.volume * 100
            val priceChange = (latest.closePrice - baseline.closePrice) / baseline.closePrice * 100

            // === 캔들 타입 판단 ===
            val candleType = when {
                latest.closePrice > latest.openPrice -> "Bullish (상승)"
                latest.closePrice < latest.openPrice -> "Bearish (하락)"
                else -> "Doji (균형)"
            }

            // === Wick(꼬리) 분석 ===
            // 위꼬리: 고점 - max(시가, 종가)
            val upperWick = latest.highPrice - maxOf(latest.openPrice, latest.closePrice)
            // 아래꼬리: min(시가, 종가) - 저점
            val lowerWick = minOf(latest.openPrice, latest.closePrice) - latest.lowPrice
            
            // 전체 범위 (고점 - 저점)
            val totalRange = latest.highPrice - latest.lowPrice
            
            // 꼬리가 전체 범위의 30% 이상이면 "강한" 압력으로 판단
            val strongUpperWick = totalRange > 0 && upperWick > totalRange * 0.3  // 매도 압력
            val strongLowerWick = totalRange > 0 && lowerWick > totalRange * 0.3  // 매수 압력

            // === Trigger 조건 ===
            val trigger =
                abs(priceChange) > 1.0 ||        // 가격 1% 이상 변동
                volumeChange > 50.0 ||           // 거래량 50% 이상 급증
                volumeChange < -30.0 ||          // 거래량 30% 이상 급감
                strongUpperWick ||               // 강한 매도 압력
                strongLowerWick                  // 강한 매수 압력

            if (trigger) {
                val prompt = """
                    Determine trend (spike/drop/none).
                    
                    Coin: $code
                    Price Change: ${"%.2f".format(priceChange)}%
                    Volume Change: ${"%.2f".format(volumeChange)}%
                    Candle Type: $candleType
                    Wick Pressure: ${if (strongUpperWick) "Sell (Upper)" else if (strongLowerWick) "Buy (Lower)" else "None"}
                """.trimIndent()

                val direction = localAIService.askDirection(prompt)

                if (direction != SpikeDirection.UNCHANGED) {
                    val emoji = if (direction == SpikeDirection.UP) "🚀" else "📉"
                    log.info { "$emoji AI Candle Pattern Detected: $code ($direction) - Type: $candleType" }
                    results.add(AnalyzerResult(code, direction))
                }
            }
        }

        return results
    }
}
