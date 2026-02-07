package io.github.triplec.cdc.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.triplec.common.type.StreamType

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 22.
 */
data class UpbitCandleWebSocketResponse(
    @JsonProperty("type")
    val type: String,
    @JsonProperty("code")
    val code: String,
    // UTC 기준 캔들 시간
    @JsonProperty("candle_date_time_utc")
    val candleDateTimeUtc: String,
    // KST 기준 캔들 시간
    @JsonProperty("candle_date_time_kst")
    val candleDateTimeKst: String,
    // 시가
    @JsonProperty("opening_price")
    val openingPrice: Double,
    // 고가
    @JsonProperty("high_price")
    val highPrice: Double,
    // 저가
    @JsonProperty("low_price")
    val lowPrice: Double,
    // 종가
    @JsonProperty("trade_price")
    val tradePrice: Double,
    // 누적 거래량
    @JsonProperty("candle_acc_trade_volume")
    val candleAccTradeVolume: Double,
    // 누적 거래 금액
    @JsonProperty("candle_acc_trade_price")
    val candleAccTradePrice: Double,
    // 서버 전송 timestamp
    @JsonProperty("timestamp")
    val timestamp: Long,
    // 스트림 타입
    @JsonProperty("stream_type")
    val streamType: StreamType,
)
