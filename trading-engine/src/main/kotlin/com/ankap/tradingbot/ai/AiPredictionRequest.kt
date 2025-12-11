package com.ankap.tradingbot.ai

import com.ankap.tradingbot.common.Candle
import com.ankap.tradingbot.common.PositionState

data class AiPredictionRequest(
    val symbol: String,
    val timestamp: Long,
    val lastPrice: Double,
    val candles: List<Candle>,
    val position: PositionState
    // later you can add indicators, features, etc.
)
