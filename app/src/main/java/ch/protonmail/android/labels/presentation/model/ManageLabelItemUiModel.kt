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

package ch.protonmail.android.labels.presentation.model

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.protonmail.android.labels.presentation.ui.LabelsActionSheet

data class ManageLabelItemUiModel(
    val labelId: String,
    @DrawableRes val iconRes: Int,
    val title: String? = null, // for item custom titles e.g. "Lablel123"
    @StringRes val titleRes: Int? = null, // for standard titles e.g. "Inbox"
    @ColorInt val colorInt: Int = Color.BLACK,
    val isChecked: Boolean? = null,
    val labelType: Int = LabelsActionSheet.Type.LABEL.typeInt
)