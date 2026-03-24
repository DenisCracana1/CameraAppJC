package campalans.m8.cameraappjc

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraScreen()
                }
            }
        }
    }
}

@Composable
fun CameraScreen() {

    val context = LocalContext.current
    var permissionStatus by remember { mutableStateOf(" Permission not Requested") }

    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var recordedVideoUri by remember { mutableStateOf<Uri?>(null) }

    fun createVideoFile(): Uri {
        val videoFile = File(
            context.getExternalFilesDir(null),
            "Denis_Cracana_${SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())}.mp4"
        )

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            videoFile
        )
    }

    // Launcher per gravar vídeo
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            recordedVideoUri = videoUri
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grantedPermission ->

        val cameraGranted = grantedPermission[android.Manifest.permission.CAMERA] ?: false

        permissionStatus = if (cameraGranted) "Permission Granted" else "Permission denied"

        if (cameraGranted) {
            videoUri = createVideoFile()
            cameraLauncher.launch(videoUri!!)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        val granted = ContextCompat.checkSelfPermission(
            LocalContext.current,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        Text(
            text = "Càmera Vídeo amb Intent",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = {
                if (!granted) {
                    permissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    )
                } else {
                    videoUri = createVideoFile()
                    cameraLauncher.launch(videoUri!!)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gravar vídeo")
        }

        // Mostrar el vídeo capturat amb Media3
        recordedVideoUri?.let { uri ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                VideoPlayer(uri)
            }

        } ?: run {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Text("El vídeo apareixerà aquí")
                }
            }
        }
    }
}

@Composable
fun VideoPlayer(videoUri: Uri) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUri)
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    DisposableEffect(videoUri) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
