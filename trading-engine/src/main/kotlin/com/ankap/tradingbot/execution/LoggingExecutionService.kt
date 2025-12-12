package com.ankap.tradingbot.execution

import com.ankap.tradingbot.common.OrderRequest
import com.ankap.tradingbot.common.OrderResult
import com.ankap.tradingbot.common.OrderStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LoggingExecutionService : ExecutionService {

    private val logger = LoggerFactory.getLogger(LoggingExecutionService::class.java)

    override fun execute(order: OrderRequest): OrderResult {
        logger.info("EXECUTE ORDER (LOGGING ONLY) -> {}", order)

        return OrderResult(
            orderId = UUID.randomUUID().toString(),
            symbol = order.symbol,
            side = order.side,
            status = OrderStatus.FILLED,
            executedQty = order.quantity,
            avgPrice = order.price
        )
    }
}

