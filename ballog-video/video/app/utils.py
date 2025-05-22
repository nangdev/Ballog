from datetime import time

def seconds_to_time(sec: float) -> time:
    """초를 받아 datetime.time 객체로 변환"""
    hours = int(sec // 3600)
    minutes = int((sec % 3600) // 60)
    seconds = int(sec % 60)
    return time(hour=hours, minute=minutes, second=seconds)