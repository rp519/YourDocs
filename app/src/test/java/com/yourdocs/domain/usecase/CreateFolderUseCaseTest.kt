package com.yourdocs.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.yourdocs.domain.model.Folder
import com.yourdocs.domain.repository.FolderRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

class CreateFolderUseCaseTest {

    private lateinit var folderRepository: FolderRepository
    private lateinit var createFolderUseCase: CreateFolderUseCase

    @Before
    fun setup() {
        folderRepository = mock()
        createFolderUseCase = CreateFolderUseCase(folderRepository)
    }

    @Test
    fun `invoke with valid name creates folder`() = runTest {
        // Given
        val folderName = "My Documents"
        val mockFolder = Folder(
            id = "1",
            name = folderName,
            isPinned = false,
            isLocked = false,
            documentCount = 0,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        whenever(folderRepository.createFolder(any(), anyOrNull(), anyOrNull())).thenReturn(Result.success(mockFolder))

        // When
        val result = createFolderUseCase(folderName)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.name).isEqualTo(folderName)
        verify(folderRepository).createFolder(eq(folderName), anyOrNull(), anyOrNull())
    }

    @Test
    fun `invoke with empty name returns failure`() = runTest {
        // When
        val result = createFolderUseCase("")

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `invoke with whitespace only name returns failure`() = runTest {
        // When
        val result = createFolderUseCase("   ")

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `invoke with too long name returns failure`() = runTest {
        // Given
        val longName = "a".repeat(101) // Max is 100

        // When
        val result = createFolderUseCase(longName)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `invoke trims whitespace from name`() = runTest {
        // Given
        val folderName = "  My Documents  "
        val trimmedName = "My Documents"
        val mockFolder = Folder(
            id = "1",
            name = trimmedName,
            isPinned = false,
            isLocked = false,
            documentCount = 0,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        whenever(folderRepository.createFolder(eq(trimmedName), anyOrNull(), anyOrNull())).thenReturn(Result.success(mockFolder))

        // When
        val result = createFolderUseCase(folderName)

        // Then
        assertThat(result.isSuccess).isTrue()
        verify(folderRepository).createFolder(eq(trimmedName), anyOrNull(), anyOrNull())
    }
}
