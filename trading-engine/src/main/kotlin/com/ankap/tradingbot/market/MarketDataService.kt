package com.ankap.tradingbot.market

import com.ankap.tradingbot.common.MarketSnapshot

interface MarketDataService {
    fun start()
    fun stop()
    fun registerListener(listener: (MarketSnapshot) -> Unit)
}
