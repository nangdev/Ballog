package com.ballog.mobile.data.model

data class ApiResponse<T>(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: T
) 
