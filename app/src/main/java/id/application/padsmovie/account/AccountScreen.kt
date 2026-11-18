package id.application.padsmovie.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import id.application.padsmovie.R
import id.application.padsmovie.ui.ScreenScaffold

@Composable
internal fun AccountScreen(onBack: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val auth = remember(context) {
        FirebaseApp.getApps(context).firstOrNull { it.name == FirebaseApp.DEFAULT_APP_NAME }
            ?.let { FirebaseAuth.getInstance(it) }
    }
    var user by remember(auth) { mutableStateOf(auth?.currentUser) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { user = it.currentUser }
        auth?.addAuthStateListener(listener)
        onDispose { auth?.removeAuthStateListener(listener) }
    }

    ScreenScaffold(stringResource(R.string.account_title), onBack) { padding ->
        Column(
            modifier = Modifier.padding(padding).consumeWindowInsets(padding)
                .imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when {
                auth == null -> Text(stringResource(R.string.account_unavailable))
                user != null -> {
                    user?.email?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
                    Text(stringResource(R.string.subscription_title), style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(R.string.subscription_unavailable))
                    OutlinedButton(onClick = auth::signOut, enabled = !submitting) {
                        Text(stringResource(R.string.account_sign_out))
                    }
                }
                else -> {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; failed = false },
                        label = { Text(stringResource(R.string.account_email)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        enabled = !submitting,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; failed = false },
                        label = { Text(stringResource(R.string.account_password)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !submitting,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (failed) Text(stringResource(R.string.account_sign_in_error), color = MaterialTheme.colorScheme.error)
                    Button(
                        enabled = email.isNotBlank() && password.isNotEmpty() && !submitting,
                        onClick = {
                            submitting = true
                            failed = false
                            auth.signInWithEmailAndPassword(email.trim(), password).addOnCompleteListener { result ->
                                submitting = false
                                failed = !result.isSuccessful
                                if (result.isSuccessful) password = ""
                            }
                        },
                    ) {
                        Text(stringResource(if (submitting) R.string.account_signing_in else R.string.account_sign_in))
                    }
                }
            }
        }
    }
}
