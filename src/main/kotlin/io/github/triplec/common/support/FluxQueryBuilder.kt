package io.github.triplec.common.support

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 5. 18.
 */
class FluxQueryBuilder {
    private val stringBuilder = StringBuilder()

    fun from(bucket: String): FluxQueryBuilder {
        stringBuilder.append("""from(bucket: "$bucket")""").append("\n")
        return this
    }

    fun range(duration: String): FluxQueryBuilder {
        stringBuilder.append("""  |> range(start: $duration)""").append("\n")
        return this
    }

    fun filter(
        field: String,
        value: String,
    ): FluxQueryBuilder {
        stringBuilder.append("""  |> filter(fn: (r) => r["$field"] == "$value")""").append("\n")
        return this
    }

    fun sort(column: String): FluxQueryBuilder {
        stringBuilder.append("""  |> sort(columns: ["$column"])""").append("\n")
        return this
    }

    fun reduce(
        idVar: String,
        lastVar: String,
        valueRef: String = "r._value",
    ): FluxQueryBuilder {
        stringBuilder
            .append(
                """
                |> reduce(
                    identity: {$idVar: 0.0, $lastVar: 0.0},
                    fn: (r, acc) => ({
                        $idVar: if acc.$idVar == 0.0 then $valueRef else acc.$idVar,
                        $lastVar: $valueRef
                    })
                )
                """.trimIndent(),
            ).append("\n")
        return this
    }

    fun map(
        resultField: String,
        expr: String,
    ): FluxQueryBuilder {
        stringBuilder.append("""  |> map(fn: (r) => ({ $resultField: $expr }))""").append("\n")
        return this
    }

    fun build(): String = stringBuilder.toString().trim()
}
