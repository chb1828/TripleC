package io.github.triplec.analyzer.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.triplec.analyzer.domain.SpikeDetectionEvent
import io.github.triplec.analyzer.domain.SpikeDirection
import io.github.triplec.common.service.RedisService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

private val log = KotlinLogging.logger {}

/**
 * Analyzer들의 급등/급락 감지 이벤트를 수신하여 최종 판단하는 Listener
 * 
 * ## 목적
 * - 4개 Analyzer(TICKER, CANDLE, ORDERBOOK, TRADE)의 결과를 통합
 * - 모든 Analyzer가 같은 방향을 가리킬 때만 최종 확정
 * - Redis를 통한 중복 알림 방지
 * 
 * ## 최종 확정 조건
 * 1. 같은 코인에 대해 4개 Analyzer 모두 감지
 * 2. 모두 같은 방향(ALL UP or ALL DOWN)
 * 3. 모든 감지 결과가 **20분 이내**에 발생
 * 4. **최근 10분 이내 중복 확정 없음** (Redis 조회)
 * 
 * ## 버퍼 관리
 * - 각 감지 결과를 타임스탬프와 함께 저장
 * - 20분이 지난 오래된 데이터는 자동 정리 (1분마다)
 * - 이유: 각 Analyzer의 Redis baseline TTL이 10분이므로, 20분이면 충분한 여유
 * 
 * ## 중복 알림 방지
 * - Redis 키: `spike:confirmed:{코인코드}:{방향}`
 * - TTL: 10분
 * - 같은 코인의 같은 방향 급등/급락이 10분 이내에 재차 확정되면 무시
 * 
 * @author 최현범(Jayce) / hb.choi@sk.com
 * @since 2025. 11. 22.
 */
@Component
class SpikeDetectionEventListener(
    private val redisService: RedisService
) {

    companion object {
        // 버퍼 만료 시간: 20분 (각 Analyzer의 baseline TTL 10분의 2배)
        private val BUFFER_EXPIRATION = Duration.ofMinutes(20)
        
        // 중복 알림 방지 시간: 10분
        private val DUPLICATE_PREVENTION_DURATION = Duration.ofMinutes(10)
    }

    /**
     * 감지 결과 + 타임스탬프
     */
    data class DetectionResult(
        val direction: SpikeDirection,
        val timestamp: Instant
    )

    // 코인별로 4개 Analyzer 결과를 임시 저장
    // Key: 코인 코드 (e.g., "KRW-BTC")
    // Value: Map<MeasurementType, DetectionResult>
    private val detectionBuffer = ConcurrentHashMap<String, MutableMap<String, DetectionResult>>()

    @EventListener
    fun onSpikeDetection(event: SpikeDetectionEvent) {
        log.debug { "이벤트 수신: ${event.measurementType} - ${event.results.size}개 감지" }

        val now = Instant.now()

        // 감지된 코인별로 처리
        event.results.forEach { result ->
            val code = result.code
            val direction = result.direction

            // 버퍼에 저장 (타임스탬프 포함)
            val codeBuffer = detectionBuffer.getOrPut(code) { mutableMapOf() }
            codeBuffer[event.measurementType] = DetectionResult(direction, now)

            log.debug { "[$code] ${event.measurementType} = $direction (현재: ${codeBuffer.size}/4)" }

            // 4개가 모였는지 확인
            if (codeBuffer.size == 4) {
                checkAndConfirm(code, codeBuffer)
                // 버퍼 초기화 (확인 후 바로 제거)
                detectionBuffer.remove(code)
            }
        }
    }

    /**
     * 4개 Analyzer 결과를 검증하고 최종 확정
     */
    private fun checkAndConfirm(code: String, results: Map<String, DetectionResult>) {
        val now = Instant.now()

        // === 시간 검증: 모든 결과가 20분 이내인지 확인 ===
        val allRecent = results.values.all { result ->
            Duration.between(result.timestamp, now) < BUFFER_EXPIRATION
        }

        if (!allRecent) {
            log.warn { "[$code] 일부 감지 결과가 20분을 초과하여 무시됨" }
            return
        }

        // === 방향 검증: 모두 같은 방향인지 체크 ===
        val ticker = results["TICKER"]?.direction
        val candle = results["CANDLE"]?.direction
        val orderbook = results["ORDERBOOK"]?.direction
        val trade = results["TRADE"]?.direction

        log.info { "[$code] 4개 Analyzer 감지 완료: TICKER=$ticker, CANDLE=$candle, ORDERBOOK=$orderbook, TRADE=$trade" }

        val allUp = ticker == SpikeDirection.UP && 
                    candle == SpikeDirection.UP && 
                    orderbook == SpikeDirection.UP && 
                    trade == SpikeDirection.UP
        
        val allDown = ticker == SpikeDirection.DOWN && 
                      candle == SpikeDirection.DOWN && 
                      orderbook == SpikeDirection.DOWN && 
                      trade == SpikeDirection.DOWN

        when {
            allUp -> confirmSpike(code, SpikeDirection.UP)
            allDown -> confirmSpike(code, SpikeDirection.DOWN)
            else -> {
                log.info { "[$code] 방향 불일치로 무시: TICKER=$ticker, CANDLE=$candle, ORDERBOOK=$orderbook, TRADE=$trade" }
            }
        }
    }

    /**
     * 급등/급락 최종 확정 및 중복 체크
     */
    private fun confirmSpike(code: String, direction: SpikeDirection) {
        val redisKey = "spike:confirmed:$code:$direction"
        
        // === 중복 체크: 10분 이내 이미 확정되었는지 확인 ===
        if (redisService.exist(redisKey)) {
            log.info { "[$code] 10분 이내 이미 ${direction.name} 확정됨, 중복 알림 방지" }
            return
        }

        // === 최종 확정 ===
        val emoji = if (direction == SpikeDirection.UP) "🚀" else "📉"
        val directionKr = if (direction == SpikeDirection.UP) "급등" else "급락"
        
        log.warn { "$emoji 🚨 [CONFIRMED SPIKE ${direction.name}] $code - 4개 Analyzer 모두 $directionKr 감지!" }

        // === Redis에 확정 기록 (10분 TTL) ===
        redisService.set(redisKey, Instant.now().toString(), DUPLICATE_PREVENTION_DURATION)

        // TODO: 알림 발송 (Slack, Discord, etc.)
        // TODO: InfluxDB에 확정 이력 저장 (통계 목적)
    }

    /**
     * 1분마다 오래된 버퍼 데이터 정리 (20분 이상 된 데이터 제거)
     * Spring @Scheduled 사용
     */
    @Scheduled(fixedRate = 60_000) // 1분마다 실행
    fun cleanupOldDetections() {
        val now = Instant.now()
        var cleanedCount = 0

        detectionBuffer.forEach { (code, results) ->
            // 20분 이상 된 결과 제거
            val toRemove = results.filter { (_, result) ->
                Duration.between(result.timestamp, now) >= BUFFER_EXPIRATION
            }.keys

            if (toRemove.isNotEmpty()) {
                toRemove.forEach { measurementType ->
                    results.remove(measurementType)
                    cleanedCount++
                }
                log.debug { "[$code] 오래된 감지 결과 ${toRemove.size}개 정리: $toRemove" }
            }

            // 버퍼가 비었으면 코인 자체를 제거
            if (results.isEmpty()) {
                detectionBuffer.remove(code)
            }
        }

        if (cleanedCount > 0) {
            log.info { "버퍼 정리 완료: ${cleanedCount}개 오래된 감지 결과 제거" }
        }
    }
}
