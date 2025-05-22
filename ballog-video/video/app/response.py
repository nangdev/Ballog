from pydantic import BaseModel
from typing import List, Optional

# 응답 모델
class BaseResponse(BaseModel):
    isSuccess: bool
    code: int
    message: str
    result: None
    
class HighlightItem(BaseModel):
    highlightId : Optional[int] = None
    highlightName: str
    startTime: str
    endTime: str

class HighlightResult(BaseModel):
    highlightList: List[HighlightItem]

