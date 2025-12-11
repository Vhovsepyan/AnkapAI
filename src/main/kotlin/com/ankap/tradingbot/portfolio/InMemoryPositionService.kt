package com.ankap.tradingbot.portfolio

import com.ankap.tradingbot.common.OrderResult
import com.ankap.tradingbot.common.OrderStatus
import com.ankap.tradingbot.common.PositionSide
import com.ankap.tradingbot.common.PositionState
import com.ankap.tradingbot.common.Side
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

@Service
class InMemoryPositionService(
    @Value("\${bot.symbol:BTCUSDT}")
    private val defaultSymbol: String
) : PositionService {

    private val logger = LoggerFactory.getLogger(InMemoryPositionService::class.java)
    private val positions = ConcurrentHashMap<String, PositionState>()

    // Daily PnL tracking
    @Volatile
    private var dailyRealizedPnl: Double = 0.0

    @Volatile
    private var currentDay: LocalDate = LocalDate.now()

    @Value("\${bot.debug.force-daily-loss:false}")
    private val forceDailyLoss: Boolean = false

    override fun getPosition(symbol: String): PositionState {
        return positions[symbol] ?: PositionState(
            symbol = symbol,
            side = PositionSide.NONE,
            quantity = 0.0,
            avgPrice = null
        )
    }

    override fun getDailyRealizedPnl(): Double {
        maybeResetDay()
        return dailyRealizedPnl
    }

    override fun onOrderExecuted(result: OrderResult) {
        if (result.status != OrderStatus.FILLED) {
            logger.debug("Order {} not filled, ignoring for position tracking", result.orderId)
            return
        }

        maybeResetDay()

        val symbol = result.symbol
        val current = getPosition(symbol)

        when (result.side) {
            Side.BUY -> handleBuy(current, result)
            Side.SELL -> handleSell(current, result)
        }
    }

    private fun handleBuy(current: PositionState, result: OrderResult) {
        val executedQty = result.executedQty
        val price = result.avgPrice ?: return

        val newState = if (current.side == PositionSide.NONE || current.quantity <= 0.0) {
            // opening new long
            PositionState(
                symbol = current.symbol,
                side = PositionSide.LONG,
                quantity = executedQty,
                avgPrice = price
            )
        } else {
            // adding to existing long → recalc avg price
            val totalQty = current.quantity + executedQty
            val totalCost = (current.avgPrice ?: price) * current.quantity + price * executedQty
            val newAvgPrice = totalCost / totalQty

            PositionState(
                symbol = current.symbol,
                side = PositionSide.LONG,
                quantity = totalQty,
                avgPrice = newAvgPrice
            )
        }

        logger.info("Updated position after BUY: {}", newState)
        positions[current.symbol] = newState
    }

    private fun handleSell(current: PositionState, result: OrderResult) {
        val executedQty = result.executedQty
        val sellPrice = result.avgPrice ?: return

        if (current.side == PositionSide.NONE || current.quantity <= 0.0) {
            logger.info("SELL executed while flat, ignoring for position state")
            return
        }

        val closingQty = minOf(executedQty, current.quantity)
        val entryPrice = current.avgPrice ?: sellPrice

        // Realized PnL for this close
        val pnl = (sellPrice - entryPrice) * closingQty
        dailyRealizedPnl += pnl
        logger.info("Realized PnL on SELL: {} (dailyRealizedPnl={})", pnl, dailyRealizedPnl)

        val remaining = current.quantity - executedQty

        val newState = if (remaining <= 0.0 || abs(remaining) < 1e-9) {
            PositionState(
                symbol = current.symbol,
                side = PositionSide.NONE,
                quantity = 0.0,
                avgPrice = null
            )
        } else {
            // partial close (we keep same avgPrice for remaining)
            PositionState(
                symbol = current.symbol,
                side = PositionSide.LONG,
                quantity = remaining,
                avgPrice = current.avgPrice
            )
        }

        logger.info("Updated position after SELL: {}", newState)
        positions[current.symbol] = newState
    }

    private fun maybeResetDay() {
        val today = LocalDate.now()
        if (today != currentDay) {
            logger.info(
                "Date changed from {} to {}, resetting dailyRealizedPnl (was {})",
                currentDay, today, dailyRealizedPnl
            )
            currentDay = today
            dailyRealizedPnl = 0.0
        }
//        // DEV TEST HOOK: force negative PnL if enabled
//        if (forceDailyLoss) {
//            if (dailyRealizedPnl > -5.0) {
//                dailyRealizedPnl = -5.0
//                logger.warn("DEV: forcing dailyRealizedPnl to {}", dailyRealizedPnl)
//            }
//        }
    }
}
