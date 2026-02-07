package io.github.triplec.common.type

/**
* 설명:
*
* @author 최현범(Jayce) / hb.choi@dreamus.io
* @since 2025. 4. 16.
*/
enum class MarketWarningType(
    override val code: String,
    override val description: String,
) : CodeType {
    NONE("NONE", "해당 없음"),
    CAUTION("CAUTION", "투자 유의"),
}
