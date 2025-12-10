package com.ankap.tradingbot.market

import com.ankap.tradingbot.common.LotSizeFilter
import com.ankap.tradingbot.common.MinNotionalFilter
import com.ankap.tradingbot.common.SymbolInfo
import com.binance.connector.client.SpotClient
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap

@Service
class SymbolInfoService(
    private val spotClient: SpotClient
) {

    private val logger = LoggerFactory.getLogger(SymbolInfoService::class.java)
    private val objectMapper = ObjectMapper()

    private val cache = ConcurrentHashMap<String, SymbolInfo>()

    fun getSymbolInfo(symbol: String): SymbolInfo? {
        return cache[symbol] ?: fetchAndCacheSymbolInfo(symbol)
    }

    private fun fetchAndCacheSymbolInfo(symbol: String): SymbolInfo? {
        logger.info("Loading exchangeInfo for symbol={}", symbol)

        val marketClient = spotClient.createMarket()
        val json = marketClient.exchangeInfo(mapOf("symbol" to symbol))
        val root = objectMapper.readTree(json)

        val symbolsNode = root["symbols"] ?: return null
        if (!symbolsNode.isArray || symbolsNode.size() == 0) {
            logger.warn("No symbol info returned for {}", symbol)
            return null
        }

        val sym = symbolsNode[0]
        val filters = sym["filters"]

        var lotSizeFilter: LotSizeFilter? = null
        var minNotionalFilter: MinNotionalFilter? = null

        filters?.forEach { f ->
            when (f["filterType"].asText()) {
                "LOT_SIZE" -> {
                    lotSizeFilter = LotSizeFilter(
                        minQty = BigDecimal(f["minQty"].asText()),
                        maxQty = BigDecimal(f["maxQty"].asText()),
                        stepSize = BigDecimal(f["stepSize"].asText())
                    )
                }
                "MIN_NOTIONAL" -> {
                    minNotionalFilter = MinNotionalFilter(
                        minNotional = BigDecimal(f["minNotional"].asText())
                    )
                }
            }
        }

        val info = SymbolInfo(
            symbol = symbol,
            lotSize = lotSizeFilter,
            minNotional = minNotionalFilter
        )

        cache[symbol] = info
        logger.info("Cached SymbolInfo for {}: {}", symbol, info)
        return info
    }
}
