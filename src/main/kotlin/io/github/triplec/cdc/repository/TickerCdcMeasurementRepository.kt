package io.github.triplec.cdc.repository

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.domain.WritePrecision
import io.github.triplec.common.domain.measurement.TickerMeasurement
import org.springframework.stereotype.Repository

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 15.
 */
@Repository
class TickerCdcMeasurementRepository(
    private val influxDBClient: InfluxDBClient,
) : CdcMeasurementRepository<TickerMeasurement> {
    private val writeApi by lazy { influxDBClient.writeApiBlocking }

    fun insert(tickerMeasurement: TickerMeasurement) {
        writeApi.writeMeasurement(WritePrecision.MS, tickerMeasurement)
    }

    override fun insertAll(measurements: Collection<TickerMeasurement>) {
        writeApi.writeMeasurements(WritePrecision.MS, measurements.toList())
    }
}
