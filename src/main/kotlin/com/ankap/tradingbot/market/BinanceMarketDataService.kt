package com.ankap.tradingbot.market

import com.ankap.tradingbot.common.Candle
import com.ankap.tradingbot.common.Indicators
import com.ankap.tradingbot.common.MarketSnapshot
import com.binance.connector.client.SpotClient
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.LinkedHashMap
import kotlin.math.max
import kotlin.math.min

@Service
class BinanceMarketDataService(
    private val spotClient: SpotClient,
    @Value("\${bot.symbol:BTCUSDT}")
    private val symbol: String
) : MarketDataService {

    private val logger = LoggerFactory.getLogger(BinanceMarketDataService::class.java)

    private val listeners = CopyOnWriteArrayList<(MarketSnapshot) -> Unit>()
    private val running = AtomicBoolean(false)
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val objectMapper = ObjectMapper()

    private val candlesWindowSize = 200
    private val candles = ArrayDeque<Candle>()

    override fun start() {
        if (running.compareAndSet(false, true)) {
            logger.info("Starting BinanceMarketDataService for symbol={} (interval=1m)", symbol)
            executor.scheduleAtFixedRate(
                { fetchAndDispatch() },
                0,
                1,
                TimeUnit.SECONDS
            )
        }
    }

    override fun stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("Stopping BinanceMarketDataService...")
            executor.shutdownNow()
        }
    }

    override fun registerListener(listener: (MarketSnapshot) -> Unit) {
        listeners.add(listener)
    }

    private fun fetchAndDispatch() {
        if (!running.get()) return

        try {
            val marketClient = spotClient.createMarket()
            val params = LinkedHashMap<String, Any>().apply {
                put("symbol", symbol)
                put("interval", "1m")
                put("limit", candlesWindowSize)
            }

            val json = marketClient.klines(params)
            val arrayNode = objectMapper.readTree(json)

            if (!arrayNode.isArray || arrayNode.size() == 0) return

            candles.clear()
            arrayNode.forEach { node ->
                val openTime = node[0].asLong()
                val open = node[1].asText().toDouble()
                val high = node[2].asText().toDouble()
                val low = node[3].asText().toDouble()
                val close = node[4].asText().toDouble()
                val volume = node[5].asText().toDouble()
                val closeTime = node[6].asLong()

                candles.addLast(
                    Candle(
                        symbol = symbol,
                        openTime = openTime,
                        closeTime = closeTime,
                        open = open,
                        high = high,
                        low = low,
                        close = close,
                        volume = volume
                    )
                )
            }

            val last = candles.lastOrNull() ?: return
            val snapshot = MarketSnapshot(
                symbol = symbol,
                lastPrice = last.close,
                candles = candles.toList(),
                indicators = Indicators()
            )

            listeners.forEach { it.invoke(snapshot) }

        } catch (ex: Exception) {
            // If we’re stopping, don’t scare with error logs
            if (!running.get()) {
                logger.debug("Ignoring market data error during shutdown: {}", ex.toString())
            } else {
                logger.error("Error fetching Binance klines", ex)
            }
        }
    }

}
