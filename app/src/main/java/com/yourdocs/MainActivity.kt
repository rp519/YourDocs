package com.yourdocs

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.yourdocs.data.preferences.UserPreferencesRepository
import com.yourdocs.ui.navigation.YourDocsNavigation
import com.yourdocs.ui.theme.ThemeMode
import com.yourdocs.ui.theme.YourDocsTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by userPreferencesRepository.themeMode
                .collectAsState(initial = ThemeMode.SYSTEM)

            YourDocsTheme(themeMode = themeMode) {
                YourDocsNavigation()
            }
        }
    }
}
