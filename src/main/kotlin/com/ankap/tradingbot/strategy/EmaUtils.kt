package com.ankap.tradingbot.strategy

object EmaUtils {
    /**
     * Returns list of EMA values for the given period.
     * For first (period) elements, it uses SMA as seed.
     */
    fun ema(values: List<Double>, period: Int): List<Double> {
        require(period > 0) { "EMA period must be positive" }
        if (values.size < period) return emptyList()

        val k = 2.0 / (period + 1)
        val result = MutableList(values.size) { 0.0 }

        // seed with SMA
        var sma = 0.0
        for (i in 0 until period) {
            sma += values[i]
        }
        sma /= period
        result[period - 1] = sma

        // EMA from period onward
        for (i in period until values.size) {
            result[i] = values[i] * k + result[i - 1] * (1 - k)
        }

        // leading elements (before period-1) stay 0 – we will ignore them
        return result
    }
}
