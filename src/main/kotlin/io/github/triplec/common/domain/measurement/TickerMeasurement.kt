package io.github.triplec.common.domain.measurement

import com.influxdb.annotations.Column
import com.influxdb.annotations.Measurement
import java.time.Instant

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 19.
 */
@Measurement(name = "ticker")
data class TickerMeasurement(
    @Column(tag = true, name = "code")
    override val code: String,
    @Column(name = "trade_price")
    val tradePrice: Double,
    @Column(name = "trade_volume")
    val tradeVolume: Double,
    @Column(name = "trade_volume_24h")
    val tradeVolume24h: Double,
    @Column(timestamp = true)
    override val time: Instant,
) : BaseMeasurement
