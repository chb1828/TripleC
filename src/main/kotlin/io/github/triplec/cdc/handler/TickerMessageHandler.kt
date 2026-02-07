package io.github.triplec.cdc.handler

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.triplec.analyzer.service.TickerMeasurementAIDetectAnalyzer
import io.github.triplec.cdc.domain.dto.UpbitTickerWebSocketResponse
import io.github.triplec.cdc.repository.TickerCdcMeasurementRepository
import io.github.triplec.cdc.support.BufferedMeasurementWriter
import io.github.triplec.cdc.support.DataChannel
import io.github.triplec.cdc.support.SimpleTokenBucketRateLimiter
import io.github.triplec.common.domain.measurement.TickerMeasurement
import io.github.triplec.common.type.WebSocketTopicType
import io.github.triplec.common.util.Utils
import io.github.triplec.constant.UpbitConstant.COIN_CODE
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
class TickerMessageHandler(
    webSocketClient: WebSocketClient,
    rateLimiter: SimpleTokenBucketRateLimiter,
    tickerMeasurementRepository: TickerCdcMeasurementRepository,
    tickerMeasurementDetectAnalyzer: TickerMeasurementAIDetectAnalyzer,
    eventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
) : AbstractMessageHandler<TickerMeasurement>(
        webSocketClient,
        BufferedMeasurementWriter(
            dataChannel = DataChannel(1000),
            cdcMeasurementRepository = tickerMeasurementRepository,
            detectAnalyzer = tickerMeasurementDetectAnalyzer,
            measurementType = "TICKER",
            eventPublisher = eventPublisher,
        ),
        rateLimiter,
        objectMapper,
    ) {
    override fun consume(message: BinaryMessage): TickerMeasurement {
        val jsonString = Utils.Byte.toUtf8String(message.payload)
        val response = objectMapper.readValue(jsonString, UpbitTickerWebSocketResponse::class.java)

        return TickerMeasurement(
            code = response.code,
            tradePrice = response.tradePrice,
            tradeVolume = response.tradeVolume,
            tradeVolume24h = response.accTradeVolume24h,
            time = Instant.ofEpochMilli(response.tradeTimestamp),
        )
    }

    override fun codes(): Collection<String> = COIN_CODE

    override fun requestType(): String = WebSocketTopicType.TICKER.code
}
