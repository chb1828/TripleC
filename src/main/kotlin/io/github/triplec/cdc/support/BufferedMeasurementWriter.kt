package io.github.triplec.cdc.support

import io.github.triplec.analyzer.domain.SpikeDetectionEvent
import io.github.triplec.analyzer.service.MeasurementDetectAnalyzer
import io.github.triplec.cdc.repository.CdcMeasurementRepository
import io.github.triplec.common.domain.measurement.BaseMeasurement
import org.springframework.context.ApplicationEventPublisher
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Measurement 데이터를 버퍼링하여 InfluxDB에 일괄 저장하는 Writer
 * 
 * ## 책임
 * 1. 데이터 버퍼링 및 일괄 저장
 * 2. Analyzer를 통한 급등/급락 감지
 * 3. 감지 결과를 Spring 이벤트로 발행
 * 
 * ## 이벤트 발행
 * - Analyzer가 급등/급락을 감지하면 `SpikeDetectionEvent` 발행
 * - Listener가 이벤트를 수신하여 최종 판단
 * 
 * @param measurementType 데이터 타입 (TICKER, CANDLE, ORDERBOOK, TRADE)
 * @param eventPublisher Spring 이벤트 발행기 (nullable - 테스트 등에서 null 가능)
 */
class BufferedMeasurementWriter<T : BaseMeasurement>(
    private val dataChannel: DataChannel<T>,
    private val cdcMeasurementRepository: CdcMeasurementRepository<T>,
    private val detectAnalyzer: MeasurementDetectAnalyzer<T>? = null,
    private val measurementType: String? = null,
    private val eventPublisher: ApplicationEventPublisher? = null,
    private val autoFlushBuffer: AutoFlushBuffer<T> =
        AutoFlushBuffer(
            100,
            Duration.ofSeconds(5),
        ) { list ->
            // 1) InfluxDB 저장
            cdcMeasurementRepository.insertAll(list)
            
            // 2) Analyzer 실행 및 이벤트 발행
            detectAnalyzer?.let { analyzer ->
                val results = analyzer.detect(list)
                
                // 급등/급락이 감지되면 이벤트 발행
                if (results.isNotEmpty() && measurementType != null && eventPublisher != null) {
                    val event = SpikeDetectionEvent(
                        measurementType = measurementType,
                        results = results
                    )
                    eventPublisher.publishEvent(event)
                }
            }
        },
) : Runnable,
    AutoCloseable {
    private val closed = AtomicBoolean(false)

    fun write(measurement: T) {
        dataChannel.add(measurement)
    }

    override fun run() {
        autoFlushBuffer.flush()

        while (!closed.get()) {
            val data = dataChannel.poll() ?: continue
            autoFlushBuffer.add(data)
        }

        autoFlushBuffer.flush()
    }

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            dataChannel.close()
            autoFlushBuffer.flush()
        }
    }
}
