package io.github.triplec.common.type

import com.fasterxml.jackson.annotation.JsonValue

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 13.
 */
enum class WebSocketTopicType(
    @get:JsonValue
    override val code: String,
    override val description: String,
) : CodeType {
    TICKER("ticker", "현재 시세 정보"),
    TRADE("trade", "체결 정보"),
    ORDERBOOK("orderbook", "호가 정보"),
    CANDLE("candle", "캔들 정보"),
}
