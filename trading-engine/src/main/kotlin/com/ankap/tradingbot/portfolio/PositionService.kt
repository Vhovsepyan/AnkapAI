package com.ankap.tradingbot.portfolio

import com.ankap.tradingbot.common.OrderResult
import com.ankap.tradingbot.common.PositionState

interface PositionService {

    fun getPosition(symbol: String): PositionState

    fun onOrderExecuted(result: OrderResult)

    /**
     * Realized PnL for the current day (e.g., in USDT).
     * Positive = profit, negative = loss.
     */
    fun getDailyRealizedPnl(): Double
}
