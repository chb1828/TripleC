package io.github.triplec.common.type

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 16.
 */
enum class StreamType(
    override val code: String,
    override val description: String,
) : CodeType {
    SNAPSHOT("SNAPSHOT", "스냅샷"),
    REALTIME("REALTIME", "실시간"),
}
