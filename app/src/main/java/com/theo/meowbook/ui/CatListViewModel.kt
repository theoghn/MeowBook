package com.theo.meowbook.ui

import com.theo.meowbook.api.Order
import com.theo.meowbook.model.Breed
import com.theo.meowbook.model.CatListItem
import com.theo.meowbook.repository.CatRepository
import com.theo.meowbook.service.CatService
import com.theo.meowbook.utils.BaseViewModel
import com.theo.meowbook.utils.allSuccess
import com.theo.meowbook.utils.data
import com.theo.meowbook.utils.dataFlowOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CatListViewModel @Inject constructor(
    private val catService: CatService,
    private val catRepository: CatRepository
) : BaseViewModel<Unit>() {
    val catListFLow = dataFlowOf<List<CatListItem>>()
    val breedsFlow = dataFlowOf<List<Breed>>()

    val isRefreshing = MutableStateFlow(false)
    val currentPage = MutableStateFlow(0)
    val isPageLoading = MutableStateFlow(false)
    private val isMaxPage = MutableStateFlow(false)
    val pageSize = 25

    private var filterJob: Job? = null

    init {
        loadBreeds()
        loadCats()
    }

    fun onPullToRefresh(
        breedIds: List<String>,
        withBreedInfo: Boolean,
        orderBy: Order
    ) {
        isRefreshing.value = true
        launch {
            loadCats(
                breedIds = breedIds,
                withBreedInfo = withBreedInfo,
                orderBy = orderBy
            )
            isRefreshing.value = false
        }
    }

    fun loadBreeds() {
        breedsFlow.execute {
            val breeds = catService.getBreeds()
            setValue(breeds)
        }

    }

    fun loadCats(
        breedIds: List<String> = emptyList(),
        withBreedInfo: Boolean = false,
        orderBy: Order = Order.RANDOM
    ) {
        currentPage.value = 0
        isMaxPage.value = false

        filterJob?.cancel()
        filterJob = catListFLow.execute {

            val catList = catRepository.getCatList(
                limit = pageSize,
                page = currentPage.value,
                breedIds = breedIds,
                withBreedInfo = withBreedInfo,
                orderBy = orderBy
            ).getOrThrow()

            setValue(catList)
        }
    }

    fun loadNextPage(
        breedIds: List<String> = emptyList(),
        withBreedInfo: Boolean = false,
        orderBy: Order = Order.RANDOM
    ) {
        if (isPageLoading.value || isMaxPage.value) return
        if (!allSuccess(catListFLow.value)) return

        isPageLoading.value = true

        currentPage.value += 1

        filterJob?.cancel()
        filterJob = launch {
            try {
                val newCatList = catService.getCatList(
                    limit = pageSize,
                    page = currentPage.value,
                    breedIds = breedIds,
                    withBreedInfo = withBreedInfo,
                    orderBy = orderBy
                )

                if (newCatList.isEmpty()) {
                    isMaxPage.value = true
                    return@launch
                }

                val updatedList = catListFLow.value.data + newCatList

                catListFLow.executeIn(
                    scope = this,
                    withLoading = false
                ) {
                    setValue(updatedList)
                }

                isPageLoading.value = false
            } catch (e: Exception) {
                isPageLoading.value = false
                isMaxPage.value = true
            }
        }
    }
}