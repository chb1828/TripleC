package io.github.triplec.cdc.repository

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 22.
 */
interface CdcMeasurementRepository<T> {
    fun insertAll(measurements: Collection<T>)
}
