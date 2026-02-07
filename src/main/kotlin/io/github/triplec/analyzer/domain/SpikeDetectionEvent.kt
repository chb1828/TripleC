package io.github.triplec.analyzer.domain

import java.time.Instant

/**
 * Analyzer가 급등/급락을 감지했을 때 발행하는 이벤트
 * 
 * ## 용도
 * - 4개 Analyzer(Ticker, Candle, Orderbook, Trade)가 각각 감지 결과를 발행
 * - SpikeDetectionEventListener가 수신하여 통합 판단
 * 
 * ## 통합 판단 로직
 * - 같은 코인에 대해 4개 Analyzer 결과를 수집
 * - 모두 같은 방향(UP or DOWN)일 때만 InfluxDB 조회
 * - 중복 감지 방지 후 최종 확정
 * 
 * @param measurementType Analyzer 타입 ("TICKER", "CANDLE", "ORDERBOOK", "TRADE")
 * @param results 감지된 급등/급락 결과 리스트
 * @param timestamp 이벤트 발행 시각
 */
data class SpikeDetectionEvent(
    val measurementType: String,
    val results: Collection<AnalyzerResult>,
    val timestamp: Instant = Instant.now()
)
