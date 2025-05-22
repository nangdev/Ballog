package com.ballog.mobile.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class MatchDataCardInfo(
    val id: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val buttonText: String
) 
