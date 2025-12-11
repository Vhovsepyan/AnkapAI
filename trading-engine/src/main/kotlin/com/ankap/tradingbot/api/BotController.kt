package com.ankap.tradingbot.api

import com.ankap.tradingbot.BotRunner
import com.ankap.tradingbot.trade.TradeHistoryService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/bot")
class BotController(
    private val botRunner: BotRunner,
    private val tradeHistoryService: TradeHistoryService
) {

    private val logger = LoggerFactory.getLogger(BotController::class.java)

    @GetMapping("/status")
    fun status(): BotStatusDto = botRunner.getStatusDto()

    @PostMapping("/start")
    fun start(): ResponseEntity<BotStatusDto> {
        logger.info("API requested bot start")
        botRunner.start()
        return ResponseEntity.ok(botRunner.getStatusDto())
    }

    @PostMapping("/stop")
    fun stop(): ResponseEntity<BotStatusDto> {
        logger.info("API requested bot stop")
        botRunner.stop()
        return ResponseEntity.ok(botRunner.getStatusDto())
    }

    @GetMapping("/trades")
    fun trades(): List<TradeDto> =
        tradeHistoryService.getRecentTrades(50).map { it.toDto() }
}
