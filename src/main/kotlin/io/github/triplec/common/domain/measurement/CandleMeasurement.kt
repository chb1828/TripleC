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
@Measurement(name = "candle")
data class CandleMeasurement(
    @Column(tag = true, name = "code")
    override val code: String,
    @Column(name = "open_price")
    val openPrice: Double,
    @Column(name = "high_price")
    val highPrice: Double,
    @Column(name = "low_price")
    val lowPrice: Double,
    @Column(name = "close_price")
    val closePrice: Double,
    @Column(name = "volume")
    val volume: Double,
    @Column(name = "trade_amount")
    val tradeAmount: Double,
    @Column(timestamp = true)
    override val time: Instant,
) : BaseMeasurement
