package com.theo.meowbook.repository

import com.theo.meowbook.api.CatApi
import com.theo.meowbook.api.Order
import com.theo.meowbook.model.CatListItem
import com.theo.meowbook.persistence.CatDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatRepository @Inject constructor(
    private val catApi: CatApi,
    private val catDao: CatDao
) {
    suspend fun getCatList(
        limit: Int,
        page: Int,
        breedIds: List<String>,
        withBreedInfo: Boolean,
        orderBy: Order
    ): Result<List<CatListItem>> {
        return try {
            val networkResult = catApi.getCatList(limit, page, breedIds, withBreedInfo, orderBy)

            if (page == 0 && networkResult.isNotEmpty()) {
                catDao.clearAllCats()
                catDao.insertCats(networkResult)
            }
            Result.success(networkResult)
        } catch (e: Exception) {
            if (page == 0) {
                val cachedCats = catDao.getAllCats()
                if (cachedCats.isNotEmpty()) {

                    Result.success(cachedCats)
                } else {
                    Result.failure(e)
                }
            } else {
                Result.failure(e)
            }
        }
    }
}
