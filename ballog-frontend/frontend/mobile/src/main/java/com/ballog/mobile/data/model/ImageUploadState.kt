package com.ballog.mobile.data.model

sealed class ImageUploadState {
    object Initial : ImageUploadState()
    object Loading : ImageUploadState()
    data class Success(val imageUrl: String) : ImageUploadState()
    data class Error(val message: String) : ImageUploadState()
} 