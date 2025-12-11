package com.ankap.tradingbot.ai

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class AiClient(
    private val webClientBuilder: WebClient.Builder,
    @Value("\${ai.engine.base-url:http://localhost:8001}") private val baseUrl: String
) {

    private val logger = LoggerFactory.getLogger(AiClient::class.java)

    private val client: WebClient = webClientBuilder
        .baseUrl(baseUrl)
        .build()

    fun predictSync(request: AiPredictionRequest): AiPredictionResponse? {
        return try {
            client.post()
                .uri("/predict")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiPredictionResponse::class.java)
                .block()  // for now, sync call; later can be async with timeout
        } catch (ex: Exception) {
            logger.error("AI prediction call failed: ${ex.message}", ex)
            null
        }
    }
}
