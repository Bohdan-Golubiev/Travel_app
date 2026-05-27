package com.example.travelapp.viewmodel.profile

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.travelapp.notification.NotificationPrefs
import com.example.travelapp.notification.TravelAlarmManager

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    var isSettingsMenuOpen by mutableStateOf(false)
        private set

    fun openSettingsMenu()  { isSettingsMenuOpen = true  }
    fun closeSettingsMenu() { isSettingsMenuOpen = false }

    var notificationsEnabled by mutableStateOf(NotificationPrefs.areEnabled(application))
        private set

    fun toggleNotifications(enabled: Boolean) {
        NotificationPrefs.setEnabled(getApplication(), enabled)
        notificationsEnabled = enabled

        if (enabled) {
            TravelAlarmManager.restoreAllSavedAlarms(getApplication())
        } else {
            TravelAlarmManager.cancelAllSystemAlarms(getApplication())
        }
    }
}