package io.github.triplec.analyzer.domain

/**
 * 설명:
 *
 * @author 서버개발 / g-dev-server@dreamus.io
 */

data class LocalAICompletionResponse(
    val id: String? = null,
    val created: Long? = null,
    val model: String? = null,
    val choices: List<Choice>? = null,
    val usage: Usage? = null
) {
    data class Choice(
        val text: String? = null,
        val message: Message? = null,
        val index: Int? = null,
        val finish_reason: String? = null
    )

    data class Message(
        val role: String? = null,
        val content: String? = null
    )

    data class Usage(
        val prompt_tokens: Int? = null,
        val completion_tokens: Int? = null,
        val total_tokens: Int? = null
    )

    /**
     * 여러 조각으로 들어온 text를 합치고, markdown/태그/깨진 문자 등을 정리
     */
    fun cleanedText(): String {
        // 모든 text 조각 합치기
        val merged = choices
            ?.joinToString(" ") { it.text.orEmpty() }
            ?.trim()
            ?: ""

        return merged
            .replace(Regex("(?s)```.*?```"), "")   // ```python ... ``` 제거
            .replace(Regex("`"), "")               // 단일 백틱 제거
            .replace(Regex("<\\|.*?\\|>"), "")     // <|assistant|>, <|end|> 등 제거
            .replace(Regex("[^\\p{L}\\p{N}\\p{Zs}:%+\\-_.]"), "") // 깨진 문자 정리
            .replace(Regex("(?i)(ai|assistant|system|bot|data|ta)[:：\\s-]+"), "") // ai:, ta:, system: 등 접두사 제거
            .trim()
    }
}