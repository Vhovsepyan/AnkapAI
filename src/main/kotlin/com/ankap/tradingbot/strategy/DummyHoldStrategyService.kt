package com.ankap.tradingbot.strategy

import com.ankap.tradingbot.common.Action
import com.ankap.tradingbot.common.MarketSnapshot
import com.ankap.tradingbot.common.TradeSignal
import org.springframework.stereotype.Service

@Service
class DummyHoldStrategyService : StrategyService {
    override fun onMarketSnapshot(snapshot: MarketSnapshot): TradeSignal {
        return TradeSignal(
            symbol = snapshot.symbol,
            action = Action.HOLD,
            confidence = 0.0
        )
    }
}
