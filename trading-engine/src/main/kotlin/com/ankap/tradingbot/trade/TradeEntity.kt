package com.ankap.tradingbot.trade

import com.ankap.tradingbot.common.OrderStatus
import com.ankap.tradingbot.common.Side
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "trades")
class TradeEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var orderId: String = "",

    @Column(nullable = false)
    var symbol: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var side: Side = Side.BUY,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.NEW,

    @Column(nullable = false)
    var executedQty: Double = 0.0,

    @Column
    var avgPrice: Double? = null,

    @Column
    var realizedPnl: Double? = null,

    @Column(nullable = false)
    var timestamp: Instant = Instant.now()

) {
    // REQUIRED BY HIBERNATE / JPA
    constructor() : this(
        id = null,
        orderId = "",
        symbol = "",
        side = Side.BUY,
        status = OrderStatus.NEW,
        executedQty = 0.0,
        avgPrice = null,
        realizedPnl = null,
        timestamp = Instant.now()
    )
}
