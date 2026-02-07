package io.github.triplec.cdc.handler

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.triplec.cdc.domain.dto.UpbitCandleWebSocketResponse
import io.github.triplec.cdc.repository.CandleCdcMeasurementRepository
import io.github.triplec.cdc.support.BufferedMeasurementWriter
import io.github.triplec.cdc.support.DataChannel
import io.github.triplec.cdc.support.SimpleTokenBucketRateLimiter
import io.github.triplec.common.domain.measurement.CandleMeasurement
import io.github.triplec.common.type.WebSocketTopicType
import io.github.triplec.common.util.Utils
import io.github.triplec.constant.UpbitConstant.COIN_CODE
import io.github.triplec.analyzer.service.CandleMeasurementAIDetectAnalyzer
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.client.WebSocketClient
import java.time.Instant

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 17.
 */

@Component
class CandleMessageHandler(
    webSocketClient: WebSocketClient,
    rateLimiter: SimpleTokenBucketRateLimiter,
    cdcCandleMeasurementRepository: CandleCdcMeasurementRepository,
    candleMeasurementDetectAnalyzer: CandleMeasurementAIDetectAnalyzer,
    eventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
) : AbstractMessageHandler<CandleMeasurement>(
        webSocketClient,
        BufferedMeasurementWriter(
            dataChannel = DataChannel(1000),
            cdcMeasurementRepository = cdcCandleMeasurementRepository,
            detectAnalyzer = candleMeasurementDetectAnalyzer,
            measurementType = "CANDLE",
            eventPublisher = eventPublisher,
        ),
        rateLimiter,
        objectMapper,
    ) {
    override fun consume(message: BinaryMessage): CandleMeasurement {
        val jsonString = Utils.Byte.toUtf8String(message.payload)
        val response = objectMapper.readValue(jsonString, UpbitCandleWebSocketResponse::class.java)

        return CandleMeasurement(
            code = response.code,
            openPrice = response.openingPrice,
            highPrice = response.highPrice,
            lowPrice = response.lowPrice,
            closePrice = response.tradePrice,
            volume = response.candleAccTradeVolume,
            tradeAmount = response.candleAccTradePrice,
            time = Instant.ofEpochMilli(response.timestamp),
        )
    }

    override fun codes(): Collection<String> = COIN_CODE

    // 초봉
    override fun requestType(): String = "${WebSocketTopicType.CANDLE.code}.1s"
}
