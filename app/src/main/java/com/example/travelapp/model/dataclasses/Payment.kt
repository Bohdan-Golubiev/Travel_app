package com.example.travelapp.model.dataclasses

data class PaymentItem(
    val id: Int,
    val booking: String,
    val sum: String,
    val billing: String,
    val date: String
)