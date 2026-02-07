package io.github.triplec.common.type

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 16.
 */
enum class MarketStateType(
    override val code: String,
    override val description: String,
) : CodeType {
    PREVIEW("ASK", "입금지원"),
    ACTIVE("BID", "거래지원가능"),
    DELISTED("DELISTED", "거래 지원 종료"),
    PREDELISTING("PREDELISTING", "상장폐지예정"),
}
