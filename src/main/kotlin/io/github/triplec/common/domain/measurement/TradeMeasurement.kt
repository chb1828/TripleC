package io.github.triplec.common.domain.measurement

import com.influxdb.annotations.Column
import com.influxdb.annotations.Measurement
import io.github.triplec.common.type.OrderSideType
import java.time.Instant

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 5. 18.
 */
@Measurement(name = "trade")
data class TradeMeasurement(
    @Column(tag = true, name = "code")
    override val code: String,
    @Column(name = "trade_price")
    val tradePrice: Double,
    @Column(name = "trade_volume")
    val tradeVolume: Double,
    @Column(name = "askBid")
    val askBid: OrderSideType,
    @Column(timestamp = true)
    override val time: Instant,
) : BaseMeasurement
