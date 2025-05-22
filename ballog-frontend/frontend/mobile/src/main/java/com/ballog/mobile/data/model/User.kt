package com.ballog.mobile.data.model

// 유저 도메인 모델 정의 예정

data class User(
    val email: String,
    val nickname: String,
    val birthDate: String,
    val profileImageUrl: String?
)

data class PlayerCardInfo(
    val nickname: String,
    val profileImageUrl: String,
    val stats: List<Pair<String, String>>
)
