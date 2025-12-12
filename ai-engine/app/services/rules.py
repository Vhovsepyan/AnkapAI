from app.schemas import AiPredictionRequest, AiPredictionResponse

def pct_move_rule(req: AiPredictionRequest, pct_threshold: float) -> AiPredictionResponse:
    if len(req.candles) < 2:
        return AiPredictionResponse(action="HOLD", confidence=0.0, extraInfo="Not enough candles")

    # Use previous candle close, not the last candle
    ref = req.candles[-2].close
    if ref == 0:
        return AiPredictionResponse(action="HOLD", confidence=0.0, extraInfo="Bad ref price")

    change = (req.lastPrice - ref) / ref

    if change >= pct_threshold:
        return AiPredictionResponse(action="SELL", confidence=0.7, extraInfo=f"pct_move={change:.6f}")
    if change <= -pct_threshold:
        return AiPredictionResponse(action="BUY", confidence=0.7, extraInfo=f"pct_move={change:.6f}")

    return AiPredictionResponse(
        action="HOLD",
        confidence=0.2,
        extraInfo=f"pct_move={change:.6f}, thr={pct_threshold}"
    )

