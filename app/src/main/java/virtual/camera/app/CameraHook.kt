package virtual.camera.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File

class CameraHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        // We only want to hook apps that actually use the camera, skip system framework
        if (lpparam.packageName == "android" || lpparam.packageName == "virtual.camera.app") return

        try {
            // Hook the classic Camera1 API method 'setPreviewCallbackWithBuffer'
            XposedHelpers.findAndHookMethod(
                "android.hardware.Camera",
                lpparam.classLoader,
                "setPreviewCallbackWithBuffer",
                Camera.PreviewCallback::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("VirtualCamera: Camera hooked in package ${lpparam.packageName}")
                        
                        // 1. Read the user's selected media path
                        // We use XSharedPreferences because standard SharedPreferences don't work across app boundaries
                        val prefs = XSharedPreferences("virtual.camera.app", "VirtualCameraPrefs")
                        prefs.makeWorldReadable()
                        val mediaPath = prefs.getString("saved_media_path", null)

                        if (mediaPath != null) {
                            XposedBridge.log("VirtualCamera: Injecting media from $mediaPath")
                            
                            // Get the original callback the target app provided
                            val originalCallback = param.args[0] as? Camera.PreviewCallback
                            
                            // Replace it with our own callback
                            param.args[0] = Camera.PreviewCallback { data, camera ->
                                // ---------------------------------------------------------
                                // THE INJECTION POINT
                                // Here is where you convert your saved Photo/Video into a NV21 byte array (data)
                                // and feed it to the originalCallback.onPreviewFrame(data, camera)
                                // ---------------------------------------------------------
                                
                                // Example: If you have an image, you would decode it, 
                                // convert the Bitmap to YUV NV21 format, and overwrite 'data'
                                
                                originalCallback?.onPreviewFrame(data, camera)
                            }
                        }
                    }
                }
            )
        } catch (e: Throwable) {
            XposedBridge.log("VirtualCamera Error: ${e.message}")
        }
    }
}
