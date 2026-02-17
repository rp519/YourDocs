package com.yourdocs.domain.model

class FreeLimitReachedException(val limitType: String) : Exception(
    when (limitType) {
        "folder" -> "You've reached the free limit of 5 folders. Upgrade to Pro for unlimited folders."
        "document" -> "You've reached the free limit of 25 documents. Upgrade to Pro for unlimited documents."
        else -> "You've reached a free tier limit. Upgrade to Pro to continue."
    }
)
