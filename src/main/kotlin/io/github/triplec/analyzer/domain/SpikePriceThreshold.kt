package io.github.triplec.analyzer.domain

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 5. 18.
 */
data class SpikePriceThreshold(
    val priceRate: Double = 0.025,   // 가격 상승률
) {
    companion object {
        private val DEFAULT = SpikePriceThreshold()

        fun getDefault(): SpikePriceThreshold = DEFAULT
    }
}

object SpikeThresholds {

    private val thresholds: Map<String, SpikePriceThreshold> = mapOf(
        //"KRW-BTC"     to SpikePriceThreshold(0.010),
        "KRW-BTC"     to SpikePriceThreshold(0.00001),
        "KRW-ETH"     to SpikePriceThreshold(0.012),
        "KRW-XRP"     to SpikePriceThreshold(0.018),
        "KRW-SOL"     to SpikePriceThreshold(0.020),
        "KRW-STX"     to SpikePriceThreshold(0.030),
        "KRW-MLK"     to SpikePriceThreshold(0.050),
        "KRW-AERGO"   to SpikePriceThreshold(0.050),
        "KRW-TT"      to SpikePriceThreshold(0.060),
        "KRW-USDT"    to SpikePriceThreshold(0.100),
        "KRW-AUCTION" to SpikePriceThreshold(0.035),
        "KRW-ZRO"     to SpikePriceThreshold(0.040),
        "KRW-ZETA"    to SpikePriceThreshold(0.040),
        "KRW-NEO"     to SpikePriceThreshold(0.030),
        "KRW-VANA"    to SpikePriceThreshold(0.050)
    )

    fun from(code: String): SpikePriceThreshold =
        thresholds[code] ?: SpikePriceThreshold.getDefault()
}