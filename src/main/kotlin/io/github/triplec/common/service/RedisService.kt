package io.github.triplec.common.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 8. 24.
 */
@Service
class RedisService(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) {
    fun set(
        key: String,
        value: String,
        ttlDuration: Duration? = null,
    ) {
        if (ttlDuration == null) {
            redisTemplate.opsForValue().set(key, value)
        } else {
            redisTemplate.opsForValue().set(key, value, ttlDuration)
        }
    }

    fun get(key: String): String? = redisTemplate.opsForValue().get(key)

    fun exist(key: String) = get(key) != null

    fun delete(key: String) = redisTemplate.delete(key)

    fun <T : Any> setObject(key: String, value: T, ttl: Duration? = null) {
        val json = objectMapper.writeValueAsString(value)
        set(key, json, ttl)
    }

    fun <T : Any> getObject(key: String, clazz: Class<T>): T? {
        val json = get(key) ?: return null
        return runCatching { objectMapper.readValue(json, clazz) }.getOrNull()
    }
}
