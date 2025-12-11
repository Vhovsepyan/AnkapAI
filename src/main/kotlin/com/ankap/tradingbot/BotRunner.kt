package com.ankap.tradingbot

import com.ankap.tradingbot.common.*
import com.ankap.tradingbot.execution.ExecutionService
import com.ankap.tradingbot.market.MarketDataService
import com.ankap.tradingbot.portfolio.PositionService
import com.ankap.tradingbot.risk.RiskService
import com.ankap.tradingbot.strategy.StrategyService
import com.ankap.tradingbot.trade.TradeHistoryService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicBoolean

@Component
class BotRunner(
    private val marketDataService: MarketDataService,
    private val strategyService: StrategyService,
    private val riskService: RiskService,
    private val executionService: ExecutionService,
    private val positionService: PositionService,
    private val tradeHistoryService: TradeHistoryService,
    @Value("\${bot.symbol:BTCUSDT}")
    private val symbol: String
) {

    private val logger = LoggerFactory.getLogger(BotRunner::class.java)

    private val tradingActive = AtomicBoolean(false)

    @Volatile
    private var lastSignal: TradeSignal? = null

    @Volatile
    private var lastTrade: OrderResult? = null

    @Volatile
    private var lastSnapshot: MarketSnapshot? = null

    fun start() {
        if (tradingActive.compareAndSet(false, true)) {
            logger.info("Starting trading bot with symbol {}", symbol)
            marketDataService.registerListener { snapshot ->
                if (tradingActive.get()) {
                    handleSnapshot(snapshot)
                }
            }
            marketDataService.start()
        } else {
            logger.info("Bot is already running")
        }
    }

    fun stop() {
        if (tradingActive.compareAndSet(true, false)) {
            logger.info("Stopping trading bot (tradingActive=false)")
            marketDataService.stop()
        } else {
            logger.info("Bot is already stopped")
        }
    }

    fun isActive(): Boolean = tradingActive.get()

    fun getStatusDto(): com.ankap.tradingbot.api.BotStatusDto {
        val position = positionService.getPosition(symbol)
        val dailyPnl = positionService.getDailyRealizedPnl()
        val signal = lastSignal
        val trade = lastTrade
        val snapshot = lastSnapshot

        val lastPrice = snapshot?.lastPrice

        // Unrealized PnL = (lastPrice - entryPrice) * quantity, if long
        val unrealizedPnl = if (
            lastPrice != null &&
            position.side == PositionSide.LONG &&
            position.avgPrice != null &&
            position.quantity > 0.0
        ) {
            (lastPrice - position.avgPrice!!) * position.quantity
        } else {
            null
        }

        return com.ankap.tradingbot.api.BotStatusDto(
            tradingActive = isActive(),
            symbol = symbol,
            position = position,
            dailyRealizedPnl = dailyPnl,
            lastSignalAction = signal?.action?.name,
            lastSignalTime = signal?.timestamp,
            lastTradeOrderId = trade?.orderId,
            lastTradeStatus = trade?.status?.name,
            lastTradeSide = trade?.side?.name,
            lastTradeQty = trade?.executedQty,
            lastTradePrice = trade?.avgPrice,

            lastPrice = lastPrice,
            unrealizedPnl = unrealizedPnl
        )
    }


    private fun handleSnapshot(snapshot: MarketSnapshot) {
        lastSnapshot = snapshot
        logger.debug("Received snapshot: {}", snapshot.lastPrice)

        val signal = strategyService.onMarketSnapshot(snapshot)
        lastSignal = signal

        val position = positionService.getPosition(snapshot.symbol)
        logger.debug("Current position for {}: {}", snapshot.symbol, position)

        // Single LONG logic
        val shouldTrade = when (signal.action) {
            Action.BUY -> position.side == PositionSide.NONE
            Action.SELL -> position.side == PositionSide.LONG
            Action.HOLD -> false
        }

        if (!shouldTrade) {
            logger.debug("Ignoring signal={} due to position={}", signal.action, position.side)
            return
        }

        val decision = riskService.evaluate(signal, snapshot)
        if (!decision.allowed || decision.orderRequest == null) {
            logger.debug("Risk rejected trade or no trade: {}", decision.reason)
            return
        }

        val result = executionService.execute(decision.orderRequest)
        logger.info("Trade executed: {}", result)

        // record trade before we update position (to compute PnL using old position)
        tradeHistoryService.recordTrade(result)

        positionService.onOrderExecuted(result)
        lastTrade = result
    }
}
