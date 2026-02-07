package io.github.triplec.cdc.repository

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.domain.WritePrecision
import io.github.triplec.common.domain.measurement.OrderbookMeasurement
import org.springframework.stereotype.Repository

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 15.
 */
@Repository
class OrderbookCdcMeasurementRepository(
    private val influxDBClient: InfluxDBClient,
) : CdcMeasurementRepository<OrderbookMeasurement> {
    private val writeApi by lazy { influxDBClient.writeApiBlocking }

    fun insert(tickerMeasurement: OrderbookMeasurement) {
        writeApi.writeMeasurement(WritePrecision.MS, tickerMeasurement)
    }

    override fun insertAll(measurements: Collection<OrderbookMeasurement>) {
        writeApi.writeMeasurements(WritePrecision.MS, measurements.toList())
    }
}
