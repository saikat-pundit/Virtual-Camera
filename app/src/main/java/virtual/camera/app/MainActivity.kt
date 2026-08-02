package virtual.camera.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.hack.opensdk.HackApi
import virtual.camera.app.app.App
import virtual.camera.app.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedUri: Uri? = null

    private val selectMediaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            binding.tvSelectedPath.text = "Selected: $uri"
            binding.tvSelectedPath.setTextColor(resources.getColor(android.R.color.holo_green_dark, theme))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPrefs = getSharedPreferences("VirtualCameraPrefs", Context.MODE_PRIVATE)
        val savedPath = sharedPrefs.getString("saved_media_path", null)
        if (savedPath != null) {
            binding.tvSelectedPath.text = "Configured: $savedPath"
            binding.tvSelectedPath.setTextColor(resources.getColor(android.R.color.holo_green_dark, theme))
        }

        // Open gallery for both images and videos
        binding.btnSelectMedia.setOnClickListener {
            selectMediaLauncher.launch("*/*")
        }

        // Save selected photo/video to local storage for the virtual camera engine
        binding.btnSave.setOnClickListener {
            if (selectedUri != null) {
                copyMediaToInternal(selectedUri!!)
            } else {
                Toast.makeText(this, "Please select an image or video first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun copyMediaToInternal(uri: Uri) {
        try {
            val destFile = File(App.getContext().filesDir, "virtual_camera_feed")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            val sharedPrefs = getSharedPreferences("VirtualCameraPrefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().putString("saved_media_path", destFile.absolutePath).apply()
            
            Toast.makeText(this, "Virtual feed saved successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to process media: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Call this function when you want to launch an app inside the virtual sandbox
    private fun launchAppInSandbox(packageName: String, userId: Int = 0) {
        val intent: Intent? = HackApi.getLaunchIntentForPackage(packageName, userId)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            HackApi.startActivity(intent, 0)
        } else {
            Toast.makeText(this, "App not found in sandbox", Toast.LENGTH_SHORT).show()
        }
    }
}
