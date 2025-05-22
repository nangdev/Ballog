package com.ballog.mobile.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri

fun getVideoDurationInSec(context: Context, uri: Uri): Int {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        val durationMs =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                ?: 0L
        (durationMs / 1000).toInt()
    } finally {
        retriever.release()
    }
}

fun isValidHighlightTime(
    startMin: String, startSec: String,
    endMin: String, endSec: String,
    videoDurationSec: Int
): Boolean {
    val sh = startMin.toIntOrNull()
    val sm = startSec.toIntOrNull()
    val eh = endMin.toIntOrNull()
    val em = endSec.toIntOrNull()

    if (sh == null || sm == null || eh == null || em == null) return false
    if (sm !in 0..59 || em !in 0..59) return false

    val startSec = sh * 60 + sm
    val endSec = eh * 60 + em

    if (startSec >= endSec) return false
    if (endSec > videoDurationSec) return false

    return true
}
