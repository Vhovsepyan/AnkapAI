from pydantic import BaseModel
from typing import List, Optional, Literal

PositionSide = Literal["NONE", "LONG"]

class Candle(BaseModel):
    symbol: str
    openTime: int
    closeTime: int
    open: float
    high: float
    low: float
    close: float
    volume: float

class PositionState(BaseModel):
    symbol: str
    side: PositionSide
    quantity: float
    avgPrice: Optional[float] = None

class AiPredictionRequest(BaseModel):
    symbol: str
    timestamp: int
    lastPrice: float
    candles: List[Candle]
    position: PositionState

class AiPredictionResponse(BaseModel):
    action: Literal["BUY", "SELL", "HOLD"]
    confidence: float
    extraInfo: Optional[str] = None
