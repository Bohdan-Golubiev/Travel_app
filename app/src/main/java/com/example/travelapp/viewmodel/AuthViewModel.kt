package com.example.travelapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.model.AuthModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AuthModel())
    val uiState: StateFlow<AuthModel> = _uiState.asStateFlow()

    // ─── UI events ────────────────────────────────────────────────────────────

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onPasswordVisibilityToggle() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onTabSwitch(isLogin: Boolean) {
        _uiState.update { it.copy(isLogin = isLogin, errorMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ─── Auth actions ─────────────────────────────────────────────────────────

    fun signInOrRegister(onSuccess: (FirebaseUser) -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = if (state.isLogin) {
                    auth.signInWithEmailAndPassword(state.email.trim(), state.password).await()
                } else {
                    auth.createUserWithEmailAndPassword(state.email.trim(), state.password).await()
                }
                result.user?.let { onSuccess(it) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = parseFirebaseError(e.message)) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signInWithCredential(credential: AuthCredential, onSuccess: (FirebaseUser) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = auth.signInWithCredential(credential).await()
                result.user?.let { onSuccess(it) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.localizedMessage ?: "Google Sign-In failed") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun sendPasswordReset(onSent: () -> Unit, onError: () -> Unit) {
        val email = _uiState.value.email
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter your email first") }
            return
        }
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email.trim()).await()
                onSent()
            } catch (e: Exception) {
                onError()
            }
        }
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private fun parseFirebaseError(message: String?): String = when {
        message == null -> "Authentication failed"
        "email address is already in use" in message -> "This email is already registered"
        "no user record" in message -> "Invalid email"
        "password is invalid" in message -> "Invalid password"
        "badly formatted" in message -> "Invalid email format"
        "at least 6 characters" in message -> "Password must be at least 6 characters"
        "network error" in message -> "Network error. Check your connection"
        else -> "Authentication failed"
    }
}