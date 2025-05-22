package com.ballog.mobile.data.repository

import android.content.Context
import android.util.Log

class IdStore(context: Context) {
    private val prefs = context.getSharedPreferences("id_store", Context.MODE_PRIVATE)
    private val KEY_ID_LIST = "uuid_list"
    private val MAX_SIZE = 50

    // UUID 리스트 불러오기
    fun getAll(): List<String> {
        val saved = prefs.getString(KEY_ID_LIST, "") ?: ""
        val list = if (saved.isBlank()) emptyList() else saved.split(",")
        Log.d("IdStore", "[getAll] list: $list")
        return list
    }

    // UUID 추가 (LRU 방식)
    fun add(uuid: String) {
        val deque = ArrayDeque(getAll())
        deque.remove(uuid) // 중복 제거(최신으로 이동)
        if (deque.size >= MAX_SIZE) {
            deque.removeFirst()
        }
        deque.addLast(uuid)
        prefs.edit().putString(KEY_ID_LIST, deque.joinToString(",")).apply()
        Log.d("IdStore", "[add] uuid: $uuid, result: ${deque.toList()}")
    }

    // UUID 삭제
    fun remove(uuid: String) {
        val deque = ArrayDeque(getAll())
        val removed = deque.remove(uuid)
        prefs.edit().putString(KEY_ID_LIST, deque.joinToString(",")).apply()
        Log.d("IdStore", "[remove] uuid: $uuid, removed: $removed, result: ${deque.toList()}")
    }

    // 제일 앞(가장 오래된) UUID 삭제
    fun removeFirst() {
        val deque = ArrayDeque(getAll())
        if (deque.isNotEmpty()) {
            val removed = deque.removeFirst()
            prefs.edit().putString(KEY_ID_LIST, deque.joinToString(",")).apply()
            Log.d("IdStore", "[removeFirst] removed: $removed, result: ${deque.toList()}")
        } else {
            Log.d("IdStore", "[removeFirst] deque is empty")
        }
    }

    // 특정 ID가 저장소에 존재하는지 확인
    fun checkId(id: String): Boolean {
        val result = getAll().contains(id)
        Log.d("IdStore", "[contains] id: $id, result: $result")
        return result
    }

    // 전체 삭제
    fun clear() {
        prefs.edit().remove(KEY_ID_LIST).apply()
        Log.d("IdStore", "[clear] 전체 삭제")
    }
} 
