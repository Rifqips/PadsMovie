package id.application.padsmovie.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.padsmovie.core.designsystem.R
import com.padsmovie.core.domain.model.Trailer

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TrailerDialog(trailer: Trailer, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val webView = remember(trailer.key) {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            webViewClient = WebViewClient()
            webChromeClient = android.webkit.WebChromeClient()
            val key = Uri.encode(trailer.key)
            val origin = "https://${context.packageName}"
            loadDataWithBaseURL(
                origin,
                """<html><head><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;background:black"><iframe width="100%" height="100%"
                src="https://www.youtube.com/embed/$key?playsinline=1&amp;origin=$origin"
                title="YouTube trailer" frameborder="0" allow="encrypted-media; fullscreen" allowfullscreen></iframe></body></html>""",
                "text/html", "UTF-8", null,
            )
        }
    }
    DisposableEffect(webView, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) webView.onPause()
            if (event == Lifecycle.Event.ON_START) webView.onResume()
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            webView.stopLoading()
            webView.onPause()
            webView.destroy()
        }
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
                AndroidView(factory = { webView }, modifier = Modifier.fillMaxWidth().height(220.dp))
                Text(stringResource(R.string.trailer_playback_error))
                TextButton(onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(trailer.youtubeUrl)))
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(context, R.string.no_video_app, Toast.LENGTH_SHORT).show()
                    }
                }) { Text(stringResource(R.string.open_youtube)) }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.back)) }
            }
        }
    }
}
