package com.ankap.tradingbot.strategy

import com.ankap.tradingbot.common.Action
import com.ankap.tradingbot.common.MarketSnapshot
import com.ankap.tradingbot.common.TradeSignal
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import kotlin.math.max

@Service
@Primary
class EmaCrossoverStrategyService(

    @Value("\${bot.strategy.ema.short:9}")
    private val shortPeriod: Int,

    @Value("\${bot.strategy.ema.long:21}")
    private val longPeriod: Int

) : StrategyService {

    private val logger = LoggerFactory.getLogger(EmaCrossoverStrategyService::class.java)

    override fun onMarketSnapshot(snapshot: MarketSnapshot): TradeSignal {
        val closes = snapshot.candles.map { it.close }
        val minRequired = max(shortPeriod, longPeriod) + 2

        if (closes.size < minRequired) {
            return TradeSignal(
                symbol = snapshot.symbol,
                action = Action.HOLD,
                confidence = 0.0
            )
        }

        val emaShort = EmaUtils.ema(closes, shortPeriod)
        val emaLong = EmaUtils.ema(closes, longPeriod)

        val lastIndex = closes.size - 1

        if (emaShort.size <= lastIndex || emaLong.size <= lastIndex) {
            return TradeSignal(
                symbol = snapshot.symbol,
                action = Action.HOLD,
                confidence = 0.0
            )
        }

        val shortPrev = emaShort[lastIndex - 1]
        val longPrev = emaLong[lastIndex - 1]
        val shortNow = emaShort[lastIndex]
        val longNow = emaLong[lastIndex]

        val action = when {
            // Bullish crossover: short crosses above long
            shortPrev <= longPrev && shortNow > longNow -> Action.BUY
            // Bearish crossover: short crosses below long
            shortPrev >= longPrev && shortNow < longNow -> Action.SELL
            else -> Action.HOLD
        }

        val confidence = when (action) {
            Action.BUY, Action.SELL -> 0.8
            Action.HOLD -> 0.0
        }

        val signal = TradeSignal(
            symbol = snapshot.symbol,
            action = action,
            confidence = confidence
        )

        logger.debug(
            "EMA signal for {}: action={}, shortNow={}, longNow={}",
            snapshot.symbol, action, shortNow, longNow
        )

        return signal
    }
}
