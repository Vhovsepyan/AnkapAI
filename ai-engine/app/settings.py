import os
from pydantic_settings import BaseSettings, SettingsConfigDict

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))  # ai-engine/app/.. = ai-engine

class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=os.path.join(BASE_DIR, ".env"),
        env_prefix="AI_ENGINE_"
    )

    mode: str = "rule"
    log_level: str = "INFO"
    pct_threshold: float = 0.002
    min_confidence: float = 0.60
    ema_short: int = 3
    ema_long: int = 5

settings = Settings()
