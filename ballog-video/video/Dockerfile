FROM python:3.10-slim

WORKDIR /app

# 시스템 패키지 설치 (PostgreSQL 드라이버와 ffmpeg를 위해 필요)
RUN apt-get update && apt-get install -y \
    gcc \
    libpq-dev \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
# 윈도우 전용 패키지 제외하고 설치
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# app 디렉토리 내의 main.py를 실행하도록 수정
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]