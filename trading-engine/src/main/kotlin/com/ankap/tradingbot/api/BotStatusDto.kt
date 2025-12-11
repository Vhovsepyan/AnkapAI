package com.ankap.tradingbot.api

import com.ankap.tradingbot.common.PositionState

data class BotStatusDto(
    val tradingActive: Boolean,
    val symbol: String,
    val position: PositionState,
    val dailyRealizedPnl: Double,
    val lastSignalAction: String?,
    val lastSignalTime: Long?,
    val lastTradeOrderId: String?,
    val lastTradeStatus: String?,
    val lastTradeSide: String?,
    val lastTradeQty: Double?,
    val lastTradePrice: Double?,
    val lastPrice: Double?,
    val unrealizedPnl: Double?
)
