package com.ankap.tradingbot.strategy

import com.ankap.tradingbot.common.Action
import com.ankap.tradingbot.common.MarketSnapshot
import com.ankap.tradingbot.common.TradeSignal
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class SimpleRandomStrategyService : StrategyService {

    private val logger = LoggerFactory.getLogger(SimpleRandomStrategyService::class.java)

    override fun onMarketSnapshot(snapshot: MarketSnapshot): TradeSignal {
        // random decision just to test the pipeline
        val r = Random.nextDouble()

        val action = when {
            r < 0.05 -> Action.BUY   // 5% probability
            r < 0.10 -> Action.SELL  // 5% probability
            else -> Action.HOLD
        }

        val confidence = when (action) {
            Action.BUY, Action.SELL -> 0.7
            Action.HOLD -> 0.0
        }

        val signal = TradeSignal(
            symbol = snapshot.symbol,
            action = action,
            confidence = confidence
        )

        logger.debug("Generated random signal: {}", signal)
        return signal
    }
}
