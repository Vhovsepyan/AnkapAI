package com.ankap.tradingbot.config

import com.binance.connector.client.SpotClient
import com.binance.connector.client.impl.SpotClientImpl
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BinanceConfig(
    @Value("\${binance.api.base-url}") private val baseUrl: String,
    @Value("\${binance.api.key:}") private val apiKey: String,
    @Value("\${binance.api.secret:}") private val secretKey: String,
) {
    private val logger = LoggerFactory.getLogger(BinanceConfig::class.java)

    @Bean
    @ConditionalOnProperty(name = ["binance.trade.spot.enabled"], havingValue = "true")
    fun spotClient(): SpotClient {
        return SpotClientImpl(apiKey, secretKey, baseUrl)
    }
}
