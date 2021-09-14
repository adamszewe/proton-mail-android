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

package ch.protonmail.android.labels.data

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.work.WorkInfo
import ch.protonmail.android.api.ProtonMailApi
import ch.protonmail.android.core.NetworkConnectivityManager
import ch.protonmail.android.labels.data.local.LabelDao
import ch.protonmail.android.labels.data.local.model.LabelEntity
import ch.protonmail.android.labels.data.mapper.LabelEntityApiMapper
import ch.protonmail.android.labels.data.mapper.LabelEntityDomainMapper
import ch.protonmail.android.labels.data.remote.worker.ApplyMessageLabelWorker
import ch.protonmail.android.labels.data.remote.worker.DeleteLabelsWorker
import ch.protonmail.android.labels.data.remote.worker.PostLabelWorker
import ch.protonmail.android.labels.data.remote.worker.RemoveMessageLabelWorker
import ch.protonmail.android.labels.domain.LabelRepository
import ch.protonmail.android.labels.domain.model.Label
import ch.protonmail.android.labels.domain.model.LabelId
import ch.protonmail.android.labels.domain.model.LabelType
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.runBlocking
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

internal class LabelRepositoryImpl @Inject constructor(
    private val labelDao: LabelDao,
    private val api: ProtonMailApi,
    private val labelApiMapper: LabelEntityApiMapper,
    private val labelDomainMapper: LabelEntityDomainMapper,
    private val networkConnectivityManager: NetworkConnectivityManager,
    private val applyMessageLabelWorker: ApplyMessageLabelWorker.Enqueuer,
    private val removeMessageLabelWorker: RemoveMessageLabelWorker.Enqueuer,
    private val deleteLabelsWorker: DeleteLabelsWorker.Enqueuer,
    private val postLabelWorker: PostLabelWorker.Enqueuer
) : LabelRepository {

    override fun observeAllLabels(userId: UserId, shallRefresh: Boolean): Flow<List<Label>> =
        labelDao.observeAllLabels(userId)
            .onStart {
                if (shallRefresh && networkConnectivityManager.isInternetConnectionPossible()) {
                    Timber.v("Fetching fresh labels")
                    fetchAndSaveAllLabels(userId)
                }
            }
            .mapToLabel()
            .onEach {
                Timber.v("Emitting new labels size: ${it.size} user: $userId")
            }

    override suspend fun findAllLabels(userId: UserId, shallRefresh: Boolean): List<Label> =
        observeAllLabels(userId, shallRefresh).first()

    override fun observeLabels(labelsIds: List<LabelId>, userId: UserId): Flow<List<Label>> =
        labelDao.observeLabelsById(userId, labelsIds)
            .mapToLabel()

    override suspend fun findLabels(labelsIds: List<LabelId>, userId: UserId): List<Label> =
        observeLabels(labelsIds, userId).first()

    override fun observeLabel(labelId: LabelId): Flow<Label?> =
        labelDao.observeLabelById(labelId)
            .transform { label ->
                if (label != null) {
                    emit(labelDomainMapper.toLabel(label))
                } else {
                    return@transform
                }
            }

    override suspend fun findLabel(labelId: LabelId): Label? =
        observeLabel(labelId).first()

    override fun findLabelBlocking(labelId: LabelId): Label? {
        return runBlocking {
            findLabel(labelId)
        }
    }

    override fun observeContactGroups(userId: UserId): Flow<List<Label>> =
        labelDao.observeLabelsByType(userId, LabelType.CONTACT_GROUP)
            .mapToLabel()

    override suspend fun findContactGroups(userId: UserId): List<Label> =
        labelDao.findLabelsByType(userId, LabelType.CONTACT_GROUP)
            .map { labelDomainMapper.toLabel(it) }

    override fun observeSearchContactGroups(labelName: String, userId: UserId): Flow<List<Label>> =
        labelDao.observeSearchLabelsByNameAndType(userId, labelName, LabelType.CONTACT_GROUP)
            .mapToLabel()

    override suspend fun findLabelByName(labelName: String, userId: UserId): Label? =
        labelDao.findLabelByName(userId, labelName)?.let {
            labelDomainMapper.toLabel(it)
        }

    override fun findAllLabelsPaged(userId: UserId): DataSource.Factory<Int, LabelEntity> =
        labelDao.findAllLabelsPaged(userId, LabelType.MESSAGE_LABEL)

    override fun findAllFoldersPaged(userId: UserId): DataSource.Factory<Int, LabelEntity> =
        labelDao.findAllLabelsPaged(userId, LabelType.FOLDER)

    override suspend fun saveLabels(labels: List<Label>, userId: UserId) {
        saveLabels(
            labels.map {
                labelDomainMapper.toEntity(it, userId)
            }
        )
    }

    private suspend fun saveLabels(labels: List<LabelEntity>) {
        Timber.v("Save labels: ${labels.map { it.id.id }}")
        labelDao.insertOrUpdate(*labels.toTypedArray())
    }

    override suspend fun saveLabel(label: Label, userId: UserId) {
        saveLabels(
            listOf(labelDomainMapper.toEntity(label, userId))
        )
    }

    override suspend fun deleteLabel(labelId: LabelId) {
        labelDao.deleteLabelsById(listOf(labelId))
    }

    override suspend fun deleteAllLabels(userId: UserId) {
        labelDao.deleteAllLabels(userId)
    }

    override suspend fun deleteContactGroups(userId: UserId) {
        labelDao.deleteContactGroups(userId)
    }

    private suspend fun fetchAndSaveAllLabels(
        userId: UserId
    ): List<LabelEntity> = coroutineScope {
        val serverLabels = async { api.getLabels(userId).valueOrThrow.labels }
        val serverFolders = async { api.getFolders(userId).valueOrThrow.labels }
        val serverContactGroups = async { api.getContactGroups(userId).valueOrThrow.labels }
        val allLabels = serverLabels.await() + serverFolders.await() + serverContactGroups.await()
        val allLabelsEntities = allLabels.map { labelApiMapper.toEntity(it, userId) }
        Timber.v("fetchAndSaveAllLabels size: ${allLabelsEntities.size} user: $userId")
        saveLabels(allLabelsEntities)
        allLabelsEntities
    }

    override fun applyMessageLabelWithWorker(messageIds: List<String>, labelId: String) {
        applyMessageLabelWorker.enqueue(messageIds, labelId)
    }

    override fun removeMessageLabelWithWorker(messageIds: List<String>, labelId: String) {
        removeMessageLabelWorker.enqueue(messageIds, labelId)
    }

    override suspend fun deleteLabelsWithWorker(labelIds: List<LabelId>): LiveData<WorkInfo> {
        // delete db
        labelDao.deleteLabelsById(labelIds)
        // schedule remote removal
        return deleteLabelsWorker.enqueue(labelIds)
    }

    override fun saveLabelWithWorker(
        labelName: String,
        color: String,
        isUpdate: Boolean,
        labelType: LabelType,
        labelId: String?
    ) = postLabelWorker.enqueue(
        labelName,
        color,
        isUpdate,
        labelType,
        labelId
    )

    private fun Flow<List<LabelEntity>>.mapToLabel() = map { entities ->
        entities.map { entity ->
            labelDomainMapper.toLabel(entity)
        }
    }

}
