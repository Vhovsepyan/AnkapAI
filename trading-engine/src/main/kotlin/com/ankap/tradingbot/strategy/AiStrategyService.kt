package com.ankap.tradingbot.strategy

import com.ankap.tradingbot.ai.AiClient
import com.ankap.tradingbot.ai.AiPredictionRequest
import com.ankap.tradingbot.common.*
import com.ankap.tradingbot.portfolio.PositionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Service
@Primary
class AiStrategyService(
    private val aiClient: AiClient,
    private val positionService: PositionService,
    private val emaFallbackStrategy: EmaCrossoverStrategyService,
    @Value("\${ai.strategy.enabled:false}") private val aiEnabled: Boolean,
    @Value("\${ai.strategy.min-confidence:0.6}")
    private val minConfidence: Double
) : StrategyService {

    private val logger = LoggerFactory.getLogger(AiStrategyService::class.java)


    override fun onMarketSnapshot(snapshot: MarketSnapshot): TradeSignal {
        if (!aiEnabled) return emaFallbackStrategy.onMarketSnapshot(snapshot)

        val position = positionService.getPosition(snapshot.symbol)

        val req = AiPredictionRequest(
            symbol = snapshot.symbol,
            timestamp = System.currentTimeMillis(),
            lastPrice = snapshot.lastPrice,
            candles = snapshot.candles,
            position = position
        )

        val resp = aiClient.predictSync(req)
        if (resp == null) {
            logger.warn("AI returned null; fallback to EMA")
            return emaFallbackStrategy.onMarketSnapshot(snapshot)
        }

        val action = when (resp.action.uppercase()) {
            "BUY" -> Action.BUY
            "SELL" -> Action.SELL
            else -> Action.HOLD
        }

        if (action != Action.HOLD && resp.confidence < minConfidence) {
            logger.info("AI decision below min-confidence ({} < {}), forcing HOLD", resp.confidence, minConfidence)
            return TradeSignal(snapshot.symbol, Action.HOLD, resp.confidence)
        }

        logger.info(
            "AI decision: symbol={}, action={}, confidence={}, extra={}",
            snapshot.symbol, action, resp.confidence, resp.extraInfo
        )
        return TradeSignal(
            symbol = snapshot.symbol,
            action = action,
            confidence = resp.confidence
        )
    }
}
