package tomato.simple.gallery.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import tomato.simple.gallery.jobs.BootScanJob
import tomato.simple.gallery.jobs.NewPhotoFetcher

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NewPhotoFetcher().scheduleJob(context)
        BootScanJob.schedule(context)
    }
}
