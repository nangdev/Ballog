import os
import shutil
from fastapi import FastAPI, File, UploadFile, Form, HTTPException
from app.response import HighlightItem, HighlightResult
from app.service import analyze_audio_file

app = FastAPI(
    title="Audio Analysis Service",
    description="오디오 파일에서 하이라이트 구간 추출",
    version="1.0.0"
)

@app.post("/api/v1/videos/highlight/extract")
async def extract_highlight(
    file: UploadFile = File(...)
):
    # 1. 임시 폴더 생성
    tmp_dir = os.path.join(os.path.dirname(__file__), "../temp")
    os.makedirs(tmp_dir, exist_ok=True)

    # 2. 파일 저장
    audio_path = os.path.join(tmp_dir, file.filename)
    try:
        with open(audio_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"파일 저장 실패: {str(e)}")

    # 3. 하이라이트 추출
    try:
        result = await analyze_audio_file(audio_path)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"오디오 분석 실패: {str(e)}")
    finally:
        # 임시 파일 삭제
        if os.path.exists(audio_path):
            os.remove(audio_path)

    # 4. 하이라이트 결과 생성
    highlightList = [HighlightItem(**item) for item in result]
    
    # 5. 응답 생성
    response = HighlightResult(highlightList=highlightList)

    return response
