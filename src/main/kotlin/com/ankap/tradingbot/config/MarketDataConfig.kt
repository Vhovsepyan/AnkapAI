package com.ankap.tradingbot.config

import com.ankap.tradingbot.market.BinanceMarketDataService
import com.ankap.tradingbot.market.FakeMarketDataService
import com.ankap.tradingbot.market.MarketDataService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MarketDataConfig(
    @Value("\${bot.fake-market.enabled:true}")
    private val fakeMarketEnabled: Boolean
) {

    private val logger = LoggerFactory.getLogger(MarketDataConfig::class.java)

    @Bean
    fun marketDataService(
        fakeMarketDataService: FakeMarketDataService,
        binanceMarketDataService: BinanceMarketDataService
    ): MarketDataService {
        return if (fakeMarketEnabled) {
            logger.info("Using FakeMarketDataService (bot.fake-market.enabled=true)")
            fakeMarketDataService
        } else {
            logger.info("Using BinanceMarketDataService (bot.fake-market.enabled=false)")
            binanceMarketDataService
        }
    }
}
