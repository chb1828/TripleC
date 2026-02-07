package io.github.triplec.cdc.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.triplec.common.type.OrderSideType
import io.github.triplec.common.type.StreamType

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 22.
 */
data class UpbitTradeWebSocketResponse(
    val type: String,
    val code: String,
    // 체결 가격
    @JsonProperty("trade_price")
    val tradePrice: Double,
    // 체결량
    @JsonProperty("trade_volume")
    val tradeVolume: Double,
    // 매수/매도 구분 (ASK: 매도, BID: 매수)
    @JsonProperty("ask_bid")
    val askBid: OrderSideType,
    // 전일 종가
    @JsonProperty("prev_closing_price")
    val prevClosingPrice: Double,
    // 전일 대비 (RISE: 상승, EVEN: 보합, FALL: 하락)
    @JsonProperty("change")
    val change: String,
    // 부호 없는 전일 대비 값
    @JsonProperty("change_price")
    val changePrice: Double,
    // 체결 일자 (UTC 기준, yyyy-MM-dd)
    @JsonProperty("trade_date")
    val tradeDate: String,
    // 체결 시각 (UTC 기준, HH:mm:ss)
    @JsonProperty("trade_time")
    val tradeTime: String,
    // 체결 타임스탬프 (milliseconds)
    @JsonProperty("trade_timestamp")
    val tradeTimestamp: Long,
    // 서버 타임스탬프 (milliseconds)
    @JsonProperty("timestamp")
    val timestamp: Long,
    // 체결 번호 (Unique, Long 타입)
    @JsonProperty("sequential_id")
    val sequentialId: Long,
    // 최우선 매도 호가
    @JsonProperty("best_ask_price")
    val bestAskPrice: Double,
    // 최우선 매도 잔량
    @JsonProperty("best_ask_size")
    val bestAskSize: Double,
    // 최우선 매수 호가
    @JsonProperty("best_bid_price")
    val bestBidPrice: Double,
    // 최우선 매수 잔량
    @JsonProperty("best_bid_size")
    val bestBidSize: Double,
    // 스트림 타입 (SNAPSHOT: 스냅샷, REALTIME: 실시간)
    @JsonProperty("stream_type")
    val streamType: StreamType,
)
