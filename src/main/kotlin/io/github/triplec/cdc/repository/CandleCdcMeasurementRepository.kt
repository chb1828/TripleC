package io.github.triplec.cdc.repository

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.domain.WritePrecision
import io.github.triplec.common.domain.measurement.CandleMeasurement
import org.springframework.stereotype.Repository

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 15.
 */
@Repository
class CandleCdcMeasurementRepository(
    private val influxDBClient: InfluxDBClient,
) : CdcMeasurementRepository<CandleMeasurement> {
    private val writeApi by lazy { influxDBClient.writeApiBlocking }

    fun insert(tickerMeasurement: CandleMeasurement) {
        writeApi.writeMeasurement(WritePrecision.MS, tickerMeasurement)
    }

    override fun insertAll(measurements: Collection<CandleMeasurement>) {
        writeApi.writeMeasurements(WritePrecision.MS, measurements.toList())
    }
}
