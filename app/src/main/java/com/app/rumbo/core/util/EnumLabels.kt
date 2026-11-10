package com.app.rumbo.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.app.rumbo.domain.model.Achievement
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.FeedSort
import com.app.rumbo.domain.model.Level
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.model.RoutePriority
import com.app.rumbo.domain.model.Transport

val Category.label: String
    @Composable
    @ReadOnlyComposable
    get() = stringResource(labelRes)

val PriceLevel.label: String
    @Composable
    @ReadOnlyComposable
    get() = stringResource(labelRes)

val FeedSort.label: String
    @Composable
    @ReadOnlyComposable
    get() = stringResource(labelRes)

val Transport.label: String
    @Composable
    @ReadOnlyComposable
    get() = stringResource(labelRes)

val RoutePriority.label: String
    @Composable
    @ReadOnlyComposable
    get() = stringResource(labelRes)

val PublicationStatus.label: String
    @Composable
    @ReadOnlyComposable
    get() = stringResource(labelRes)

val Level.displayName: String
    @Composable
    @ReadOnlyComposable
    get() = stringResource(displayNameRes)

val Achievement.displayName: String
    @Composable
    @ReadOnlyComposable
    get() = stringResource(displayNameRes)

val Achievement.description: String
    @Composable
    @ReadOnlyComposable
    get() = stringResource(descriptionRes)
