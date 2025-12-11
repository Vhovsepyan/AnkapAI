package com.ankap.tradingbot.execution

import com.ankap.tradingbot.common.OrderRequest
import com.ankap.tradingbot.common.OrderResult
import com.ankap.tradingbot.common.OrderStatus
import com.ankap.tradingbot.common.Side
import com.ankap.tradingbot.market.SymbolInfoService
import com.binance.connector.client.SpotClient
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.LinkedHashMap

@Service
class BinanceExecutionService(
    private val spotClient: SpotClient,
    private val symbolInfoService: SymbolInfoService
) : ExecutionService {

    private val logger = LoggerFactory.getLogger(BinanceExecutionService::class.java)
    private val objectMapper = ObjectMapper()

    override fun execute(order: OrderRequest): OrderResult {
        logger.info("Sending REAL TESTNET order to Binance: {}", order)

        val symbolInfo = symbolInfoService.getSymbolInfo(order.symbol)
        val adjustedQty = adjustQuantity(order.quantity, order.symbol, symbolInfo)

        if (adjustedQty == null) {
            logger.warn("Quantity too small for symbol {}. Skipping order.", order.symbol)
            return OrderResult(
                orderId = "SKIPPED_TOO_SMALL",
                symbol = order.symbol,
                side = order.side,
                status = OrderStatus.REJECTED,
                executedQty = 0.0,
                avgPrice = null
            )
        }

        val qtyStr = adjustedQty.toPlainString()

        val params = LinkedHashMap<String, Any>().apply {
            put("symbol", order.symbol)
            put("side", order.side.name)   // BUY / SELL
            put("type", order.type.name)   // MARKET for now
            put("quantity", qtyStr)
        }

        val tradeClient = spotClient.createTrade()
        val responseJson = tradeClient.newOrder(params)

        logger.info("Binance newOrder response: {}", responseJson)

        val node = objectMapper.readTree(responseJson)

        val orderId = node["orderId"]?.asText() ?: "UNKNOWN"
        val executedQty = node["executedQty"]?.asDouble() ?: adjustedQty.toDouble()
        val statusStr = node["status"]?.asText() ?: "NEW"

        val status = try {
            OrderStatus.valueOf(statusStr)
        } catch (ex: IllegalArgumentException) {
            logger.warn("Unknown order status from Binance: {}", statusStr)
            OrderStatus.NEW
        }

        val sideFromResponseText = node["side"]?.asText()
        val side = try {
            if (sideFromResponseText != null) {
                Side.valueOf(sideFromResponseText)
            } else {
                order.side
            }
        } catch (ex: IllegalArgumentException) {
            logger.warn("Unknown side from Binance response: {}, using request side {}", sideFromResponseText, order.side)
            order.side
        }

        val avgPrice =
            node["avgPrice"]?.asDouble()
                ?: node["fills"]?.get(0)?.get("price")?.asDouble()

        return OrderResult(
            orderId = orderId,
            symbol = order.symbol,
            side = side,
            status = status,
            executedQty = executedQty,
            avgPrice = avgPrice
        )
    }

    /**
     * Adjusts quantity to match LOT_SIZE rules.
     * Returns null if quantity is too small and trade should be skipped.
     */
    private fun adjustQuantity(
        originalQty: Double,
        symbol: String,
        symbolInfo: com.ankap.tradingbot.common.SymbolInfo?
    ): BigDecimal? {
        var qty = BigDecimal(originalQty)

        val lot = symbolInfo?.lotSize

        if (lot != null) {
            // snap down to stepSize grid
            if (lot.stepSize > BigDecimal.ZERO) {
                val steps = qty.divide(lot.stepSize, 0, RoundingMode.DOWN)
                qty = steps.multiply(lot.stepSize)
            }

            // Check min/max qty
            if (qty < lot.minQty) {
                return null
            }
            if (qty > lot.maxQty) {
                qty = lot.maxQty
            }
        }

        // Just to be safe, cut to 8 decimals for BTC-like assets.
        qty = qty.setScale(8, RoundingMode.DOWN).stripTrailingZeros()

        if (qty <= BigDecimal.ZERO) {
            return null
        }

        return qty
    }
}
