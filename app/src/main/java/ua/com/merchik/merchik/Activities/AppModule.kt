package ua.com.merchik.merchik.Activities

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideLogRepository(nameUIRepository: NameUIRepository): MainRepository {
        return MainRepository(nameUIRepository)
    }

    @Provides
    fun provideNameUIRepository(): NameUIRepository {
        return NameUIRepository()
    }

}
