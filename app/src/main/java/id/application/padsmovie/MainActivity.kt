package id.application.padsmovie

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.padsmovie.core.designsystem.theme.PadsMovieTheme
import com.padsmovie.core.domain.repository.MovieRepository
import dagger.hilt.android.AndroidEntryPoint
import id.application.padsmovie.feature.notifications.MOVIE_ID
import id.application.padsmovie.feature.notifications.MOVIE_TOPIC
import id.application.padsmovie.feature.notifications.movieIdFrom
import id.application.padsmovie.navigation.PadsMovieApp
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var repository: MovieRepository

    private var notificationMovieId by mutableStateOf<Int?>(null)
    private val requestNotifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) subscribeToMovieUpdates()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationMovieId = if (savedInstanceState == null) {
            movieIdFrom(intent.getStringExtra(MOVIE_ID))
        } else {
            savedInstanceState.getInt(MOVIE_ID).takeIf { it > 0 }
        }

        val firebaseConfigured = FirebaseApp.getApps(this).any { it.name == FirebaseApp.DEFAULT_APP_NAME }
        enableEdgeToEdge()
        setContent {
            PadsMovieTheme {
                PadsMovieApp(
                    repository = repository,
                    notificationMovieId = notificationMovieId,
                    onNotificationOpened = { notificationMovieId = null },
                    onEnableNotifications = if (firebaseConfigured) ::enableMovieNotifications else null,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        movieIdFrom(intent.getStringExtra(MOVIE_ID))?.let { notificationMovieId = it }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        notificationMovieId?.let { outState.putInt(MOVIE_ID, it) }
        super.onSaveInstanceState(outState)
    }

    private fun enableMovieNotifications() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            val preferences = getPreferences(MODE_PRIVATE)
            if (preferences.getBoolean("notification_permission_requested", false) &&
                !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
            ) {
                openNotificationSettings()
            } else {
                preferences.edit().putBoolean("notification_permission_requested", true).apply()
                requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            return
        }
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            openNotificationSettings()
            return
        }
        subscribeToMovieUpdates()
    }

    private fun openNotificationSettings() {
        startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
    }

    private fun subscribeToMovieUpdates() {
        FirebaseMessaging.getInstance().subscribeToTopic(MOVIE_TOPIC).addOnCompleteListener(this) { result ->
            val message = if (result.isSuccessful) R.string.notification_subscribed else R.string.notification_subscription_failed
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
