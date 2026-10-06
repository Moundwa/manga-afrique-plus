package app.mangaafricaplus.data

enum class Shelf { PLAN, READING, DONE }

data class Title(
    val id: String,
    val name: String,
    val synopsis: String,
    val posterUrl: String?,
    val chapters: Int?,
    val rating: String?,
    val origin: String,
    val source: String
)
