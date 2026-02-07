package io.github.triplec.common.type

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 16.
 */
enum class TickerChangeType(
    override val code: String,
    override val description: String,
) : CodeType {
    RISE("RISE", "상승"),
    EVEN("EVEN", "보합"),
    FALL("FALL", "하락"),
}
