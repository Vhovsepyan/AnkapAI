package com.ankap.tradingbot.config

import com.ankap.tradingbot.execution.BinanceExecutionService
import com.ankap.tradingbot.execution.ExecutionService
import com.ankap.tradingbot.execution.LoggingExecutionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ExecutionConfig(
    @Value("\${binance.trade.spot.enabled:false}")
    private val spotEnabled: Boolean
) {

    private val logger = LoggerFactory.getLogger(ExecutionConfig::class.java)

    @Bean
    fun executionService(
        loggingExecutionService: LoggingExecutionService,
        binanceExecutionService: BinanceExecutionService
    ): ExecutionService {
        return if (spotEnabled) {
            logger.info("Execution mode: Binance Spot TESTNET (binance.trade.spot.enabled=true)")
            binanceExecutionService
        } else {
            logger.info("Execution mode: LOGGING ONLY (binance.trade.spot.enabled=false)")
            loggingExecutionService
        }
    }
}
