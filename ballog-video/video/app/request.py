from pydantic import BaseModel
from fastapi import Form, File, UploadFile

class HighlightRequest(BaseModel):
    video_id: int = Form(...)
    audioFile: UploadFile = File(...)
    