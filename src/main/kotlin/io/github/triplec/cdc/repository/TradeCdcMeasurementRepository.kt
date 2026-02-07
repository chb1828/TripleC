package io.github.triplec.cdc.repository

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.domain.WritePrecision
import io.github.triplec.common.domain.measurement.TradeMeasurement
import org.springframework.stereotype.Repository

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 15.
 */
@Repository
class TradeCdcMeasurementRepository(
    private val influxDBClient: InfluxDBClient,
) : CdcMeasurementRepository<TradeMeasurement> {
    private val writeApi by lazy { influxDBClient.writeApiBlocking }

    fun insert(tradeMeasurement: TradeMeasurement) {
        writeApi.writeMeasurement(WritePrecision.MS, tradeMeasurement)
    }

    override fun insertAll(measurements: Collection<TradeMeasurement>) {
        writeApi.writeMeasurements(WritePrecision.MS, measurements.toList())
    }
}
