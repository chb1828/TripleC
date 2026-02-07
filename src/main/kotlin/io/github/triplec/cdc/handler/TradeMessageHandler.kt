package io.github.triplec.cdc.handler

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.triplec.cdc.domain.dto.UpbitTradeWebSocketResponse
import io.github.triplec.cdc.repository.TradeCdcMeasurementRepository
import io.github.triplec.cdc.support.BufferedMeasurementWriter
import io.github.triplec.cdc.support.DataChannel
import io.github.triplec.cdc.support.SimpleTokenBucketRateLimiter
import io.github.triplec.common.domain.measurement.TradeMeasurement
import io.github.triplec.common.type.WebSocketTopicType
import io.github.triplec.common.util.Utils
import io.github.triplec.constant.UpbitConstant.COIN_CODE
import io.github.triplec.analyzer.service.TradeMeasurementAIDetectAnalyzer
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.client.WebSocketClient
import java.time.Instant

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 5. 18.
 */
@Component
class TradeMessageHandler(
    webSocketClient: WebSocketClient,
    rateLimiter: SimpleTokenBucketRateLimiter,
    tradeMeasurementRepository: TradeCdcMeasurementRepository,
    tradeMeasurementDetectAnalyzer: TradeMeasurementAIDetectAnalyzer,
    eventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
) : AbstractMessageHandler<TradeMeasurement>(
        webSocketClient,
        BufferedMeasurementWriter(
            dataChannel = DataChannel(1000),
            cdcMeasurementRepository = tradeMeasurementRepository,
            detectAnalyzer = tradeMeasurementDetectAnalyzer,
            measurementType = "TRADE",
            eventPublisher = eventPublisher,
        ),
        rateLimiter,
        objectMapper,
    ) {
    override fun consume(message: BinaryMessage): TradeMeasurement {
        val jsonString = Utils.Byte.toUtf8String(message.payload)
        val response = objectMapper.readValue(jsonString, UpbitTradeWebSocketResponse::class.java)

        return TradeMeasurement(
            code = response.code,
            tradePrice = response.tradePrice,
            tradeVolume = response.tradeVolume,
            askBid = response.askBid,
            time = Instant.ofEpochMilli(response.tradeTimestamp),
        )
    }

    override fun codes(): Collection<String> = COIN_CODE

    override fun requestType(): String = WebSocketTopicType.TRADE.code
}
