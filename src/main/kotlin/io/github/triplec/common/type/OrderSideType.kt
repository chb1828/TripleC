package io.github.triplec.common.type

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 16.
 */
enum class OrderSideType(
    override val code: String,
    override val description: String,
) : CodeType {
    ASK("ASK", "매도"),
    BID("BID", "매수"),
}
