package io.github.triplec.analyzer.domain

data class TickerSpikeState(
    val code: String,
    val baselinePrice: Double,     // 기준가
    val lastPrice: Double,         // 직전 샘플 가격
    val consecutiveCount: Int,   // 연속 상승/하락 카운트 (상승 +1, 하락 -1)
    val lastUpdatedEpochSec: Long  // 마지막 업데이트 시각
)