package io.github.triplec.common.domain.measurement

import java.time.Instant

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 22.
 */
interface BaseMeasurement {
    val code: String
    val time: Instant
}
