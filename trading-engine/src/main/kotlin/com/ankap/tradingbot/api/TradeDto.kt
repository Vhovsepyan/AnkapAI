package com.ankap.tradingbot.api

import com.ankap.tradingbot.trade.TradeEntity

data class TradeDto(
    val id: Long?,
    val orderId: String,
    val symbol: String,
    val side: String,
    val status: String,
    val executedQty: Double,
    val avgPrice: Double?,
    val realizedPnl: Double?,
    val timestamp: Long        // epoch millis
)

fun TradeEntity.toDto() = TradeDto(
    id = this.id,
    orderId = this.orderId,
    symbol = this.symbol,
    side = this.side.name,
    status = this.status.name,
    executedQty = this.executedQty,
    avgPrice = this.avgPrice,
    realizedPnl = this.realizedPnl,
    timestamp = this.timestamp.toEpochMilli()
)
