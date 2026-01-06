package ua.com.merchik.merchik.features.maps.presentation.viewModels

import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.features.maps.di.FromMaps
import ua.com.merchik.merchik.features.maps.domain.usecases.BuildMapPointsUC
import ua.com.merchik.merchik.features.maps.domain.usecases.FilterAndSortItemsUC
import ua.com.merchik.merchik.features.maps.domain.usecases.MakePointUiUC
import ua.com.merchik.merchik.features.maps.presentation.MapActionsBridge
import javax.inject.Inject


@HiltViewModel
class MapFromMapsViewModel @Inject constructor(
    filterUC: FilterAndSortItemsUC,
    @FromMaps private val buildUC: BuildMapPointsUC,
    @FromMaps private val makeUiUC: MakePointUiUC
) : BaseMapViewModel(
    filterUC = filterUC,
    buildUC = buildUC,
    makeUiUC = makeUiUC
)
