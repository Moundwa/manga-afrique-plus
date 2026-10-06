package app.mangaafricaplus.data

import android.content.Context
import org.json.JSONObject

class LibraryStore(context: Context) {
    private val prefs = context.getSharedPreferences("library", Context.MODE_PRIVATE)

    fun all(): Map<String, Shelf> {
        val raw = prefs.getString("shelves", "{}") ?: "{}"
        val json = JSONObject(raw)
        val map = linkedMapOf<String, Shelf>()
        json.keys().forEach { key ->
            runCatching { Shelf.valueOf(json.getString(key)) }.getOrNull()?.let { map[key] = it }
        }
        return map
    }

    fun set(id: String, shelf: Shelf?) {
        val json = JSONObject(prefs.getString("shelves", "{}") ?: "{}")
        if (shelf == null) json.remove(id) else json.put(id, shelf.name)
        prefs.edit().putString("shelves", json.toString()).apply()
    }
}
