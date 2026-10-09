package com.app.rumbo.core.di

import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.repository.PublicationRepository
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
}
