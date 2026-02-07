package io.github.triplec.cdc.domain.dto

import io.github.triplec.constant.UpbitConstant.SOCKET_MESSAGE_TYPE_TICKET

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 13.
 */
data class UpbitWebSocketRequestParam(
    val ticket: String = SOCKET_MESSAGE_TYPE_TICKET,
    val type: String,
    val codes: Collection<String>,
    val isOnlySnapshot: Boolean? = null,
    val isOnlyRealtime: Boolean? = null,
) {
    fun toPayload(): List<Map<String, Any>> =
        listOf(
            mapOf("ticket" to ticket),
            buildMap {
                put("type", type)
                put("codes", codes)
                isOnlySnapshot?.let { put("is_only_snapshot", it) }
                isOnlyRealtime?.let { put("is_only_realtime", it) }
            },
        )
}
