package com.example.aware.utils

sealed class SummaryItem {
    data class Category(val title: String) : SummaryItem()
    data class Entry(val text: String) : SummaryItem()
}
