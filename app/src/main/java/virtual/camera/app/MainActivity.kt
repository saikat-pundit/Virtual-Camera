package virtual.camera.app

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import virtual.camera.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedMediaUri: Uri? = null

    // This handles launching the gallery and receiving the result
    private val selectMediaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedMediaUri = uri
            binding.tvSelectedPath.text = "Selected: $uri"
            binding.tvSelectedPath.setTextColor(resources.getColor(android.R.color.holo_green_dark, theme))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load previously saved media path if it exists
        val sharedPrefs = getSharedPreferences("VirtualCameraPrefs", Context.MODE_PRIVATE)
        val savedPath = sharedPrefs.getString("saved_media_path", null)
        if (savedPath != null) {
            binding.tvSelectedPath.text = "Saved: $savedPath"
            binding.tvSelectedPath.setTextColor(resources.getColor(android.R.color.holo_green_dark, theme))
        }

        // Button to open the gallery for both images and videos
        binding.btnSelectMedia.setOnClickListener {
            selectMediaLauncher.launch("*/*") // Accepts both photos and videos
        }

        // Button to save the selection
        binding.btnSave.setOnClickListener {
            if (selectedMediaUri != null) {
                sharedPrefs.edit().putString("saved_media_path", selectedMediaUri.toString()).apply()
                Toast.makeText(this, "Configuration Saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please select a media file first.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
