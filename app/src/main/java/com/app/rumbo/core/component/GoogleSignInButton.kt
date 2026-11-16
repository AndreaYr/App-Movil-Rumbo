package com.app.rumbo.core.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.rumbo.R
import com.app.rumbo.core.auth.GoogleIdTokenProvider
import com.app.rumbo.core.auth.GoogleTokenResult
import com.app.rumbo.core.theme.White
import kotlinx.coroutines.launch

/**
 * Botón "Continuar con Google". Abre el selector de cuentas y entrega el resultado
 * a la pantalla, que lo pasa al ViewModel; este componente no contiene lógica de negocio.
 */
@Composable
fun GoogleSignInButton(
    isLoading: Boolean,
    onToken: (String) -> Unit,
    onError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val webClientId = stringResource(R.string.google_web_client_id)
    val scope = rememberCoroutineScope()

    OutlinedButton(
        onClick = {
            scope.launch {
                when (val result = GoogleIdTokenProvider.fetch(context, webClientId)) {
                    is GoogleTokenResult.Success -> onToken(result.idToken)
                    GoogleTokenResult.Cancelled -> Unit
                    GoogleTokenResult.Failure -> onError()
                }
            }
        },
        enabled = !isLoading,
        modifier = modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Image(
                painter = painterResource(R.drawable.ic_google),
                contentDescription = null, // el texto del botón ya describe la acción
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                stringResource(R.string.google_sign_in_button),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
