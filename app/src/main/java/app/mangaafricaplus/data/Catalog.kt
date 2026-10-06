package app.mangaafricaplus.data

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object Catalog {
    val featured = listOf(
        Title("local-aya", "Aya de Yopougon", "Bande dessinee d'Abouet et Oubrerie, vie quotidienne a Yopougon dans les annees 1970.", null, 7, null, "Cote d'Ivoire", "Selection Afrique+"),
        Title("local-akissi", "Akissi", "Recits d'Abouet et Sapin, une fillette d'Abidjan et ses betises.", null, null, null, "Cote d'Ivoire", "Selection Afrique+"),
        Title("local-kagiso", "Kwezi", "Super-heros sud-africain, comics Loyiso Mkize.", null, null, null, "Afrique du Sud", "Selection Afrique+"),
        Title("local-exile", "The Exiles", "Serie de science-fiction dessinee par l'Ivoirien Koffi Roger N'Guessan.", null, null, null, "Cote d'Ivoire", "Selection Afrique+"),
        Title("local-madiba", "Nelson Mandela (bio BD)", "Biographies dessinees, a completer avec vos editions locales.", null, null, null, "Afrique du Sud", "Selection Afrique+")
    )

    fun search(query: String): List<Title> {
        val q = query.trim()
        if (q.isEmpty()) return featured
        val local = featured.filter {
            it.name.contains(q, true) || it.origin.contains(q, true) || it.synopsis.contains(q, true)
        }
        val remote = runCatching { kitsu(q) }.getOrElse { emptyList() }
        return (local + remote).distinctBy { it.id }
    }

    private fun kitsu(query: String): List<Title> {
        val url = "https://kitsu.io/api/edge/manga?filter[text]=" +
            URLEncoder.encode(query, "UTF-8") + "&page[limit]=12"
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.api+json")
            setRequestProperty("User-Agent", "MangaAfriquePlus/0.2")
            connectTimeout = 12000
            readTimeout = 12000
        }
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()
        val data = JSONObject(body).getJSONArray("data")
        val out = ArrayList<Title>(data.length())
        for (i in 0 until data.length()) {
            val item = data.getJSONObject(i)
            val attr = item.getJSONObject("attributes")
            val poster = attr.optJSONObject("posterImage")?.optString("small")?.takeIf { it.isNotBlank() }
            out += Title(
                id = "kitsu-" + item.getString("id"),
                name = attr.optString("canonicalTitle", "Sans titre"),
                synopsis = attr.optString("synopsis", "").ifBlank { "Pas de resume." },
                posterUrl = poster,
                chapters = attr.optInt("chapterCount").takeIf { it > 0 },
                rating = attr.optString("averageRating").takeIf { it.isNotBlank() },
                origin = "Catalogue public Kitsu",
                source = "Kitsu"
            )
        }
        return out
    }
}
