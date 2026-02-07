package io.github.triplec.analyzer.service

import io.github.triplec.analyzer.domain.AnalyzerResult
import io.github.triplec.common.domain.measurement.BaseMeasurement

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 8. 24.
 */
interface MeasurementDetectAnalyzer<T : BaseMeasurement> {
    fun detect(list: Collection<T>): Collection<AnalyzerResult>
}
