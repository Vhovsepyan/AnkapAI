package com.ankap.tradingbot.risk

import com.ankap.tradingbot.common.*
import com.ankap.tradingbot.portfolio.PositionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class BasicRiskService(

    @Value("\${bot.risk.maxPositionUsd:100.0}")
    private val maxPositionSizeUsd: Double,

    @Value("\${bot.risk.defaultTradeUsd:10.0}")
    private val defaultTradeUsd: Double,

    @Value("\${bot.risk.maxDailyLossUsd:50.0}")
    private val maxDailyLossUsd: Double,

    private val positionService: PositionService

) : RiskService {

    private val logger = LoggerFactory.getLogger(BasicRiskService::class.java)

    override fun evaluate(signal: TradeSignal, snapshot: MarketSnapshot): RiskDecision {
        if (signal.action == Action.HOLD) {
            return RiskDecision(
                allowed = false,
                reason = "Signal is HOLD"
            )
        }

        // 1) Daily loss guard
        val dailyPnl = positionService.getDailyRealizedPnl()
        if (dailyPnl <= -maxDailyLossUsd) {
            logger.warn(
                "Max daily loss reached (dailyPnl={}, maxLoss={}), blocking new trades",
                dailyPnl, maxDailyLossUsd
            )
            return RiskDecision(
                allowed = false,
                reason = "Max daily loss reached"
            )
        }

        // 2) Basic notional check
        val targetNotional = defaultTradeUsd
        if (targetNotional > maxPositionSizeUsd) {
            return RiskDecision(
                allowed = false,
                reason = "Target notional exceeds max position size"
            )
        }

        val position = positionService.getPosition(snapshot.symbol)

        if (signal.action == Action.SELL && position.quantity <= 0.0) {
            return RiskDecision(false, reason = "No position to close")
        }

        val side = when (signal.action) {
            Action.BUY -> Side.BUY
            Action.SELL -> Side.SELL
            else -> {
                logger.warn("Unsupported action: {}", signal.action)
                return RiskDecision(false, reason = "Unsupported action")
            }
        }

        val qty = when (signal.action) {
            Action.BUY -> targetNotional / snapshot.lastPrice
            Action.SELL -> position.quantity   // close entire position
            else -> 0.0
        }

        if (qty <= 0.0) {
            return RiskDecision(
                allowed = false,
                reason = "Invalid quantity: $qty"
            )
        }

        val order = OrderRequest(
            symbol = snapshot.symbol,
            side = side,
            quantity = qty,
            type = OrderType.MARKET,
            price = snapshot.lastPrice
        )

        return RiskDecision(
            allowed = true,
            orderRequest = order
        )

    }
}
