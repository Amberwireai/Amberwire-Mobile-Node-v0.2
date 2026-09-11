package xyz.amberwire.mobilenode.pixel

import android.app.Application
import androidx.work.*
import java.util.concurrent.TimeUnit

class AmberwireApp: Application() {
    override fun onCreate() { super.onCreate(); scheduleSync() }
    private fun scheduleSync() {
        val request=PeriodicWorkRequestBuilder<UploadWorker>(15,TimeUnit.MINUTES).setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("amberwire-upload",ExistingPeriodicWorkPolicy.UPDATE,request)
    }
}
