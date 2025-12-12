from fastapi import APIRouter
from app.schemas import AiPredictionRequest, AiPredictionResponse
from app.services.predictor import predict

router = APIRouter()

@router.get("/health")
def health():
    return {"status": "ok"}

@router.post("/predict", response_model=AiPredictionResponse)
def do_predict(req: AiPredictionRequest):
    return predict(req)
