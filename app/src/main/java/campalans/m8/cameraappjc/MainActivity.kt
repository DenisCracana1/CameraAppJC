package campalans.m8.cameraappjc

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher per capturar la foto
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            capturedImageUri = imageUri
        }
    }

    // Funció per crear el fitxer i obtenir l'URI
    fun createImageFile(): Uri {
        val imageFile = File(
            context.getExternalFilesDir(null),
            "Denis_Cracana_${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.png"
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Càmera amb Intent",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = {
                imageUri = createImageFile()
                cameraLauncher.launch(imageUri!!)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Obrir Càmera")
        }

        // Mostrar la imatge capturada
        capturedImageUri?.let { uri ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Foto capturada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

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
                    Text("La imatge apareixerà aquí")
                }
            }
        }
    }
}
