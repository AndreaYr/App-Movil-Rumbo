package com.app.rumbo.core.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.rumbo.R
import com.app.rumbo.core.util.AppLanguage
import com.app.rumbo.core.util.applyAppLanguage
import com.app.rumbo.core.util.currentLanguage
import com.app.rumbo.core.util.findActivity

@Composable
fun LanguageDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val selectedLanguage = currentLanguage(context)

    fun select(language: AppLanguage) {
        context.findActivity()?.let { applyAppLanguage(it, language) }
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language_title)) },
        text = {
            Column {
                for (language in AppLanguage.entries) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { select(language) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLanguage == language,
                            onClick = { select(language) }
                        )
                        Text(
                            text = stringResource(language.labelRes),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_close))
            }
        }
    )
}
