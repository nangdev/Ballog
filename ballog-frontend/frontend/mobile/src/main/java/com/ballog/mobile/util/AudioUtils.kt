package com.ballog.mobile.util

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

object AudioUtils {
    private const val TAG = "AudioUtils"

    /**
     * MP4 비디오에서 M4A 오디오를 추출합니다.
     */
    suspend fun extractAudioToM4a(
        context: Context,
        videoFile: File,
        saveToProject: Boolean = false
    ): File? = withContext(Dispatchers.IO) {
        Log.d(TAG, "🎬 M4A 오디오 추출 시작")
        
        if (!videoFile.exists() || !videoFile.canRead()) {
            Log.e(TAG, "❌ 입력 비디오 파일이 존재하지 않거나 읽을 수 없음")
            return@withContext null
        }
        
        var extractor: MediaExtractor? = null
        var muxer: MediaMuxer? = null
        var audioFile: File? = null
        
        try {
            // 파일 저장 위치 설정
            audioFile = if (saveToProject) {
                try {
                    val resourcesDir = File(context.filesDir, "audio")
                    if (!resourcesDir.exists()) {
                        resourcesDir.mkdirs()
                    }
                    File(resourcesDir, "converted_${System.currentTimeMillis()}.m4a")
                } catch (e: Exception) {
                    Log.e(TAG, "⚠️ 프로젝트 디렉토리 생성 실패, 앱 디렉토리로 대체", e)
                    File(context.getExternalFilesDir(null), "converted_${System.currentTimeMillis()}.m4a")
                }
            } else {
                File(context.getExternalFilesDir(null), "converted_${System.currentTimeMillis()}.m4a")
            }
            
            extractor = MediaExtractor().apply {
                setDataSource(videoFile.absolutePath)
            }
            
            muxer = MediaMuxer(audioFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            
            // 오디오 트랙 찾기
            var audioTrackIndex = -1
            var muxerTrackIndex = -1
            
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                
                if (mime?.startsWith("audio/") == true) {
                    audioTrackIndex = i
                    muxerTrackIndex = muxer.addTrack(format)
                    break
                }
            }
            
            if (audioTrackIndex == -1) {
                Log.e(TAG, "❌ 오디오 트랙을 찾을 수 없음")
                return@withContext null
            }
            
            // 추출 및 복사
            val buffer = ByteBuffer.allocate(1024 * 1024)
            val bufferInfo = MediaCodec.BufferInfo()
            
            extractor.selectTrack(audioTrackIndex)
            muxer.start()
            
            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break
                
                bufferInfo.offset = 0
                bufferInfo.size = sampleSize
                bufferInfo.presentationTimeUs = extractor.sampleTime
                bufferInfo.flags = extractor.sampleFlags
                
                muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                extractor.advance()
            }
            
            Log.d(TAG, "🏁 변환 완료!")
            
            muxer.stop()
            muxer.release()
            extractor.release()
            
            Log.d(TAG, "✅ M4A 파일 생성 완료")
            
            audioFile
        } catch (e: Exception) {
            Log.e(TAG, "❌ M4A 오디오 추출 중 오류 발생", e)
            audioFile?.delete()
            null
        } finally {
            try {
                try {
                    if (muxer != null) {
                        muxer.stop()
                    }
                } catch (e: IllegalStateException) {
                    // 이미 stop되었거나 초기화되지 않은 상태에서 stop을 호출한 경우 무시
                }
                
                muxer?.release()
                extractor?.release()
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ 리소스 정리 중 오류 발생", e)
            }
        }
    }
} 