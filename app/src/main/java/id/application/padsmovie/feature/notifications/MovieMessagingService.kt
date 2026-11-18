package id.application.padsmovie.feature.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import id.application.padsmovie.MainActivity
import id.application.padsmovie.R

internal const val MOVIE_ID = "movieId"
internal const val MOVIE_TOPIC = "movie-updates"
private const val CHANNEL_ID = "movie_updates"

internal fun movieIdFrom(value: String?): Int? = value?.toIntOrNull()?.takeIf { it > 0 }

class MovieMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val movieId = movieIdFrom(message.data[MOVIE_ID]) ?: return
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val notifications = NotificationManagerCompat.from(this)
        if (!notifications.areNotificationsEnabled()) return

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.movie_notifications), NotificationManager.IMPORTANCE_DEFAULT),
        )

        val tapIntent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra(MOVIE_ID, movieId.toString())
        val tap = PendingIntent.getActivity(this, movieId, tapIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val title = message.data["title"]?.trim()?.takeIf { it.isNotEmpty() } ?: getString(R.string.movie_notifications)
        val body = message.data["body"]?.trim()?.takeIf { it.isNotEmpty() } ?: getString(R.string.notification_movie_body)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_movie_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(tap)
            .setAutoCancel(true)
            .build()

        try {
            notifications.notify(movieId, notification)
        } catch (_: SecurityException) {
            // Permission may change between the check and posting.
        }
    }
}
