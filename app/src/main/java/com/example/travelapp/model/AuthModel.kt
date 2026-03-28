package com.example.travelapp.model

data class AuthModel(
    val email: String = "",
    val name: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLogin: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)