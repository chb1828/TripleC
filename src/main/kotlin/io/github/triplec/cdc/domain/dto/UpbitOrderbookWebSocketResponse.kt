package io.github.triplec.cdc.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.triplec.common.type.StreamType

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 22.
 */
data class UpbitOrderbookWebSocketResponse(
    @JsonProperty("type")
    val type: String,
    @JsonProperty("code")
    val code: String,
    // 서버 전송 시각 (milliseconds)
    @JsonProperty("timestamp")
    val timestamp: Long,
    // 전체 매도 잔량
    @JsonProperty("total_ask_size")
    val totalAskSize: Double,
    // 전체 매수 잔량
    @JsonProperty("total_bid_size")
    val totalBidSize: Double,
    // 호가 단위 리스트 (최대 30개)
    @JsonProperty("orderbook_units")
    val orderbookUnitResponses: List<OrderbookUnitResponse>,
    // 스트림 타입 (SNAPSHOT: 스냅샷, REALTIME: 실시간)
    @JsonProperty("stream_type")
    val streamType: StreamType,
    // 호가 레벨 (default = 0)
    val level: Int,
) {
    data class OrderbookUnitResponse(
        // 매도 호가 가격
        @JsonProperty("ask_price")
        val askPrice: Double,
        // 매수 호가 가격
        @JsonProperty("bid_price")
        val bidPrice: Double,
        // 매도 잔량
        @JsonProperty("ask_size")
        val askSize: Double,
        // 매수 잔량
        @JsonProperty("bid_size")
        val bidSize: Double,
    )
}
