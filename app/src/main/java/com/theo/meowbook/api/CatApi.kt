package com.theo.meowbook.api

import com.theo.meowbook.model.Breed
import com.theo.meowbook.model.CatDetails
import com.theo.meowbook.model.CatListItem
import net.mready.apiclient.ApiClient
import net.mready.apiclient.get
import javax.inject.Inject
import javax.inject.Singleton

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
            "has_breeds" to withBreedInfo,
            "order" to orderBy.id,
            "page" to page
        )
    ) { json ->
        json.arrayOrNull.orEmpty().map {
            CatListItem(
                id = it["id"].string,
                url = it["url"].string,
                height = it["height"].int,
                width = it["width"].int,
                hasBreedInfo = it["breeds"].arrayOrNull?.isNotEmpty() == true
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

    suspend fun getCatDetails(imageId: String): CatDetails = client.get(
        endpoint = "v1/images/$imageId"
    ) { json ->
        CatDetails(
            id = json["id"].string,
            url = json["url"].string,
            height = json["height"].int,
            width = json["width"].int,
            name = json["breeds"][0]["name"].string,
            origin = json["breeds"][0]["origin"].string,
            temperament = json["breeds"][0]["temperament"].string,
            lifeSpan = json["breeds"][0]["life_span"].string,
            intelligence = json["breeds"][0]["intelligence"].int,
            affectionLevel = json["breeds"][0]["affection_level"].int,
            childFriendly = json["breeds"][0]["child_friendly"].int,
            socialNeeds = json["breeds"][0]["social_needs"].int,
            wikipediaUrl = json["breeds"][0]["wikipedia_url"].stringOrNull,
            vetStreetUrl = json["breeds"][0]["vetstreet_url"].stringOrNull,
            description = json["breeds"][0]["description"].string
        )
    }
}