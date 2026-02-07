package io.github.triplec.constant

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 13.
 */
object UpbitConstant {
    const val SOCKET_URL = "wss://api.upbit.com/websocket/v1"
    const val SOCKET_MESSAGE_TYPE_TICKET = "tripleC"
    val COIN_CODE: Collection<String> =
        setOf(
            "KRW-BTC",
            "KRW-ETH",
            "KRW-XRP",
            "KRW-SOL",
            "KRW-MLK",
            "KRW-AERGO",
            "KRW-STX",
            "KRW-TT",
            "KRW-USDT",
            "KRW-AUCTION",
            "KRW-ZRO",
            "KRW-ZETA",
            "KRW-NEO",
            "KRW-VANA",
        )
}
