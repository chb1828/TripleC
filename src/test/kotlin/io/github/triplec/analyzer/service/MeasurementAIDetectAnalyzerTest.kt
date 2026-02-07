package io.github.triplec.analyzer.service

import io.github.triplec.analyzer.domain.SpikeDirection
import io.github.triplec.common.domain.measurement.CandleMeasurement
import io.github.triplec.common.domain.measurement.OrderbookMeasurement
import io.github.triplec.common.service.RedisService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.Instant

class MeasurementAIDetectAnalyzerTest {

    private val localAIService = Mockito.mock(LocalAIService::class.java)
    private val redisService = Mockito.mock(RedisService::class.java)

    @Test
    fun `Candle Analyzer should detect bullish reversal when baseline exists`() {
        val analyzer = CandleMeasurementAIDetectAnalyzer(localAIService, redisService)
        val now = Instant.now()
        val baseline = CandleMeasurement("BTC", 50000.0, 50100.0, 49900.0, 50000.0, 100.0, 5000000.0, now.minusSeconds(600))
        val current = CandleMeasurement("BTC", 50000.0, 52000.0, 50000.0, 52000.0, 200.0, 10400000.0, now)
        val measurements = listOf(current)

        Mockito.`when`(redisService.getObject("candle:baseline:BTC", CandleMeasurement::class.java))
            .thenReturn(baseline)
        Mockito.`when`(localAIService.askDirection(Mockito.anyString()))
            .thenReturn(SpikeDirection.UP)

        val result = analyzer.detect(measurements)
        assertEquals(1, result.size)
        assertEquals(SpikeDirection.UP, result.first().direction)
    }

    @Test
    fun `Orderbook Analyzer should detect selling pressure when baseline exists`() {
        val analyzer = OrderbookMeasurementAIDetectAnalyzer(localAIService, redisService)
        val now = Instant.now()
        val baseline = OrderbookMeasurement("BTC", 500.0, 500.0, 50100.0, 50000.0, 10.0, 10.0, 50100.0, 50000.0, 50.0, 50.0, 0.5, now.minusSeconds(600))
        val current = OrderbookMeasurement("BTC", 1000.0, 100.0, 50100.0, 50000.0, 10.0, 10.0, 50100.0, 50000.0, 50.0, 50.0, 0.1, now)
        val measurements = listOf(current)

        Mockito.`when`(redisService.getObject("orderbook:baseline:BTC", OrderbookMeasurement::class.java))
            .thenReturn(baseline)
        Mockito.`when`(localAIService.askDirection(Mockito.anyString()))
            .thenReturn(SpikeDirection.DOWN)

        val result = analyzer.detect(measurements)
        assertEquals(1, result.size)
        assertEquals(SpikeDirection.DOWN, result.first().direction)
    }
}
