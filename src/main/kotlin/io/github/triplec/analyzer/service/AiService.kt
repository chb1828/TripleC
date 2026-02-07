package io.github.triplec.analyzer.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.triplec.analyzer.domain.LocalAICompletionResponse
import io.github.triplec.analyzer.domain.SpikeDirection
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.concurrent.Semaphore

/**
 * 설명:
 *
 * @author 서버개발 / hb.choi@sk.com
 */

@Service
class LocalAIService {

    private val log = KotlinLogging.logger {}

    private val semaphore = Semaphore(1)

    private val restTemplate = RestTemplate(
        org.springframework.http.client.SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(5000) // 연결 타임아웃 5초
            setReadTimeout(120000)   // 읽기 타임아웃 120초 (모델 추론 시간 고려)
        }
    )

    fun askDirection(prompt: String): SpikeDirection {
        // 세마포어 획득 시도 (즉시 획득 못하면 건너뛰거나 대기)
        // 여기서는 대기하지 않고 즉시 리턴하거나, 일정 시간만 대기하도록 설정 가능
        // 요구사항: "호출만 하면 응답 안주고 멈추는데 고쳐줄래" -> 대기보다는 순차 처리 보장이 중요
        
        val acquired = semaphore.tryAcquire(3, java.util.concurrent.TimeUnit.SECONDS)
        if (!acquired) {
            log.warn { "⚠️ LocalAI is busy. Skipping request." }
            return SpikeDirection.UNCHANGED
        }

        try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON

            val body = mapOf(
                "model" to "qwen3",
                "messages" to listOf(
                    mapOf("role" to "user", "content" to "$prompt\n\nAnswer with only 'spike', 'drop', or 'none'.")
                ),
                "temperature" to 0.1
            )

            val entity = HttpEntity(body, headers)

            val response = restTemplate.postForObject(
                "http://localhost:8081/v1/chat/completions",
                entity,
                LocalAICompletionResponse::class.java
            )

            val raw = response?.choices?.firstOrNull()?.message?.content ?: ""
            val cleaned = raw.trim().lowercase()

            log.info { "AI 호출 결과 : $cleaned" }

            return when {
                cleaned.contains("spike") || cleaned.contains("surge") || cleaned.contains("up") -> SpikeDirection.UP
                cleaned.contains("drop") || cleaned.contains("crash") || cleaned.contains("down") -> SpikeDirection.DOWN
                else -> SpikeDirection.UNCHANGED
            }
        } catch (e: Exception) {
            log.error { "❌ AI Request Failed: ${e.message}" }
            return SpikeDirection.UNCHANGED
        } finally {
            semaphore.release()
        }
    }
}