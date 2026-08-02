package virtual.camera.app.app

import android.annotation.SuppressLint
import android.content.Context
import com.hack.opensdk.HackApplication

class App : HackApplication() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private lateinit var mContext: Context

        @JvmStatic
        fun getContext(): Context {
            return mContext
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        mContext = base!!
    }
}
