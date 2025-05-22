import time as _time_module
import numpy as np
from pydub import AudioSegment
from app.utils import seconds_to_time

async def analyze_audio_file(audio_path: str, zscore_threshold=1.0) -> list[dict]:
    print(f"[analyzer] 📊 데시벨 분석 시작 - {audio_path}")
    start_time = _time_module.time()

    sound = AudioSegment.from_file(audio_path, format=audio_path.split('.')[-1])
    frame_ms = 100
    loudness_values = [sound[i:i + frame_ms].dBFS for i in range(0, len(sound), frame_ms)]

    mean = np.mean(loudness_values)
    std = np.std(loudness_values)
    print(f"[analyzer] Z-score 계산 - 평균: {mean:.2f}, 표준편차: {std:.2f}")

    zscore_loudness = [(db - mean) / std for db in loudness_values]
    smoothed_zscore = np.convolve(zscore_loudness, np.ones(30) / 30, mode='same')

    louderthanthreshold = [idx * 0.1 for idx, val in enumerate(smoothed_zscore) if val > zscore_threshold]

    highlight_segments = [
        (max(0, round(center - 10, 2)), round(center + 10, 2)) for center in louderthanthreshold
    ]

    # 병합
    merged_segments = []
    if highlight_segments:
        highlight_segments.sort()
        merged_segments.append(highlight_segments[0])
        for current_start, current_end in highlight_segments[1:]:
            last_start, last_end = merged_segments[-1]
            if current_start <= last_end:
                continue
            else:
                merged_segments.append((current_start, current_end))
        
    
    elapsed = round(_time_module.time() - start_time, 2)
    print(f"[analyzer] ✅ 분석 완료 - 소요 {elapsed}s)")
    
    result = [
        {
            "highlightId": None,
            "highlightName": f"하이라이트 {i + 1}",
            "startTime": seconds_to_time(start).strftime("%H:%M:%S"),
            "endTime": seconds_to_time(end).strftime("%H:%M:%S")
        }
        for i, (start, end) in enumerate(merged_segments)
    ]
    return result