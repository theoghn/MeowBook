package com.theo.meowbook.service

import com.theo.meowbook.api.CatApi
import com.theo.meowbook.api.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatService @Inject constructor(
    private val catApi: CatApi,
) {
    suspend fun getCatList(
        limit: Int,
        breedIds: List<String>,
        withBreedInfo: Boolean,
        orderBy: Order,
        page: Int
    ) = catApi.getCatList(
        limit = limit,
        breedIds = breedIds,
        withBreedInfo = withBreedInfo,
        orderBy = orderBy,
        page = page
    )

    suspend fun getBreeds() = catApi.getBreeds()

}