package com.yourdocs.domain.model

import java.time.Instant

/**
 * Domain model representing a folder that contains documents.
 * This is the business logic representation, independent of data layer implementation.
 */
data class Folder(
    val id: String,
    val name: String,
    val isPinned: Boolean,
    val isLocked: Boolean,
    val documentCount: Int,
    val colorHex: String? = null,
    val emoji: String? = null,
    val description: String? = null,
    val lockMethod: LockMethod? = null,
    val sortPreference: SortPreference? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun generateId(): String = java.util.UUID.randomUUID().toString()
    }
}

enum class SortPreference {
    NAME_ASC, NAME_DESC, DATE_NEWEST, DATE_OLDEST, SIZE_LARGEST, SIZE_SMALLEST, TYPE
}
