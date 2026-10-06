package app.mangaafricaplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.mangaafricaplus.ui.HomeScreen
import app.mangaafricaplus.ui.MangaAfriqueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MangaAfriqueTheme {
                HomeScreen()
            }
        }
    }
}
