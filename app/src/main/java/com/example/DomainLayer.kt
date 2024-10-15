package com.example






data class Pagination(
    val last_visible_page: Int,
    val has_next_page: Boolean,
    val current_page: Int,
    val items: Items
)

data class Items(
    val count: Int,
    val total: Int,
    val per_page: Int
)

data class Manga(
    val mal_id: Int,
    val id: Int,
    val url: String,
    val title: String,
    val synopsis: String?,
    val score: Float?,
    val members: Int,
    val images: Images,
    val genres: List<Genre>,
    val authors: List<Author>,
    val cast: List<CastMember>? // Updated to include cast members
)

data class Images(
    val jpg: ImageDetails,
    val webp: ImageDetails
)

data class ImageDetails(
    val image_url: String,
    val small_image_url: String,
    val large_image_url: String
)

data class Genre(
    val mal_id: Int,
    val name: String
)

data class Author(
    val mal_id: Int,
    val name: String
)

data class MangaResponse(
    val pagination: Pagination,
    val data: List<Manga>
)

// CastMember now includes role and voice actors
data class CastMember(
    val name: String,
    val image_url: String, // Cast image URL
    val role: String?, // Role in the anime/manga (e.g., main, supporting)
    val voice_actors: List<VoiceActor>? // List of voice actors with language
)

data class VoiceActor(
    val name: String,
    val language: String
)
