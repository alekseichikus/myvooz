package ru.createtogether.myVooz.user.di

import com.example.myvoozkotlin.user.api.UserApi
import com.example.myvoozkotlin.data.db.DbUtils
import com.example.myvoozkotlin.user.data.*
import com.example.myvoozkotlin.user.domain.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.realm.Realm

@Module
@InstallIn(ViewModelComponent::class)
object UserModule {

    @Provides
    fun provideAuthVkUseCase(userRepository: UserRepository): ChangeFullNameUseCase{
        return ChangeFullNameUseCaseImpl(userRepository)
    }

    @Provides
    fun provideEmptyAuditoryUseCase(userRepository: UserRepository): EmptyAuditoryUseCase{
        return EmptyAuditoryUseCaseImpl(userRepository)
    }

    @Provides
    fun provideChangeIdGroupUserUseCase(userRepository: UserRepository): ChangeIdGroupUserUseCase{
        return ChangeIdGroupUserUseCaseImpl(userRepository)
    }

    @Provides
    fun provideUserRepository(realm: Realm, dbUtils: DbUtils, userApi: UserApi) : UserRepository {
        return UserRepositoryImpl(realm, dbUtils, userApi)
    }
}