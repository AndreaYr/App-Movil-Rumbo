package com.app.rumbo.core.di

import com.app.rumbo.data.remote.MapboxPlaceSearchRepository
import com.app.rumbo.data.repository.InMemoryActivityRepository
import com.app.rumbo.data.repository.remote.AuthorRepositoryImpl
import com.app.rumbo.data.repository.remote.CommentRepositoryImpl
import com.app.rumbo.data.repository.remote.NotificationRepositoryImpl
import com.app.rumbo.data.repository.remote.PhotoRepositoryImpl
import com.app.rumbo.data.repository.remote.PublicationRepositoryImpl
import com.app.rumbo.data.repository.remote.SavedRouteRepositoryImpl
import com.app.rumbo.data.repository.remote.UserRepositoryImpl
import com.app.rumbo.domain.repository.ActivityRepository
import com.app.rumbo.domain.repository.AuthorRepository
import com.app.rumbo.domain.repository.CommentRepository
import com.app.rumbo.domain.repository.NotificationRepository
import com.app.rumbo.domain.repository.PhotoRepository
import com.app.rumbo.domain.repository.PlaceSearchRepository
import com.app.rumbo.domain.repository.PublicationRepository
import com.app.rumbo.domain.repository.SavedRouteRepository
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
    abstract fun bindPublicationRepository(impl: PublicationRepositoryImpl): PublicationRepository

    @Binds
    abstract fun bindCommentRepository(impl: CommentRepositoryImpl): CommentRepository

    @Binds
    abstract fun bindAuthorRepository(impl: AuthorRepositoryImpl): AuthorRepository

    @Binds
    abstract fun bindPhotoRepository(impl: PhotoRepositoryImpl): PhotoRepository

    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    abstract fun bindActivityRepository(impl: InMemoryActivityRepository): ActivityRepository

    @Binds
    abstract fun bindSavedRouteRepository(impl: SavedRouteRepositoryImpl): SavedRouteRepository

    @Binds
    abstract fun bindPlaceSearchRepository(impl: MapboxPlaceSearchRepository): PlaceSearchRepository
}
