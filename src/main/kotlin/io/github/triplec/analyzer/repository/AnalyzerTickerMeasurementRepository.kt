package io.github.triplec.analyzer.repository

import com.influxdb.client.InfluxDBClient
import io.github.triplec.common.support.FluxQueryBuilder
import org.springframework.stereotype.Repository

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 5. 18.
 */
@Repository
class AnalyzerTickerMeasurementRepository(
    private val influxDBClient: InfluxDBClient,
) {
    private val queryApi by lazy { influxDBClient.queryApi }

    fun getPriceChangeRate(
        code: String,
        durationSeconds: Long = 5,
    ): Double {
        val query =
            FluxQueryBuilder()
                .from("triplec-bucket")
                .range("-${durationSeconds}s")
                .filter("_measurement", "ticker")
                .filter("_field", "trade_price")
                .filter("code", code)
                .sort("_time")
                .reduce("first", "last")
                .map("spike", "(r.last - r.first) / r.first")
                .build()

        val tables = queryApi.query(query)
        val records = tables.flatMap { it.records }

        return records
            .firstOrNull()
            ?.value
            ?.toString()
            ?.toDouble() ?: 0.0
    }
}
