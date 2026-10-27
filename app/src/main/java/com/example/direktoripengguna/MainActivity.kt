package com.example.direktoripengguna

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var listView: ListView
    private lateinit var btnCobaLagi: Button
    private lateinit var adapter: ArrayAdapter<String>

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        listView = findViewById(R.id.listUser)
        btnCobaLagi = findViewById(R.id.btnCobaLagi)

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            mutableListOf()
        )
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val userId = position + 1
            muatPostinganUser(userId)
        }

        // Menampilkan status dari ViewModel
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.status.collect { pesan ->
                    tvStatus.text = pesan
                }
            }
        }

        findViewById<Button>(R.id.btnKirimPostingan)
            .setOnClickListener {
                kirimPostingan()
            }

        btnCobaLagi.setOnClickListener {
            muatUsers()
        }

        findViewById<Button>(R.id.btnHapusPostingan).setOnClickListener {
            hapusPostingan(1)
        }

        // Tampilkan data yang tersimpan atau muat jika belum ada
        if (viewModel.usersLoaded) {
            tampilkanUsers()
        } else {
            muatUsers()
        }
    }

    private fun tampilkanUsers() {
        adapter.clear()
        adapter.addAll(viewModel.daftarUsers)
        adapter.notifyDataSetChanged()
    }

    private fun muatUsers() {
        if (viewModel.usersLoading) return

        viewModel.usersLoading = true
        viewModel.ubahStatus("Memuat data...")
        btnCobaLagi.visibility = View.GONE
        listView.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val daftar = RetrofitClient.api.getUsers()

                viewModel.daftarUsers.clear()
                viewModel.daftarUsers.addAll(
                    daftar.map { "${it.name} (@${it.username})" }
                )
                viewModel.usersLoaded = true

                tampilkanUsers()

                if (!viewModel.postBerhasil) {
                    viewModel.ubahStatus("${daftar.size} pengguna dimuat")
                }

            } catch (e: HttpException) {
                viewModel.ubahStatus("Server error: HTTP ${e.code()}")
                btnCobaLagi.visibility = View.VISIBLE

            } catch (e: IOException) {
                viewModel.ubahStatus("Tidak ada koneksi internet")
                btnCobaLagi.visibility = View.VISIBLE

            } catch (e: Exception) {
                viewModel.ubahStatus(
                    "Terjadi kesalahan: ${e.localizedMessage ?: "Tidak diketahui"}"
                )
                btnCobaLagi.visibility = View.VISIBLE

            } finally {
                viewModel.usersLoading = false
            }
        }
    }

    private fun kirimPostingan() {
        lifecycleScope.launch {
            try {
                viewModel.ubahStatus("Mengirim postingan...")

                val baru = PostBaru(
                    title = "Belajar Retrofit",
                    body = "Contoh POST dari aplikasi",
                    userId = 1
                )

                val hasil = RetrofitClient.api.buatPost(baru)

                viewModel.postBerhasil = true
                viewModel.ubahStatus(
                    "Tersimpan dengan id ${hasil.id}"
                )

            } catch (e: HttpException) {
                viewModel.ubahStatus(
                    "Gagal menyimpan: HTTP ${e.code()}"
                )

            } catch (e: IOException) {
                viewModel.ubahStatus("Tidak ada koneksi internet")

            } catch (e: Exception) {
                viewModel.ubahStatus(
                    "Terjadi kesalahan: ${e.localizedMessage ?: "Tidak diketahui"}"
                )
            }
        }
    }

    private fun hapusPostingan(id: Int) {
        lifecycleScope.launch {
            try {
                viewModel.ubahStatus("Menghapus postingan...")

                val response = RetrofitClient.api.hapusPost(id)

                if (response.code() == 200) {
                    viewModel.ubahStatus("Postingan berhasil dihapus")
                } else {
                    viewModel.ubahStatus(
                        "Gagal menghapus postingan. Kode HTTP: ${response.code()}"
                    )
                }
            } catch (e: HttpException) {
                viewModel.ubahStatus(
                    "Gagal menghapus postingan. Kode HTTP: ${e.code()}"
                )
            } catch (e: IOException) {
                viewModel.ubahStatus("Tidak ada koneksi internet")
            } catch (e: Exception) {
                viewModel.ubahStatus(
                    "Terjadi kesalahan: ${e.localizedMessage ?: "Tidak diketahui"}"
                )
            }
        }
    }

    private fun muatPostinganUser(userId: Int) {
        lifecycleScope.launch {
            try {
                viewModel.ubahStatus("Memuat postingan pengguna $userId...")

                val posts = RetrofitClient.api.getPostsByUser(userId)

                if (posts.isEmpty()) {
                    viewModel.ubahStatus(
                        "Pengguna $userId belum memiliki postingan"
                    )
                } else {
                    val detail = posts.joinToString("\n\n") {
                        "Judul: ${it.title}\nIsi: ${it.body}"
                    }

                    viewModel.ubahStatus(
                        "Postingan pengguna $userId:\n\n$detail"
                    )
                }
            } catch (e: HttpException) {
                viewModel.ubahStatus(
                    "Gagal mengambil postingan: HTTP ${e.code()}"
                )
            } catch (e: IOException) {
                viewModel.ubahStatus("Tidak ada koneksi internet")
            } catch (e: Exception) {
                viewModel.ubahStatus(
                    "Terjadi kesalahan: ${e.localizedMessage ?: "Tidak diketahui"}"
                )
            }
        }
    }
}