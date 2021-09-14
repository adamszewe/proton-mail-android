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

package ch.protonmail.android.labels.domain

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.work.WorkInfo
import ch.protonmail.android.labels.data.local.model.LabelEntity
import ch.protonmail.android.labels.domain.model.Label
import ch.protonmail.android.labels.domain.model.LabelId
import ch.protonmail.android.labels.domain.model.LabelType
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId

interface LabelRepository {

    fun observeAllLabels(userId: UserId, shallRefresh: Boolean = false): Flow<List<Label>>

    suspend fun findAllLabels(userId: UserId, shallRefresh: Boolean = false): List<Label>

    fun observeLabels(labelsIds: List<LabelId>, userId: UserId): Flow<List<Label>>

    suspend fun findLabels(labelsIds: List<LabelId>, userId: UserId): List<Label>

    suspend fun findLabel(labelId: LabelId): Label?

    fun observeLabel(labelId: LabelId): Flow<Label?>

    fun findLabelBlocking(labelId: LabelId): Label?

    fun observeContactGroups(userId: UserId): Flow<List<Label>>

    suspend fun findContactGroups(userId: UserId): List<Label>

    fun observeSearchContactGroups(labelName: String, userId: UserId): Flow<List<Label>>

    suspend fun findLabelByName(labelName: String, userId: UserId): Label?

    fun findAllLabelsPaged(userId: UserId): DataSource.Factory<Int, LabelEntity>

    fun findAllFoldersPaged(userId: UserId): DataSource.Factory<Int, LabelEntity>

    suspend fun saveLabel(label: Label, userId: UserId)

    suspend fun saveLabels(labels: List<Label>, userId: UserId)

    suspend fun deleteLabel(labelId: LabelId)

    suspend fun deleteAllLabels(userId: UserId)

    suspend fun deleteContactGroups(userId: UserId)

    fun applyMessageLabelWithWorker(messageIds: List<String>, labelId: String)

    fun removeMessageLabelWithWorker(messageIds: List<String>, labelId: String)

    suspend fun deleteLabelsWithWorker(labelIds: List<LabelId>): LiveData<WorkInfo>

    fun saveLabelWithWorker(
        labelName: String,
        color: String,
        isUpdate: Boolean,
        labelType: LabelType,
        labelId: String?
    ): LiveData<WorkInfo>
}
