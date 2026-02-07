package io.github.triplec.cdc.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.triplec.common.type.MarketStateType
import io.github.triplec.common.type.MarketWarningType
import io.github.triplec.common.type.OrderSideType
import io.github.triplec.common.type.StreamType
import io.github.triplec.common.type.TickerChangeType

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 16.
 */
data class UpbitTickerWebSocketResponse(
    val type: String,
    val code: String,
    // 시가
    @JsonProperty("opening_price")
    val openingPrice: Double,
    // 고가
    @JsonProperty("high_price")
    val highPrice: Double,
    // 저가
    @JsonProperty("low_price")
    val lowPrice: Double,
    // 현재가
    @JsonProperty("trade_price")
    val tradePrice: Double,
    // 전일 종가
    @JsonProperty("prev_closing_price")
    val prevClosingPrice: Double,
    // 전일 대비
    @JsonProperty("change")
    val change: TickerChangeType,
    // 부호 없는 전일 대비 값
    @JsonProperty("change_price")
    val changePrice: Double,
    // 전일 대비 값
    @JsonProperty("signed_change_price")
    val signedChangePrice: Double,
    // 부호 없는 전일 대비 등락율
    @JsonProperty("change_rate")
    val changeRate: Double,
    // 전일 대비 등락율
    @JsonProperty("signed_change_rate")
    val signedChangeRate: Double,
    // 가장 최근 거래량
    @JsonProperty("trade_volume")
    val tradeVolume: Double,
    // 누적 거래량 (UTC 0시 기준)
    @JsonProperty("acc_trade_volume")
    val accTradeVolume: Double,
    // 24시간 누적 거래량
    @JsonProperty("acc_trade_volume_24h")
    val accTradeVolume24h: Double,
    // 누적 거래대금(UTC 0시 기준)
    @JsonProperty("acc_trade_price")
    val accTradePrice: Double,
    // 24시간 누적 거래대금
    @JsonProperty("acc_trade_price_24h")
    val accTradePrice24h: Double,
    // 최근 거래 일자(UTC)
    @JsonProperty("trade_date")
    val tradeDate: String,
    // 최근 거래 시각(UTC)
    @JsonProperty("trade_time")
    val tradeTime: String,
    // 체결 타임스탬프 (milliseconds)
    @JsonProperty("trade_timestamp")
    val tradeTimestamp: Long,
    // 매수/매도 구분
    @JsonProperty("ask_bid")
    val askBid: OrderSideType,
    // 누적 매도량
    @JsonProperty("acc_ask_volume")
    val accAskVolume: Double,
    // 누적 매수량
    @JsonProperty("acc_bid_volume")
    val accBidVolume: Double,
    // 52주 최고가
    @JsonProperty("highest_52_week_price")
    val highest52WeekPrice: Double,
    // 52주 최고가 달성일
    @JsonProperty("highest_52_week_date")
    val highest52WeekDate: String,
    // 52주 최저가
    @JsonProperty("lowest_52_week_price")
    val lowest52WeekPrice: Double,
    // 52주 최저가 달성일
    @JsonProperty("lowest_52_week_date")
    val lowest52WeekDate: String,
    // 거래 상태
    @JsonProperty("market_state")
    val marketState: MarketStateType,
    // 거래 지원 종료일
    @JsonProperty("delisting_date")
    val delistingDate: String?,
    // 유의 종목 여부
    @JsonProperty("market_warning")
    val marketWarning: MarketWarningType,
    // 타임스탬프 (millisecond)
    @JsonProperty("timestamp")
    val timestamp: Long,
    // 스트림 타입
    @JsonProperty("stream_type")
    val streamType: StreamType,
)
