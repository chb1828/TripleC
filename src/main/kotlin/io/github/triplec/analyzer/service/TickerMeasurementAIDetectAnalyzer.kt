package io.github.triplec.analyzer.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.triplec.analyzer.domain.AnalyzerResult
import io.github.triplec.analyzer.domain.SpikeDirection
import io.github.triplec.common.domain.measurement.TickerMeasurement
import io.github.triplec.common.service.RedisService
import org.springframework.stereotype.Component
import java.time.Duration
import kotlin.math.abs

/**
 * Ticker(현재가) 기반 급등/급락 감지 Analyzer
 * 
 * ## 분석 방식
 * - Redis에 10분 전 기준점(baseline)을 저장하고, 현재 값과 비교하여 급등/급락 감지
 * - 가격 변동률과 거래량 변동률을 복합적으로 분석
 * - 일반적인 변동뿐만 아니라 **강한 하락 신호**도 명시적으로 감지
 * 
 * ## Redis 사용
 * - 키: `ticker:baseline:{코인코드}`
 * - TTL: 10분
 * - 저장 내용: TickerMeasurement 객체 전체
 * 
 * ## Trigger 조건 (4가지)
 * 1. **일반 변동**: 가격 0.5% 이상 변동 또는 거래량 10% 이상 변동
 * 2. **강한 하락**: 가격 -1% 이하 급락
 * 3. **거래량 붕괴**: 거래량 -30% 이하 급감 (매물 대기 상태)
 * 
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 8. 24.
 */

private val log = KotlinLogging.logger {}

@Component
class TickerMeasurementAIDetectAnalyzer(
    private val localAIService: LocalAIService,
    private val redisService: RedisService
) : MeasurementDetectAnalyzer<TickerMeasurement> {

    override fun detect(list: Collection<TickerMeasurement>): Collection<AnalyzerResult> {
        if (list.isEmpty()) return emptyList()

        val results = mutableListOf<AnalyzerResult>()
        val grouped = list.groupBy { it.code }

        grouped.forEach { (code, measurements) ->
            val sorted = measurements.sortedBy { it.time }
            val latest = sorted.last()

            // Redis에서 10분 전 기준점 조회
            val redisKey = "ticker:baseline:$code"
            val baseline = redisService.getObject(redisKey, TickerMeasurement::class.java)

            if (baseline == null) {
                // 기준점이 없으면 현재 값을 저장하고 종료 (다음 사이클부터 비교 시작)
                redisService.setObject(redisKey, latest, Duration.ofMinutes(10))
                return@forEach
            }

            // === 변동률 계산 ===
            val priceChange = (latest.tradePrice - baseline.tradePrice) / baseline.tradePrice * 100
            val volumeChange = (latest.tradeVolume - baseline.tradeVolume) / baseline.tradeVolume * 100

            // === Trigger 조건 정의 ===
            
            // 1) 일반 변동 감지: 0.5% 이상 가격 변동 또는 10% 이상 거래량 변동
            val normalChange = abs(priceChange) > 0.5 || abs(volumeChange) > 10.0
            
            // 2) 강한 하락 신호 (명시적 표현)
            //    - dropStrong: -1% 이하 급락 (panic sell 가능성)
            //    - volumeCollapse: -30% 이하 거래량 급감 (매물 대기 상태, 추가 하락 전조)
            val dropStrong = priceChange < -1.0
            val volumeCollapse = volumeChange < -30.0
            val strongDrop = dropStrong || volumeCollapse

            // 최종 Trigger: 일반 변동이거나 강한 하락 신호일 때 AI 분석 요청
            // 참고: dropStrong은 normalChange에도 포함되지만, 명시적 표현으로 가독성 향상
            val trigger = normalChange || strongDrop

            if (trigger) {
                val prompt = """
                    Determine trend (spike/drop/none).
                    
                    Coin: $code
                    Price Change: ${"%.2f".format(priceChange)}%
                    Volume Change: ${"%.2f".format(volumeChange)}%
                    Strong Drop Signal: $strongDrop
                """.trimIndent()

                val direction = localAIService.askDirection(prompt)

                if (direction != SpikeDirection.UNCHANGED) {
                    results.add(AnalyzerResult(code, direction))
                    val emoji = if (direction == SpikeDirection.UP) "🚀" else "📉"
                    log.info { "$emoji AI Detected: $code ($direction) (Price: ${latest.tradePrice}, Change: ${"%.2f".format(priceChange)}%)" }
                }
            }
        }

        return results
    }
}