package com.example.direktoripengguna

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private val _status = MutableStateFlow("Memuat data...")
    val status = _status.asStateFlow()
    val daftarUsers = mutableListOf<String>()
    var usersLoaded = false
    var usersLoading = false
    var postBerhasil = false

    fun ubahStatus(pesan: String) {
        _status.value = pesan
    }
}