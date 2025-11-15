package com.theo.meowbook.model

data class CatDetails(
    val id: String,
    val url: String,
    val width: Int,
    val height: Int,
    val name: String,
    val origin: String,
    val temperament: String,
    val lifeSpan: String,
    val intelligence: Int,
    val affectionLevel: Int,
    val childFriendly: Int,
    val socialNeeds: Int,
    val wikipediaUrl: String?,
    val vetStreetUrl: String?,
    val description: String
)
