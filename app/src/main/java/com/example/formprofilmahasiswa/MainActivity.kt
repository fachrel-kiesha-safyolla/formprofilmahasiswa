package com.example.formprofilmahasiswa   // sesuaikan dengan package project kamu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

//1. WARNA
private val WarnaUtama = Color(0xFF880E4F)
private val WarnaKartu = Color(0xFF880E4F)
private val WarnaLatar = Color(0xFFFDFBD4)

private val SkemaWarna = lightColorScheme(
    primary = WarnaUtama,
    onPrimary = Color.White,
    primaryContainer = WarnaKartu,
    onPrimaryContainer = Color(0xFF0D3B12),
    background = WarnaLatar
)

// Data hasil simpan
data class ProfilMahasiswa(
    val nama: String,
    val nim: String,
    val prodi: String,
    val semester: String,
    val kelas: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = SkemaWarna) {
                ProfilMahasiswaScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilMahasiswaScreen() {
    // ---------- STATE ----------
    var nama by remember { mutableStateOf("") }
    var nim by remember { mutableStateOf("") }
    var prodi by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }
    var kelas by remember { mutableStateOf("") }              // "" = belum dipilih
    var dropdownTerbuka by remember { mutableStateOf(false) }
    var profilTersimpan by remember { mutableStateOf<ProfilMahasiswa?>(null) }

    val pilihanKelas = listOf("Pagi", "Malam")

    // ---------- VALIDASI (diturunkan dari state) ----------
    val namaValid = nama.isNotBlank()
    val nimValid = nim.length >= 5 && nim.all { it.isDigit() }
    val prodiValid = prodi.isNotBlank()
    val semesterValid = semester.toIntOrNull() in 1..14
    val kelasValid = kelas.isNotEmpty()
    val formValid = namaValid && nimValid && prodiValid && semesterValid && kelasValid

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),   // supaya bisa di-scroll
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---------- 2. HEADER + LOGO ----------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_universitas),
                    contentDescription = "Logo Universitas",
                    modifier = Modifier.size(64.dp)
                )
                Column {
                    Text(
                        "UNIVERSITAS MULIA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Form Profil Mahasiswa", style = MaterialTheme.typography.bodyLarge)
                }
            }

            OutlinedTextField(
                value = nama,
                onValueChange = { nama = it },
                label = { Text("Nama") },
                isError = nama.isNotEmpty() && !namaValid,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = nim,
                onValueChange = { nim = it },
                label = { Text("NIM") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = nim.isNotEmpty() && !nimValid,
                supportingText = {
                    if (nim.isNotEmpty() && !nimValid) Text("NIM harus angka, minimal 5 digit")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = prodi,
                onValueChange = { prodi = it },
                label = { Text("Program Studi") },
                isError = prodi.isNotEmpty() && !prodiValid,
                modifier = Modifier.fillMaxWidth()
            )

            // ---------- 3. TEXTFIELD SEMESTER ----------
            OutlinedTextField(
                value = semester,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) semester = input   // hanya angka
                },
                label = { Text("Semester") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = semester.isNotEmpty() && !semesterValid,
                supportingText = {
                    if (semester.isNotEmpty() && !semesterValid) Text("Semester harus 1 - 14")
                },
                modifier = Modifier.fillMaxWidth()
            )

            // ---------- 4. DROPDOWN KELAS (Pagi / Malam) ----------
            ExposedDropdownMenuBox(
                expanded = dropdownTerbuka,
                onExpandedChange = { dropdownTerbuka = it }
            ) {
                OutlinedTextField(
                    value = kelas,
                    onValueChange = {},                  // read-only, hanya boleh pilih
                    readOnly = true,
                    label = { Text("Kelas") },
                    placeholder = { Text("Pilih kelas") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownTerbuka) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = dropdownTerbuka,
                    onDismissRequest = { dropdownTerbuka = false }
                ) {
                    pilihanKelas.forEach { pilihan ->
                        DropdownMenuItem(
                            text = { Text(pilihan) },
                            onClick = {
                                kelas = pilihan          // EVENT -> ubah state
                                dropdownTerbuka = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    profilTersimpan = ProfilMahasiswa(nama, nim, prodi, semester, kelas)
                    scope.launch { snackbarHostState.showSnackbar("Profil berhasil disimpan") }
                },
                enabled = formValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan")
            }

            // ---------- KARTU PROFIL ----------
            profilTersimpan?.let { p ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Kartu Profil", style = MaterialTheme.typography.titleMedium)
                        Text("Nama: ${p.nama}")
                        Text("NIM: ${p.nim}")
                        Text("Program Studi: ${p.prodi}")
                        Text("Semester: ${p.semester}")
                        Text("Kelas: ${p.kelas}")
                    }
                }
            }
        }
    }
}