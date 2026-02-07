package io.github.triplec.common.util

import java.nio.ByteBuffer

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 16.
 */
object Utils {
    object Byte {
        fun toUtf8String(buffer: ByteBuffer) = Charsets.UTF_8.decode(buffer.slice()).toString()
    }
}
