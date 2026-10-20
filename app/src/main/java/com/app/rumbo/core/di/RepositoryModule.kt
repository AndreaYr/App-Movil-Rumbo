package com.app.rumbo.core.di

import com.app.rumbo.data.repository.InMemoryNotificationRepository
import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.data.repository.InMemoryUserRepository
import com.app.rumbo.domain.repository.NotificationRepository
import com.app.rumbo.domain.repository.PublicationRepository
import com.app.rumbo.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Une cada interfaz del dominio con su implementación en la capa data. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindPublicationRepository(impl: InMemoryPublicationRepository): PublicationRepository

    @Binds
    abstract fun bindUserRepository(impl: InMemoryUserRepository): UserRepository

    @Binds
    abstract fun bindNotificationRepository(impl: InMemoryNotificationRepository): NotificationRepository
}
