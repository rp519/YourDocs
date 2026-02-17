package com.yourdocs.domain.model

import java.time.Instant

data class Tag(
    val id: String,
    val name: String,
    val colorHex: String,
    val createdAt: Instant
) {
    companion object {
        fun generateId(): String = java.util.UUID.randomUUID().toString()
    }
}
