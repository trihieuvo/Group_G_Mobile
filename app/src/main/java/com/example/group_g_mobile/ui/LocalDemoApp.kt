package com.example.group_g_mobile.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
// Emojis are used for icons to avoid extra dependency loading overhead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.group_g_mobile.data.*
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalDemoApp(
    preferencesManager: PreferencesManager,
    fileManager: InternalFileManager,
    repository: NoteRepository,
    api: MockServerApi,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val networkAvailable by api.isNetworkAvailable.collectAsState()
    val serverFail by api.shouldServerFail.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Local Storage & Sync Demo",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    // Network indicator
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (networkAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (networkAvailable) "☁️" else "🔌",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (networkAvailable) "Online" else "Offline",
                            color = if (networkAvailable) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Ghi chú (Database)") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Hồ sơ (Pref & File)") }
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (selectedTab == 0) {
                    NotesTab(
                        repository = repository,
                        api = api,
                        networkAvailable = networkAvailable,
                        serverFail = serverFail
                    )
                } else {
                    ProfileTab(
                        preferencesManager = preferencesManager,
                        fileManager = fileManager,
                        isDarkTheme = isDarkTheme,
                        onThemeChange = onThemeChange
                    )
                }
            }

            // Sync Log Terminal at bottom
            SyncLogConsole(modifier = Modifier.height(180.dp))
        }
    }
}

@Composable
fun NotesTab(
    repository: NoteRepository,
    api: MockServerApi,
    networkAvailable: Boolean,
    serverFail: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    val notesList by repository.notes.collectAsState()
    var noteInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Control panel for network emulation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "BẢNG ĐIỀU KHIỂN GIẢ LẬP MẠNG",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Kết nối mạng (WiFi / 4G)", fontSize = 14.sp)
                    Switch(
                        checked = networkAvailable,
                        onCheckedChange = {
                            api.setNetworkAvailable(it)
                            SyncLogger.log("SYSTEM: Network toggled to ${if (it) "CONNECTED" else "DISCONNECTED"}")
                            if (it) {
                                // Automatically trigger sync of pending notes when network returns
                                coroutineScope.launch {
                                    repository.syncPendingNotes()
                                }
                            }
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mô phỏng lỗi Server (500)", fontSize = 14.sp)
                    Switch(
                        checked = serverFail,
                        onCheckedChange = {
                            api.setServerFail(it)
                            SyncLogger.log("SYSTEM: Simulated server failure toggled to ${if (it) "ON" else "OFF"}")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.syncPendingNotes()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("🔄", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đồng bộ thủ công (Retry Sync)")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Note Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                label = { Text("Nhập nội dung ghi chú...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (noteInput.isNotBlank()) {
                        val text = noteInput
                        noteInput = ""
                        coroutineScope.launch {
                            repository.addNote(text)
                        }
                    }
                },
                modifier = Modifier.height(56.dp)
            ) {
                Text("Thêm")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Notes list
        Text(
            "Danh sách ghi chú (Lưu trong SQLite)",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (notesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Chưa có ghi chú nào.\nHãy thử thêm mới!",
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notesList, key = { it.id }) { note ->
                    NoteCard(note = note, onDelete = {
                        coroutineScope.launch {
                            repository.deleteNote(note.id)
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun NoteCard(note: Note, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.content,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Format timestamp helper
                val timeString = java.text.SimpleDateFormat("HH:mm:ss dd/MM", java.util.Locale.getDefault())
                    .format(java.util.Date(note.timestamp))
                Text(
                    text = "Lưu lúc: $timeString",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sync status badge
                val (badgeText, badgeColor) = when {
                    note.isSynced -> "Đã đồng bộ" to Color(0xFF2E7D32)
                    note.syncFailed -> "Lỗi mạng" to Color(0xFFC62828)
                    else -> "Đang chờ" to Color(0xFFEF6C00)
                }
                
                val icon = when {
                    note.isSynced -> "✔️"
                    note.syncFailed -> "❌"
                    else -> "🕒"
                }

                Surface(
                    color = badgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "$icon $badgeText",
                        color = badgeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(onClick = onDelete) {
                    Text("🗑️", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun ProfileTab(
    preferencesManager: PreferencesManager,
    fileManager: InternalFileManager,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val currentUsername by preferencesManager.usernameFlow.collectAsState(initial = "Guest")
    var usernameInput by remember { mutableStateOf("") }

    LaunchedEffect(currentUsername) {
        usernameInput = currentUsername
    }

    var avatarPath by remember { mutableStateOf(fileManager.getAvatarPath()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                SyncLogger.log("File System: Coping selected photo to app sandbox...")
                val savedPath = fileManager.saveAvatar(uri)
                if (savedPath != null) {
                    avatarPath = savedPath
                    SyncLogger.log("File System: Saved avatar image to Internal Storage ($savedPath)")
                } else {
                    SyncLogger.log("File System Error: Failed to save avatar photo.")
                }
            }
        }
    )

    // Load bitmap custom helper
    val avatarBitmap = remember(avatarPath) {
        if (avatarPath != null) {
            val file = File(avatarPath!!)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                bitmap?.asImageBitmap()
            } else null
        } else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "CÀI ĐẶT HỒ SƠ",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )

        // Avatar Image (File Storage)
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (avatarBitmap != null) {
                Image(
                    bitmap = avatarBitmap,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("👤", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Chọn ảnh", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        Text(
            "Chạm vào hình tròn để đổi ảnh (Lưu File vào Internal Storage)",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Username Input (Key-Value)
        OutlinedTextField(
            value = usernameInput,
            onValueChange = {
                usernameInput = it
                coroutineScope.launch {
                    preferencesManager.saveUsername(it)
                }
                SyncLogger.log("Key-Value: Username updated to \"$it\" in DataStore")
            },
            label = { Text("Tên người dùng (Username)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Theme Toggle (Key-Value)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Giao diện tối (Dark Mode)", fontWeight = FontWeight.Bold)
                    Text("Trạng thái lưu trong Key-Value", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = {
                        onThemeChange(it)
                        coroutineScope.launch {
                            preferencesManager.saveDarkMode(it)
                        }
                        SyncLogger.log("Key-Value: Theme changed to ${if (it) "DARK" else "LIGHT"} in DataStore")
                    }
                )
            }
        }
        
        Button(
            onClick = {
                val deleted = fileManager.deleteAvatar()
                if (deleted) {
                    avatarPath = null
                    SyncLogger.log("File System: Deleted avatar image from internal storage")
                } else {
                    SyncLogger.log("File System: No avatar file to delete")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Xóa ảnh đại diện")
        }
    }
}

@Composable
fun SyncLogConsole(modifier: Modifier = Modifier) {
    val logs by SyncLogger.logs.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll logic when new logs are added
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "REAL-TIME LOGS TERMINAL",
                color = Color(0xFF64B5F6),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                "Clear",
                color = Color.Gray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .clickable { SyncLogger.clear() }
                    .padding(horizontal = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = Color(0xFF333333))
        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(logs) { log ->
                val textColor = when {
                    log.contains("API Success") || log.contains("Synced") -> Color(0xFF81C784) // green
                    log.contains("Error") || log.contains("failed") -> Color(0xFFE57373) // red
                    log.contains("API:") || log.contains("Connecting") -> Color(0xFF4FC3F7) // cyan
                    log.contains("Local DB") -> Color(0xFFFFB74D) // orange
                    log.contains("Key-Value") -> Color(0xFFF06292) // pink
                    log.contains("File System") -> Color(0xFFBA68C8) // purple
                    else -> Color.White
                }
                Text(
                    text = log,
                    color = textColor,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NoteCardPreview() {
    NoteCard(
        note = Note(
            id = 1,
            content = "Chuẩn bị slide thuyết trình (Đã đồng bộ)",
            timestamp = System.currentTimeMillis(),
            isSynced = true,
            syncFailed = false
        ),
        onDelete = {}
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NoteCardPendingPreview() {
    NoteCard(
        note = Note(
            id = 2,
            content = "Ghi chú Offline (Chờ mạng)",
            timestamp = System.currentTimeMillis(),
            isSynced = false,
            syncFailed = false
        ),
        onDelete = {}
    )
}
