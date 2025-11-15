package com.theo.meowbook.api

import com.theo.meowbook.model.Breed
import com.theo.meowbook.model.CatListItem
import net.mready.apiclient.ApiClient
import net.mready.apiclient.get
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map
import kotlin.collections.orEmpty

enum class Order(
    val id: String,
    val displayName: String
) {
    ASC("ASC", "Date Asc"),
    DESC("DESC", "Date Desc"),
    RANDOM("RAND", "Random")
}

@Singleton
class CatApi @Inject constructor(private val client: ApiClient) {

    suspend fun getCatList(
        limit: Int,
        page: Int,
        breedIds: List<String>,
        withBreedInfo: Boolean,
        orderBy: Order
    ): List<CatListItem> = client.get(
        endpoint = "v1/images/search",
        query = mapOf(
            "limit" to limit.toString(),
            "breed_ids" to breedIds.joinToString(","),
            "with_breeds" to withBreedInfo,
            "order" to orderBy.id,
            "page" to page
        )
    ) { json ->
        json.arrayOrNull.orEmpty().map {
            CatListItem(
                id = it["id"].string,
                url = it["url"].string,
                height = it["height"].int,
                width = it["width"].int
            )
        }
    }

    suspend fun getBreeds(): List<Breed> = client.get(
        endpoint = "v1/breeds"
    ) { json ->
        json.arrayOrNull.orEmpty().map {
            Breed(
                id = it["id"].string,
                name = it["name"].string,
            )
        }
    }
}