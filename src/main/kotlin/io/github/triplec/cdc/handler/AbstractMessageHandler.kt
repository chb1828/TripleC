package io.github.triplec.cdc.handler

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.triplec.cdc.domain.dto.UpbitWebSocketRequestParam
import io.github.triplec.cdc.support.BufferedMeasurementWriter
import io.github.triplec.cdc.support.SimpleTokenBucketRateLimiter
import io.github.triplec.common.domain.measurement.BaseMeasurement
import io.github.triplec.constant.UpbitConstant
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import java.util.concurrent.Future

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 17.
 */
private val log = KotlinLogging.logger {}

abstract class AbstractMessageHandler<T : BaseMeasurement>(
    private val webSocketClient: WebSocketClient,
    private val bufferedMeasurementWriter: BufferedMeasurementWriter<T>,
    private val rateLimiter: SimpleTokenBucketRateLimiter,
    private val objectMapper: ObjectMapper,
) : AbstractWebSocketHandler() {
    private val connections = mutableListOf<UpbitSocketSession>()

    abstract fun consume(message: BinaryMessage): T

    abstract fun codes(): Collection<String>

    abstract fun requestType(): String

    override fun handleBinaryMessage(
        session: WebSocketSession,
        message: BinaryMessage,
    ) {
        val measurement = consume(message)
        bufferedMeasurementWriter.write(measurement)
    }

    @EventListener(ApplicationReadyEvent::class)
    fun startWebSocketConnections() {
        codes()
            .chunked(2)
            .forEach {
                rateLimiter.acquire()
                log.info { "(${requestType()}) WebSocket 연결 시도: $it" }

                val future = webSocketClient.execute(this, UpbitConstant.SOCKET_URL)
                connections += UpbitSocketSession(it, future)

                CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    runCatching {
                        val session = future.get()
                        val requestParam = UpbitWebSocketRequestParam(type = requestType(), codes = it)
                        val payload = objectMapper.writeValueAsString(requestParam.toPayload())
                        session.sendMessage(TextMessage(payload))
                        log.info { "WebSocket 요청 전송 완료: $it" }
                    }.onFailure {
                        log.info { "WebSocket 요청 전송 실패 ($it): ${it.message}" }
                    }
                }
            }

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            bufferedMeasurementWriter.run()
        }
    }

    @PreDestroy
    fun destroy() {
        connections.forEach { (codes, future) ->
            runCatching {
                future.get().close()
                log.info { "WebSocket 세션 종료 완료: $codes" }
            }.onFailure {
                log.info { "WebSocket 세션 종료 실패 ($codes): ${it.message}" }
            }
        }
        bufferedMeasurementWriter.close()
    }

    data class UpbitSocketSession(
        val codes: List<String>,
        val future: Future<WebSocketSession>,
    )
}
