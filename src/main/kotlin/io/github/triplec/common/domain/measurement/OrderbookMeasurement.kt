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
@Measurement(name = "orderbook")
data class OrderbookMeasurement(
    @Column(tag = true, name = "code")
    override val code: String,
    @Column(name = "total_ask_size")
    val totalAskSize: Double,
    @Column(name = "total_bid_size")
    val totalBidSize: Double,
    @Column(name = "top_ask_price")
    val topAskPrice: Double,
    @Column(name = "top_bid_price")
    val topBidPrice: Double,
    @Column(name = "top_ask_size")
    val topAskSize: Double,
    @Column(name = "top_bid_size")
    val topBidSize: Double,
    @Column(name = "avg_ask_price_top5")
    val avgAskPriceTop5: Double,
    @Column(name = "avg_bid_price_top5")
    val avgBidPriceTop5: Double,
    @Column(name = "sum_ask_size_top5")
    val sumAskSizeTop5: Double,
    @Column(name = "sum_bid_size_top5")
    val sumBidSizeTop5: Double,
    @Column(name = "imbalance_ratio")
    val imbalanceRatio: Double,
    @Column(timestamp = true)
    override val time: Instant,
) : BaseMeasurement
