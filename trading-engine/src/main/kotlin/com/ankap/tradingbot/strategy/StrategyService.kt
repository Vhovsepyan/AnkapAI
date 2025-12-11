package com.ankap.tradingbot.strategy

import com.ankap.tradingbot.common.MarketSnapshot
import com.ankap.tradingbot.common.TradeSignal

interface StrategyService {
    fun onMarketSnapshot(snapshot: MarketSnapshot): TradeSignal
}
