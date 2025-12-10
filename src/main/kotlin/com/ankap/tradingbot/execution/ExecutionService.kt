package com.ankap.tradingbot.execution

import com.ankap.tradingbot.common.OrderRequest
import com.ankap.tradingbot.common.OrderResult

interface ExecutionService {
    fun execute(order: OrderRequest): OrderResult
}
