/*
 * Copyright (c) 2020 Proton Technologies AG
 *
 * This file is part of ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail. If not, see https://www.gnu.org/licenses/.
 */

package ch.protonmail.android.usecase.delete

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import androidx.work.workDataOf
import ch.protonmail.android.labels.domain.LabelRepository
import ch.protonmail.android.labels.domain.model.Label
import ch.protonmail.android.labels.domain.model.LabelId
import ch.protonmail.android.labels.domain.usecase.DeleteLabels
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DeleteLabelsTest {

    @get:Rule
    val archRule = InstantTaskExecutorRule()

    private val labelRepository: LabelRepository = mockk()

    private val deleteLabel = DeleteLabels(labelRepository)

    @Test
    fun verifyThatMessageIsSuccessfullyDeleted() {
        runBlockingTest {
            // given
            val testLabelId = LabelId("Id1")
            val contactLabel = mockk<Label> {
                every { id } returns testLabelId
            }
            val finishState = WorkInfo.State.SUCCEEDED
            val outputData = workDataOf("a" to "b")
            val workInfo = WorkInfo(
                UUID.randomUUID(),
                finishState,
                outputData,
                emptyList(),
                outputData,
                0
            )
            val workerStatusLiveData = MutableLiveData<WorkInfo>()
            workerStatusLiveData.value = workInfo
            val expected = true

            coEvery { labelRepository.findLabel(testLabelId) } returns contactLabel
            coEvery { labelRepository.deleteLabelsWithWorker(listOf(testLabelId)) } returns workerStatusLiveData

            // when
            val response = deleteLabel(listOf(testLabelId))
            response.observeForever { }

            // then
            assertNotNull(response.value)
            assertEquals(expected, response.value)
        }
    }

    @Test
    fun verifyThatMessageDeleteHasFailed() {
        runBlockingTest {
            // given
            val testLabelId = LabelId("Id1")
            val contactLabel = mockk<Label> {
                every { id } returns testLabelId
            }
            val finishState = WorkInfo.State.FAILED
            val outputData = workDataOf()
            val workInfo = WorkInfo(
                UUID.randomUUID(),
                finishState,
                outputData,
                emptyList(),
                outputData,
                0
            )
            val workerStatusLiveData = MutableLiveData<WorkInfo>()
            workerStatusLiveData.value = workInfo
            val expected = false

            coEvery { labelRepository.findLabel(testLabelId) } returns contactLabel
            coEvery { labelRepository.deleteLabelsWithWorker(listOf(testLabelId)) } returns workerStatusLiveData

            // when
            val response = deleteLabel(listOf(testLabelId))
            response.observeForever { }

            // then
            assertNotNull(response.value)
            assertEquals(expected, response.value)
        }
    }

    @Test
    fun verifyThatMessageIsEnqueuedThereIsNoValueEmitted() {
        runBlockingTest {
            // given
            val testLabelId = LabelId("Id1")
            val contactLabel = mockk<Label> {
                every { id } returns testLabelId
            }
            val finishState = WorkInfo.State.ENQUEUED
            val outputData = workDataOf("a" to "b")
            val workInfo = WorkInfo(
                UUID.randomUUID(),
                finishState,
                outputData,
                emptyList(),
                outputData,
                0
            )
            val workerStatusLiveData = MutableLiveData<WorkInfo>()
            workerStatusLiveData.value = workInfo

            coEvery { labelRepository.findLabel(testLabelId) } returns contactLabel
            coEvery { labelRepository.deleteLabelsWithWorker(listOf(testLabelId)) } returns workerStatusLiveData

            // when
            val response = deleteLabel(listOf(testLabelId))
            response.observeForever { }

            // then
            assertEquals(null, response.value)
        }
    }

}
