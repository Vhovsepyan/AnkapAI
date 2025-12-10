package com.ankap.tradingbot.market

import com.ankap.tradingbot.common.Candle
import com.ankap.tradingbot.common.Indicators
import com.ankap.tradingbot.common.MarketSnapshot
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@Service
class FakeMarketDataService : MarketDataService {

    private val logger = LoggerFactory.getLogger(FakeMarketDataService::class.java)

    private val listeners = CopyOnWriteArrayList<(MarketSnapshot) -> Unit>()
    private val running = AtomicBoolean(false)

    // single-threaded executor for generating data
    private val executor = Executors.newSingleThreadScheduledExecutor()

    // state for synthetic price series
    private var currentPrice: Double = 50_000.0     // start price
    private val symbol: String = "BTCUSDT"
    private val candlesWindowSize = 50
    private val candles = ArrayDeque<Candle>()

    override fun start() {
        if (running.compareAndSet(false, true)) {
            logger.info("Starting FakeMarketDataService...")
            executor.scheduleAtFixedRate(
                { generateAndDispatchSnapshot() },
                0,
                1,
                TimeUnit.SECONDS
            )
        }
    }

    override fun stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("Stopping FakeMarketDataService...")
            executor.shutdownNow()
        }
    }

    override fun registerListener(listener: (MarketSnapshot) -> Unit) {
        listeners.add(listener)
    }

    private fun generateAndDispatchSnapshot() {
        if (!running.get()) return

        try {
            val now = System.currentTimeMillis()
            val prevPrice = currentPrice

            // very simple random walk
            val changePercent = Random.nextDouble(from = -0.001, until = 0.001) // ±0.1%
            currentPrice *= (1.0 + changePercent)

            // clamp a bit so it stays in a reasonable range
            currentPrice = min(80_000.0, max(10_000.0, currentPrice))

            val candle = Candle(
                symbol = symbol,
                openTime = now - 1_000,
                closeTime = now,
                open = prevPrice,
                high = max(prevPrice, currentPrice),
                low = min(prevPrice, currentPrice),
                close = currentPrice,
                volume = Random.nextDouble(1.0, 10.0)
            )

            if (candles.size >= candlesWindowSize) {
                candles.removeFirst()
            }
            candles.addLast(candle)

            val snapshot = MarketSnapshot(
                symbol = symbol,
                lastPrice = currentPrice,
                candles = candles.toList(),
                indicators = Indicators(
                    // you can compute simple indicators later
                    rsi = null,
                    emaShort = null,
                    emaLong = null
                )
            )

            listeners.forEach { it.invoke(snapshot) }
        } catch (ex: Exception) {
            logger.error("Error generating fake market snapshot", ex)
        }
    }
}
