package ua.com.merchik.merchik.Activities

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ua.com.merchik.merchik.dataLayer.MainRepository
import ua.com.merchik.merchik.dataLayer.NameUIRepository
import ua.com.merchik.merchik.features.maps.data.repo.AddressRepositoryImpl
import ua.com.merchik.merchik.features.maps.data.repo.ItemsRepositoryImpl
import ua.com.merchik.merchik.features.maps.di.FromMaps
import ua.com.merchik.merchik.features.maps.di.FromWPdata
import ua.com.merchik.merchik.features.maps.domain.repositories.AddressRepository
import ua.com.merchik.merchik.features.maps.domain.repositories.ItemsRepository
import ua.com.merchik.merchik.features.maps.domain.scenarios.FromMapsScenario
import ua.com.merchik.merchik.features.maps.domain.scenarios.FromWPdataScenario
import ua.com.merchik.merchik.features.maps.domain.scenarios.MapScenario
import ua.com.merchik.merchik.features.maps.domain.usecases.BuildMapPointsUC
import ua.com.merchik.merchik.features.maps.domain.usecases.FilterAndSortItemsUC
import ua.com.merchik.merchik.features.maps.domain.usecases.MakePointUiUC

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


    @Provides
    fun provideItemsRepository(impl: ItemsRepositoryImpl): ItemsRepository = impl

    @Provides
    fun provideAddressRepository(impl: AddressRepositoryImpl): AddressRepository = impl


    @Provides
    fun provideFilterUC(repo: ItemsRepository) = FilterAndSortItemsUC(repo)

    @Provides
    @FromMaps
    fun provideFromMapsScenario(): MapScenario = FromMapsScenario()
    @Provides
    @FromWPdata
    fun provideFromWPdataScenario(): MapScenario = FromWPdataScenario()

    @Provides
    @FromMaps
    fun provideBuildFromMapsUC(
        @FromMaps scenario: MapScenario,
        addr: AddressRepository
    ) = BuildMapPointsUC(scenario, addr)

    @Provides
    @FromWPdata
    fun provideBuildFromWPdataUC(
        @FromWPdata scenario: MapScenario,
        addr: AddressRepository
    ) = BuildMapPointsUC(scenario, addr)

    // ← ВОТ ЭТИ ДВА — ключевые для твоей ошибки:
    @Provides
    @FromMaps
    fun provideMakeUiFromMaps(@FromMaps scenario: MapScenario): MakePointUiUC =
        MakePointUiUC(scenario)

    @Provides
    @FromWPdata
    fun provideMakeUiFromWPdata(@FromWPdata scenario: MapScenario): MakePointUiUC =
        MakePointUiUC(scenario)
}
