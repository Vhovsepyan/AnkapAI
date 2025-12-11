package com.ankap.tradingbot.trade

import com.ankap.tradingbot.common.OrderResult
import com.ankap.tradingbot.common.OrderStatus
import com.ankap.tradingbot.common.PositionSide
import com.ankap.tradingbot.common.Side
import com.ankap.tradingbot.portfolio.PositionService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.min

@Service
class TradeHistoryServiceImpl(
    private val tradeRepository: TradeRepository,
    private val positionService: PositionService
) : TradeHistoryService {

    private val logger = LoggerFactory.getLogger(TradeHistoryServiceImpl::class.java)

    override fun recordTrade(result: OrderResult) {
        val symbol = result.symbol
        val positionBefore = positionService.getPosition(symbol)

        val realizedPnl =
            if (result.status == OrderStatus.FILLED && result.side == Side.SELL) {
                val sellPrice = result.avgPrice
                val entryPrice = positionBefore.avgPrice
                if (sellPrice != null && entryPrice != null && positionBefore.side == PositionSide.LONG) {
                    val closingQty = min(result.executedQty, positionBefore.quantity)
                    (sellPrice - entryPrice) * closingQty
                } else null
            } else null

        val entity = TradeEntity(
            orderId = result.orderId,
            symbol = result.symbol,
            side = result.side,
            status = result.status,
            executedQty = result.executedQty,
            avgPrice = result.avgPrice,
            realizedPnl = realizedPnl
        )

        logger.debug("Saving trade to DB: {}", entity)
        tradeRepository.save(entity)
        logger.info("Recorded trade in history: {}", entity)
    }

    override fun getRecentTrades(limit: Int): List<TradeEntity> {
        val list = tradeRepository.findTop50ByOrderByTimestampDesc()
        return if (limit >= list.size) list else list.take(limit)
    }
}
