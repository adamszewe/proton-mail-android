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

package ch.protonmail.android.di

import ch.protonmail.android.core.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.humanverification.presentation.CaptchaBaseUrl

@Module
@InstallIn(SingletonComponent::class)
object ConfigurableParametersModule {

    @BaseUrl
    @Provides
    fun provideBaseUrl(): String = Constants.ENDPOINT_URI

    @Provides
    @CaptchaBaseUrl
    fun provideCaptchaBaseUrl(): String = Constants.BASE_URL

    @Provides
    @AlternativeApiPins
    fun alternativeApiPins() = me.proton.core.network.data.di.Constants.ALTERNATIVE_API_SPKI_PINS

    @Provides
    @DefaultApiPins
    fun defaultApiPins() = me.proton.core.network.data.di.Constants.DEFAULT_SPKI_PINS
}
