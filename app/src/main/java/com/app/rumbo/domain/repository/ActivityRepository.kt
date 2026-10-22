package com.app.rumbo.domain.repository

import com.app.rumbo.domain.model.ActivityStats
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    /** Actividad acumulada del usuario que inició sesión; se vuelve a emitir cuando cambia. */
    fun observeActivity(): Flow<ActivityStats>
}
