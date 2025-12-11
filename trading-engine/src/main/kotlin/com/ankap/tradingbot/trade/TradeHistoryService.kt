package com.ankap.tradingbot.trade

import com.ankap.tradingbot.common.OrderResult

interface TradeHistoryService {
    fun recordTrade(result: OrderResult)
    fun getRecentTrades(limit: Int = 50): List<TradeEntity>
}
