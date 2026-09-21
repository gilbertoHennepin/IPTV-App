package com.iptvapp.updater

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.iptvapp.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File

data class UpdateInfo(
    val isUpdateAvailable: Boolean,
    val latestVersionCode: Int,
    val latestVersionName: String,
    val apkUrl: String,
    val releaseNotes: String
)

@Singleton
class UpdateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Placeholder URL - update this to your actual hosted update.json later!
    private val UPDATE_JSON_URL = "https://raw.githubusercontent.com/PLACEHOLDER/update.json"

    suspend fun checkForUpdates(): Result<UpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val url = URL(UPDATE_JSON_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            if (connection.responseCode == 200) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonString)
                
                val latestVersionCode = json.getInt("versionCode")
                val latestVersionName = json.getString("versionName")
                val apkUrl = json.getString("apkUrl")
                val releaseNotes = json.optString("releaseNotes", "Bug fixes and improvements")
                
                val currentVersionCode = BuildConfig.VERSION_CODE
                val isUpdateAvailable = latestVersionCode > currentVersionCode
                
                Result.success(
                    UpdateInfo(
                        isUpdateAvailable = isUpdateAvailable,
                        latestVersionCode = latestVersionCode,
                        latestVersionName = latestVersionName,
                        apkUrl = apkUrl,
                        releaseNotes = releaseNotes
                    )
                )
            } else {
                Result.failure(Exception("Failed to fetch update info: HTTP ${connection.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun startDownload(updateInfo: UpdateInfo): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(updateInfo.apkUrl)
        
        // Use external files dir so we don't need dangerous storage permissions on newer Androids
        val destinationFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "update.apk")
        if (destinationFile.exists()) {
            destinationFile.delete()
        }

        val request = DownloadManager.Request(uri)
            .setTitle("Downloading App Update")
            .setDescription("Downloading version ${updateInfo.latestVersionName}")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(destinationFile))
            // Only download over unmetered or metered Wi-Fi/Ethernet
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            
        return downloadManager.enqueue(request)
    }
}
