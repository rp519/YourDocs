package com.yourdocs.domain.usecase

import com.yourdocs.data.preferences.UserPreferencesRepository
import com.yourdocs.data.security.PinHasher
import javax.inject.Inject

class SetupPinUseCase @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val pinHasher: PinHasher
) {
    suspend operator fun invoke(pin: String): Result<Unit> {
        if (pin.length !in 4..6 || !pin.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("PIN must be 4-6 digits"))
        }
        return try {
            val hash = pinHasher.hash(pin)
            preferencesRepository.setPinHash(hash)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
