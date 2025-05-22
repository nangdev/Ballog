package com.ballog.mobile.util

import android.content.Context
import android.media.MediaMetadataRetriever

object VideoUtils {
    fun getVideoDurationString(context: Context, uri: android.net.Uri): String {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0
        retriever.release()
        val totalSec = durationMs / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format("00:%02d:%02d", min, sec)
    }
}
