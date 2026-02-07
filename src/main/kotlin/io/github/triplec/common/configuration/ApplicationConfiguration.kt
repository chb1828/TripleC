package io.github.triplec.common.configuration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.blackbird.BlackbirdModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 16.
 */
@Configuration
class ApplicationConfiguration {
    @Bean
    @Primary
    fun objectMapper(jackson2ObjectMapperBuilder: Jackson2ObjectMapperBuilder): ObjectMapper =
        jackson2ObjectMapperBuilder
            .featuresToEnable(
                DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES,
            ).modules(BlackbirdModule())
            .modulesToInstall(
                KotlinModule
                    .Builder()
                    .configure(KotlinFeature.StrictNullChecks, true)
                    .build(),
            ).build()
}
