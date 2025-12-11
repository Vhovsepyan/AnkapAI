package com.ankap.tradingbot.trade

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TradeRepository : JpaRepository<TradeEntity, Long> {

    fun findTop50ByOrderByTimestampDesc(): List<TradeEntity>
}
