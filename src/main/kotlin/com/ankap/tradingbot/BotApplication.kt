package com.ankap.tradingbot

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class BotApplication {

	private val logger = LoggerFactory.getLogger(BotApplication::class.java)
//
//	@Bean
//	fun botRunnerCommandLine(botRunner: BotRunner): CommandLineRunner {
//		return CommandLineRunner {
//			logger.info("Application started, initializing trading bot...")
//			botRunner.start()
//			// important: do NOT block here; BotRunner uses its own async flow
//		}
//	}
}

fun main(args: Array<String>) {
	runApplication<BotApplication>(*args)
}
