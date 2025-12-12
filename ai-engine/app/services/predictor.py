from app.schemas import AiPredictionRequest, AiPredictionResponse
from app.settings import settings
from app.services.rules import pct_move_rule
from app.services.features import ema

def ema_crossover(req: AiPredictionRequest) -> AiPredictionResponse:
    closes = [c.close for c in req.candles]
    s = settings.ema_short
    l = settings.ema_long

    min_required = max(s, l) + 2
    if len(closes) < min_required:
        return AiPredictionResponse(action="HOLD", confidence=0.0, extraInfo="Not enough candles")

    ema_s = ema(closes, s)
    ema_l = ema(closes, l)
    i = len(closes) - 1

    if len(ema_s) <= i or len(ema_l) <= i:
        return AiPredictionResponse(action="HOLD", confidence=0.0, extraInfo="EMA calc short")

    sp, lp = ema_s[i - 1], ema_l[i - 1]
    sn, ln = ema_s[i], ema_l[i]

    if sp <= lp and sn > ln:
        return AiPredictionResponse(action="BUY", confidence=0.8, extraInfo="ema_cross_up")
    if sp >= lp and sn < ln:
        return AiPredictionResponse(action="SELL", confidence=0.8, extraInfo="ema_cross_down")

    return AiPredictionResponse(action="HOLD", confidence=0.2, extraInfo="no_cross")

def predict(req: AiPredictionRequest) -> AiPredictionResponse:
    if settings.mode == "ema":
        out = ema_crossover(req)
    else:
        out = pct_move_rule(req, settings.pct_threshold)

    # confidence gate (optional, but good hygiene)
    if out.action != "HOLD" and out.confidence < settings.min_confidence:
        return AiPredictionResponse(action="HOLD", confidence=out.confidence, extraInfo=f"gated({out.extraInfo})")

    return out
