package com.ankap.tradingbot.common

import java.math.BigDecimal

data class LotSizeFilter(
    val minQty: BigDecimal,
    val maxQty: BigDecimal,
    val stepSize: BigDecimal
)

data class MinNotionalFilter(
    val minNotional: BigDecimal
)

data class SymbolInfo(
    val symbol: String,
    val lotSize: LotSizeFilter?,
    val minNotional: MinNotionalFilter?
)
