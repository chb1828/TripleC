package io.github.triplec.cdc.handler

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.triplec.cdc.domain.dto.UpbitOrderbookWebSocketResponse
import io.github.triplec.cdc.repository.OrderbookCdcMeasurementRepository
import io.github.triplec.cdc.support.BufferedMeasurementWriter
import io.github.triplec.cdc.support.DataChannel
import io.github.triplec.cdc.support.SimpleTokenBucketRateLimiter
import io.github.triplec.common.domain.measurement.OrderbookMeasurement
import io.github.triplec.common.type.WebSocketTopicType
import io.github.triplec.common.util.Utils
import io.github.triplec.constant.UpbitConstant.COIN_CODE
import io.github.triplec.analyzer.service.OrderbookMeasurementAIDetectAnalyzer
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
class OrderbookMessageHandler(
    webSocketClient: WebSocketClient,
    rateLimiter: SimpleTokenBucketRateLimiter,
    orderbookMeasurementRepository: OrderbookCdcMeasurementRepository,
    orderbookMeasurementDetectAnalyzer: OrderbookMeasurementAIDetectAnalyzer,
    eventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
) : AbstractMessageHandler<OrderbookMeasurement>(
        webSocketClient,
        BufferedMeasurementWriter(
            dataChannel = DataChannel(1000),
            cdcMeasurementRepository = orderbookMeasurementRepository,
            detectAnalyzer = orderbookMeasurementDetectAnalyzer,
            measurementType = "ORDERBOOK",
            eventPublisher = eventPublisher,
        ),
        rateLimiter,
        objectMapper,
    ) {
    override fun consume(message: BinaryMessage): OrderbookMeasurement {
        val jsonString = Utils.Byte.toUtf8String(message.payload)
        val response = objectMapper.readValue(jsonString, UpbitOrderbookWebSocketResponse::class.java)

        val units = response.orderbookUnitResponses.take(5)

        val avgAskPriceTop5 = units.map { it.askPrice }.average()
        val avgBidPriceTop5 = units.map { it.bidPrice }.average()
        val sumAskSizeTop5 = units.sumOf { it.askSize }
        val sumBidSizeTop5 = units.sumOf { it.bidSize }

        val topUnit = units.firstOrNull()

        return OrderbookMeasurement(
            code = response.code,
            totalAskSize = response.totalAskSize,
            totalBidSize = response.totalBidSize,
            topAskPrice = topUnit?.askPrice ?: 0.0,
            topBidPrice = topUnit?.bidPrice ?: 0.0,
            topAskSize = topUnit?.askSize ?: 0.0,
            topBidSize = topUnit?.bidSize ?: 0.0,
            avgAskPriceTop5 = avgAskPriceTop5,
            avgBidPriceTop5 = avgBidPriceTop5,
            sumAskSizeTop5 = sumAskSizeTop5,
            sumBidSizeTop5 = sumBidSizeTop5,
            imbalanceRatio = if (response.totalAskSize == 0.0) 0.0 else response.totalBidSize / response.totalAskSize,
            time = Instant.ofEpochMilli(response.timestamp),
        )
    }

    override fun codes(): Collection<String> = COIN_CODE

    override fun requestType(): String = WebSocketTopicType.ORDERBOOK.code
}
