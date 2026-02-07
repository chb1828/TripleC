package io.github.triplec.analyzer.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 5. 18.
 */
@Configuration
@EnableScheduling
class SchedulerConfiguration {
    @Bean
    fun threadPoolTaskScheduler(): ThreadPoolTaskScheduler {
        val taskScheduler = ThreadPoolTaskScheduler()
        taskScheduler.poolSize = 5
        taskScheduler.setThreadNamePrefix("scheduler-")
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true)
        return taskScheduler
    }
}
