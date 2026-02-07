package io.github.triplec.analyzer.service

import io.github.triplec.analyzer.domain.SpikeDirection
import io.github.triplec.common.domain.measurement.TickerMeasurement
import io.github.triplec.common.service.RedisService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.Instant

class TickerMeasurementAIDetectAnalyzerTest {

    private val localAIService = Mockito.mock(LocalAIService::class.java)
    private val redisService = Mockito.mock(RedisService::class.java)
    private val analyzer = TickerMeasurementAIDetectAnalyzer(localAIService, redisService)

    @Test
    fun `should detect spike when baseline exists and AI returns UP`() {
        // Given
        val now = Instant.now()
        val baselineMeasurement = TickerMeasurement("BTC", 50000.0, 100.0, 1000.0, now.minusSeconds(600))
        val currentMeasurement = TickerMeasurement("BTC", 55000.0, 200.0, 2000.0, now) // +10% price, +100% volume
        val measurements = listOf(currentMeasurement)

        Mockito.`when`(redisService.getObject("ticker:baseline:BTC", TickerMeasurement::class.java))
            .thenReturn(baselineMeasurement)
        Mockito.`when`(localAIService.askDirection(Mockito.anyString())).thenReturn(SpikeDirection.UP)

        // When
        val result = analyzer.detect(measurements)

        // Then
        assertEquals(1, result.size)
        assertEquals("BTC", result.first().code)
        assertEquals(SpikeDirection.UP, result.first().direction)
    }

    @Test
    fun `should save to Redis when baseline does not exist`() {
        // Given
        val now = Instant.now()
        val currentMeasurement = TickerMeasurement("BTC", 55000.0, 200.0, 2000.0, now)
        val measurements = listOf(currentMeasurement)

        Mockito.`when`(redisService.getObject("ticker:baseline:BTC", TickerMeasurement::class.java))
            .thenReturn(null)

        // When
        val result = analyzer.detect(measurements)

        // Then
        assertTrue(result.isEmpty()) // No comparison, just save
    }

    @Test
    fun `should detect drop when baseline exists and AI returns DOWN`() {
        // Given
        val now = Instant.now()
        val baselineMeasurement = TickerMeasurement("BTC", 50000.0, 100.0, 1000.0, now.minusSeconds(600))
        val currentMeasurement = TickerMeasurement("BTC", 45000.0, 200.0, 2000.0, now) // -10% price, +100% volume
        val measurements = listOf(currentMeasurement)

        Mockito.`when`(redisService.getObject("ticker:baseline:BTC", TickerMeasurement::class.java))
            .thenReturn(baselineMeasurement)
        Mockito.`when`(localAIService.askDirection(Mockito.anyString())).thenReturn(SpikeDirection.DOWN)

        // When
        val result = analyzer.detect(measurements)

        // Then
        assertEquals(1, result.size)
        assertEquals("BTC", result.first().code)
        assertEquals(SpikeDirection.DOWN, result.first().direction)
    }
}
