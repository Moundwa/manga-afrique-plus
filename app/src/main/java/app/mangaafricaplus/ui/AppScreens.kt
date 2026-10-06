package app.mangaafricaplus.ui

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.mangaafricaplus.data.Catalog
import app.mangaafricaplus.data.LibraryStore
import app.mangaafricaplus.data.Shelf
import app.mangaafricaplus.data.Title
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CatalogViewModel(app: Application) : AndroidViewModel(app) {
    private val store = LibraryStore(app)
    var query by mutableStateOf("")
    var results by mutableStateOf(Catalog.featured)
    var loading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var selected by mutableStateOf<Title?>(null)
    var tab by mutableStateOf(0)
    var shelves by mutableStateOf(store.all())
    private val known = Catalog.featured.associateBy { it.id }.toMutableMap()

    fun search() {
        loading = true
        error = null
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { Catalog.search(query) }
                .onSuccess { list ->
                    list.forEach { known[it.id] = it }
                    results = list
                    loading = false
                }
                .onFailure {
                    error = it.message
                    loading = false
                }
        }
    }

    fun mark(title: Title, shelf: Shelf?) {
        known[title.id] = title
        store.set(title.id, shelf)
        shelves = store.all()
    }

    fun library(): List<Pair<Title, Shelf>> =
        shelves.mapNotNull { (id, shelf) -> known[id]?.let { it to shelf } }
}

@Composable
fun MangaAfriqueTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF1B7F4E),
            background = Color(0xFF142018),
            surface = Color(0xFF1C2A22),
            onPrimary = Color(0xFFF4E7C5),
            onBackground = Color(0xFFF4E7C5),
            onSurface = Color(0xFFF4E7C5)
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: CatalogViewModel = viewModel()) {
    val title = vm.selected
    if (title != null) {
        DetailScreen(title, vm.shelves[title.id], onBack = { vm.selected = null }, onMark = { vm.mark(title, it) })
        return
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Manga Afrique+") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = vm.tab == 0, onClick = { vm.tab = 0 }, label = { Text("Explorer") }, icon = {})
                NavigationBarItem(selected = vm.tab == 1, onClick = { vm.tab = 1 }, label = { Text("Bibliotheque") }, icon = {})
            }
        }
    ) { padding ->
        if (vm.tab == 0) Explore(padding, vm) else Library(padding, vm)
    }
}

@Composable
private fun Explore(padding: PaddingValues, vm: CatalogViewModel) {
    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Selection Afrique+, plus recherche manga publique (Kitsu).", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = vm.query,
            onValueChange = { vm.query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Titre, auteur, pays") },
            singleLine = true
        )
        Button(onClick = { vm.search() }, enabled = !vm.loading) {
            Text(if (vm.loading) "Recherche..." else "Chercher")
        }
        vm.error?.let { Text("Erreur reseau : $it") }
        TitleList(vm.results) { vm.selected = it }
    }
}

@Composable
private fun Library(padding: PaddingValues, vm: CatalogViewModel) {
    val rows = vm.library()
    if (rows.isEmpty()) {
        Text("Rien sur vos rayons. Ouvrez un titre et marquez-le.", modifier = Modifier.padding(padding).padding(16.dp))
        return
    }
    LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(rows, key = { it.first.id }) { (title, shelf) ->
            TitleRow(title, shelf.name) { vm.selected = title }
        }
    }
}

@Composable
private fun TitleList(items: List<Title>, onClick: (Title) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items, key = { it.id }) { TitleRow(it, it.origin, onClick = { onClick(it) }) }
    }
}

@Composable
private fun TitleRow(title: Title, subtitle: String, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp)) {
        Text(title.name, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, style = MaterialTheme.typography.bodySmall)
        title.rating?.let { Text("Note $it") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailScreen(title: Title, shelf: Shelf?, onBack: () -> Unit, onMark: (Shelf?) -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(title.name) }, navigationIcon = { TextButton(onClick = onBack) { Text("Retour") } })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title.origin + " · " + title.source)
            title.chapters?.let { Text("$it chapitres") }
            Text(title.synopsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Shelf.entries.forEach { s ->
                    FilterChip(selected = shelf == s, onClick = { onMark(if (shelf == s) null else s) }, label = { Text(s.name) })
                }
            }
            Text("PLAN = a lire, READING = en cours, DONE = lu. Stocke sur l'appareil.")
        }
    }
}
