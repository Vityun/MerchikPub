package ua.com.merchik.merchik.features.maps.presentation.viewModels

import dagger.hilt.android.lifecycle.HiltViewModel
import ua.com.merchik.merchik.features.maps.di.FromWPdata
import ua.com.merchik.merchik.features.maps.domain.usecases.BuildMapPointsUC
import ua.com.merchik.merchik.features.maps.domain.usecases.FilterAndSortItemsUC
import ua.com.merchik.merchik.features.maps.domain.usecases.MakePointUiUC
import ua.com.merchik.merchik.features.maps.presentation.MapActionsBridge
import javax.inject.Inject


@HiltViewModel
class MapFromWPdataViewModel @Inject constructor(
    filterUC: FilterAndSortItemsUC,
    @FromWPdata private val buildUC: BuildMapPointsUC,
    @FromWPdata private val makeUiUC: MakePointUiUC
) : BaseMapViewModel(
    filterUC = filterUC,
    buildUC = buildUC,
    makeUiUC = makeUiUC,
    radiusMeters = 0.0
)
