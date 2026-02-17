package com.yourdocs.domain.usecase

import com.yourdocs.data.preferences.UserPreferencesRepository
import com.yourdocs.data.security.PinHasher
import javax.inject.Inject

class VerifyPinUseCase @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val pinHasher: PinHasher
) {
    suspend operator fun invoke(pin: String): Boolean {
        val storedHash = preferencesRepository.getPinHashSync() ?: return false
        return pinHasher.verify(pin, storedHash)
    }
}
