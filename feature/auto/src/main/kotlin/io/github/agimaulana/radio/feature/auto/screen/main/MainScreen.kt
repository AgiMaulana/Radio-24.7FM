package io.github.agimaulana.radio.feature.auto.screen.main

import androidx.annotation.OptIn
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.annotations.ExperimentalCarApi
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarIcon
import androidx.car.app.model.GridItem
import androidx.car.app.model.GridTemplate
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.SectionedItemList
import androidx.car.app.model.Tab
import androidx.car.app.model.TabContents
import androidx.car.app.model.TabTemplate
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import io.github.agimaulana.radio.core.car.state.collectAsStateWithLifecycle
import io.github.agimaulana.radio.core.car.viewmodel.hilt.ext.viewModel
import io.github.agimaulana.radio.feature.auto.R
import io.github.agimaulana.radio.feature.auto.screen.main.MainScreenViewModel.Action

class MainScreen(
    carContext: CarContext,
) : Screen(carContext) {

    private val viewModel: MainScreenViewModel by viewModel()
    private val uiState by collectAsStateWithLifecycle(viewModel.uiState)

    init {
        viewModel.init()
    }

    override fun onGetTemplate(): Template {
        return createTabTemplate()
    }

    private fun createTabTemplate(): TabTemplate {
        val tabCallback = object : TabTemplate.TabCallback {
            override fun onTabSelected(tabContentId: String) {
                super.onTabSelected(tabContentId)

            }
        }
        return TabTemplate.Builder(tabCallback)
            .setHeaderAction(androidx.car.app.model.Action.APP_ICON)
            .setActiveTabContentId("pinned")
            .addTab(
                Tab.Builder()
                    .setContentId("pinned")
                    .setTitle("Pinned")
                    .setIcon(
                        CarIcon.Builder(
                            IconCompat.createWithResource(
                                carContext,
                                R.drawable.ic_playing_wave
                            )
                        )
                            .build()
                    )
                    .build()
            ).addTab(
                Tab.Builder()
                    .setContentId("all")
                    .setTitle("All Stations")
                    .setIcon(CarIcon.APP_ICON)
                    .build()
            )
            .setTabContents(
                TabContents.Builder(createStationListTemplate()).build()
            )
            .build()
    }

    @OptIn(ExperimentalCarApi::class)
    private fun createStationListTemplate(): GridTemplate {
        return GridTemplate.Builder()
            .applyAllStations()
            .addAction(createAction())
            .build()
    }

    private fun GridTemplate.Builder.applyAllStations() = apply {
        when {
            uiState.isLoading -> {
                setLoading(true)
            }

            uiState.allStations.isNotEmpty() ->  {
//                addSectionedList(createAllStationsSection())
                setSingleList(createAllStationItemList())
            }
        }
    }

    private fun createAllStationsSection(): SectionedItemList {
        return SectionedItemList.create(
            createAllStationItemList(),
            "All Stations"
        )
    }

    private fun createAllStationItemList() : ItemList {
        return ItemList.Builder()
            .apply {
                uiState.allStations.forEachIndexed { index, station ->
                    addItem(createStationRowItem(station))
//                    setOnItemsVisibilityChangedListener { first, last ->
                        if (station.imageBitmap == null) {
                            viewModel.onAction(Action.LoadImage(carContext, station))
                        }
//                    }
                }
            }
            .build()
    }

    private fun createStationRowItem(station: MainScreenViewModel.UiState.Station): GridItem {
        val builder = GridItem.Builder()
            .setTitle(station.name)

        if (station.imageBitmap != null) {
            builder.setImage(
                CarIcon.Builder(
                    IconCompat.createWithBitmap(station.imageBitmap)
                ).build(),
                GridItem.IMAGE_TYPE_LARGE
            )
            builder.setOnClickListener {

            }
        } else {
            builder.setLoading(true)
        }

        if (station.genre.isNotEmpty()) {
            builder.setText(station.genre)
        }

        return builder.build()
    }

    private fun createAction(): androidx.car.app.model.Action {
        return androidx.car.app.model.Action.Builder()
            .setIcon(
                CarIcon.Builder(
                    IconCompat.createWithResource(carContext, R.drawable.ic_playing_wave)
                ).setTint(
                    CarColor.createCustom(
                        0xFFFFFFFF.toInt(),
                        0xFFFFFFFF.toInt()
                    )
                )
                    .build()
            )
            .setBackgroundColor(
                CarColor.createCustom(
                    0xFFFF69B4.toInt(),
                    0xFFFF69B4.toInt()
                )
            )
            .build()
    }
}
