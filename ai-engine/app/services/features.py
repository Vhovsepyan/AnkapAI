import numpy as np
from typing import List

def ema(values: List[float], period: int) -> List[float]:
    if period <= 0:
        raise ValueError("period must be > 0")
    if len(values) < period:
        return []

    k = 2.0 / (period + 1)
    out = [0.0] * len(values)

    sma = sum(values[:period]) / period
    out[period - 1] = sma

    for i in range(period, len(values)):
        out[i] = values[i] * k + out[i - 1] * (1 - k)

    return out
