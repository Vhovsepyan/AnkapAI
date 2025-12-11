package com.ankap.tradingbot.strategy

import com.ankap.tradingbot.ai.AiClient
import com.ankap.tradingbot.ai.AiPredictionRequest
import com.ankap.tradingbot.common.Action
import com.ankap.tradingbot.common.MarketSnapshot
import com.ankap.tradingbot.common.TradeSignal
import com.ankap.tradingbot.portfolio.PositionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AiStrategyService(
    private val aiClient: AiClient,
    private val positionService: PositionService,
    private val emaFallbackStrategy: EmaCrossoverStrategyService, // we will still reuse EMA
    @Value("\${bot.symbol:BTCUSDT}") private val symbol: String,
    @Value("\${ai.strategy.enabled:false}") private val aiEnabled: Boolean
) {

    private val logger = LoggerFactory.getLogger(AiStrategyService::class.java)

    /**
     * Helper method – NOT used by BotRunner directly yet.
     * BotRunner still uses EmaCrossoverStrategyService via StrategyService.
     */
    fun decide(snapshot: MarketSnapshot): TradeSignal {
        if (!aiEnabled) {
            // If AI is disabled, just delegate to EMA
            return emaFallbackStrategy.onMarketSnapshot(snapshot)
        }

        val position = positionService.getPosition(snapshot.symbol)

        val request = AiPredictionRequest(
            symbol = snapshot.symbol,
            timestamp = System.currentTimeMillis(),
            lastPrice = snapshot.lastPrice,
            candles = snapshot.candles,
            position = position
        )

        val response = aiClient.predictSync(request)

        if (response == null) {
            logger.warn("AI prediction failed or returned null; falling back to EMA strategy")
            return emaFallbackStrategy.onMarketSnapshot(snapshot)
        }

        val action = when (response.action.uppercase()) {
            "BUY" -> Action.BUY
            "SELL" -> Action.SELL
            else -> Action.HOLD
        }

        val signal = TradeSignal(
            symbol = snapshot.symbol,
            action = action,
            confidence = response.confidence
        )

        logger.debug(
            "AI strategy decision: action={}, confidence={}, extra={}",
            action, response.confidence, response.extraInfo
        )

        return signal
    }
}
