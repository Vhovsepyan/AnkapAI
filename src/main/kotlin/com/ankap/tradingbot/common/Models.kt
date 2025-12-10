package com.ankap.tradingbot.common

enum class Action { BUY, SELL, HOLD }

enum class Side { BUY, SELL }

enum class OrderType { MARKET, LIMIT }

enum class OrderStatus { NEW, PARTIALLY_FILLED, FILLED, CANCELED, REJECTED }

data class Candle(
    val symbol: String,
    val openTime: Long,
    val closeTime: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

data class Indicators(
    val rsi: Double? = null,
    val emaShort: Double? = null,
    val emaLong: Double? = null
    // extend later
)

data class MarketSnapshot(
    val symbol: String,
    val lastPrice: Double,
    val candles: List<Candle>,
    val indicators: Indicators
)

data class TradeSignal(
    val symbol: String,
    val action: Action,
    val confidence: Double,
    val timestamp: Long = System.currentTimeMillis()
)

data class OrderRequest(
    val symbol: String,
    val side: Side,
    val quantity: Double,
    val type: OrderType = OrderType.MARKET,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null
)

data class OrderResult(
    val orderId: String,
    val symbol: String,
    val side: Side,
    val status: OrderStatus,
    val executedQty: Double,
    val avgPrice: Double? = null
)

enum class PositionSide {
    NONE,
    LONG
}

data class PositionState(
    val symbol: String,
    val side: PositionSide,
    val quantity: Double,
    val avgPrice: Double?
)

