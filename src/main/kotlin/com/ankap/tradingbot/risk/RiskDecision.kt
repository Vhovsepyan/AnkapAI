package com.ankap.tradingbot.risk

import com.ankap.tradingbot.common.MarketSnapshot
import com.ankap.tradingbot.common.OrderRequest
import com.ankap.tradingbot.common.TradeSignal

data class RiskDecision(
    val allowed: Boolean,
    val orderRequest: OrderRequest? = null,
    val reason: String? = null
)

interface RiskService {
    fun evaluate(signal: TradeSignal, snapshot: MarketSnapshot): RiskDecision
}
