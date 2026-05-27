package com.example.travelapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.travelapp.data.entity.UserEntity
import com.example.travelapp.data.repository.UserRepository
import com.example.travelapp.db.TravelDB
import com.example.travelapp.ui.theme.TravelAppTheme
import com.example.travelapp.utils.LocaleManager
import com.example.travelapp.utils.LocaleProvider
import com.example.travelapp.view.AuthScreen
import com.example.travelapp.view.HomeScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.core.content.edit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("is_first_run", true)

        if (isFirstRun) {
            FirebaseAuth.getInstance().signOut()
            prefs.edit { putBoolean("is_first_run", false) }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color(0xFF0D1B2A).toArgb()

        setContent {
            TravelAppTheme {
                TravelAuthApp()
            }
        }
    }
}

@Composable
fun TravelAuthApp() {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val repository = remember {
        UserRepository(TravelDB.getInstance(context))
    }
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    val scope = rememberCoroutineScope()

    var currentLocale by remember {
        mutableStateOf(LocaleManager.getSavedLocale(context))
    }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(workDataOf("userId" to user.uid))
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork( // юник для одной на юзера + киип
                "sync_${user.uid}",
                ExistingWorkPolicy.KEEP,
                syncRequest
            )
        }
    }

    LocaleProvider(locale = currentLocale) {
        if (currentUser != null) {
            HomeScreen(
                user = currentUser!!,
                onSignOut = { auth.signOut(); currentUser = null },
                onLocaleChange = { newLocale ->
                    LocaleManager.saveLocale(context, newLocale)
                    currentLocale = newLocale
                }
            )
        } else {
            AuthScreen(onAuthSuccess = { firebaseUser ->
                scope.launch {
                    repository.saveUser(
                        UserEntity(
                            id = firebaseUser.uid,
                            name = firebaseUser.displayName ?: "",
                            email = firebaseUser.email ?: ""
                        )
                    )
                }
                currentUser = firebaseUser
            })
        }
    }
}