package com.ankap.tradingbot.ai

import com.ankap.tradingbot.common.Candle
import com.ankap.tradingbot.common.PositionSide
import com.ankap.tradingbot.common.PositionState
import com.ankap.tradingbot.market.MarketDataService
import com.ankap.tradingbot.portfolio.PositionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ai")
class AiDebugController(
    private val aiClient: AiClient,
    private val positionService: PositionService
) {

    @GetMapping("/predict")
    fun predict(
        @RequestParam symbol: String = "BTCUSDT",
        @RequestParam lastPrice: Double = 50_000.0,
        @RequestParam(defaultValue = "20") candles: Int
    ): ResponseEntity<AiPredictionResponse> {

        // Use real position from your PositionService (in-memory)
        val position: PositionState = positionService.getPosition(symbol)

        // Build dummy candles for testing (AI only needs structure for now)
        val now = System.currentTimeMillis()
        val candleList: List<Candle> = (0 until candles).map { i ->
            val t1 = now - (candles - i).toLong() * 60_000L
            val t2 = t1 + 60_000L
            Candle(
                symbol = symbol,
                openTime = t1,
                closeTime = t2,
                open = lastPrice,
                high = lastPrice,
                low = lastPrice,
                close = lastPrice,
                volume = 1.0
            )
        }

        val req = AiPredictionRequest(
            symbol = symbol,
            timestamp = now,
            lastPrice = lastPrice,
            candles = candleList,
            position = position
        )

        val resp = aiClient.predictSync(req)
            ?: AiPredictionResponse(
                action = "HOLD",
                confidence = 0.0,
                extraInfo = "AI call failed (null response)"
            )

        return ResponseEntity.ok(resp)
    }
}
