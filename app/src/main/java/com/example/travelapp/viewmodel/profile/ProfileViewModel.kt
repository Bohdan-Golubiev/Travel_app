package com.example.travelapp.viewmodel.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    var isSettingsMenuOpen by mutableStateOf(false)
        private set

    fun openSettingsMenu()  { isSettingsMenuOpen = true  }
    fun closeSettingsMenu() { isSettingsMenuOpen = false }
}