package io.github.triplec.analyzer.domain

/**
 * 설명:
 *
 * @author 서버개발 / g-dev-server@dreamus.io
 */
data class AnalyzerResult(
    val code: String,
    val direction: SpikeDirection
)