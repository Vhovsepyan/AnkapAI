package com.ankap.tradingbot.ai

data class AiPredictionResponse(
    val action: String,        // BUY / SELL / HOLD
    val confidence: Double,    // 0.0 - 1.0
    val extraInfo: String? = null
)
