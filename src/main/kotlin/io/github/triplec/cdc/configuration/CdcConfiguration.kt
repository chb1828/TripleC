package io.github.triplec.cdc.configuration

import io.github.triplec.cdc.support.SimpleTokenBucketRateLimiter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.time.Duration

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 22.
 */
@Configuration
class CdcConfiguration {
    @Bean
    fun rateLimiter(): SimpleTokenBucketRateLimiter = SimpleTokenBucketRateLimiter.from(4, Duration.ofSeconds(3))

    @Bean
    fun webSocketClient(): WebSocketClient = StandardWebSocketClient()
}
