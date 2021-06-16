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

package ch.protonmail.android.mailbox.data

import ch.protonmail.android.api.models.MessageRecipient
import ch.protonmail.android.data.local.model.MessageSender
import ch.protonmail.android.mailbox.domain.model.Correspondent

internal fun Correspondent.toMessageSender() = MessageSender(name, address)

internal fun Correspondent.toMessageRecipient() = MessageRecipient(name, address)

internal fun List<Correspondent>.toMessageRecipients() = map { it.toMessageRecipient() }