/*
 * Copyright (c) 2022 Proton Technologies AG
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

package ch.protonmail.android.worker.repository

import androidx.lifecycle.asFlow
import androidx.work.Operation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class WorkerRepository @Inject constructor(
    private val workManager: WorkManager
) {

    suspend fun findWorkInfoForUniqueWork(uniqueWorkName: String): List<WorkInfo> =
        workManager.getWorkInfosForUniqueWorkLiveData(uniqueWorkName)
            .asFlow()
            .filterNotNull()
            .first()

    fun cancelUniqueWork(uniqueWorkName: String): Operation = workManager.cancelUniqueWork(uniqueWorkName)
}