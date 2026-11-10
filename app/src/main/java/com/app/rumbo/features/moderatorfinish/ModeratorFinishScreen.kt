package com.app.rumbo.features.moderatorfinish

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R
import com.app.rumbo.core.util.label
import com.app.rumbo.core.util.formatThousands

private val BodyBg = Color(0xFF0F1E1B)
private val SurfaceBg = Color(0xFF162824)
private val White = Color(0xFFFFFFFF)
private val FinishGreen = Color(0xFF117361)
private val TextSecondary = Color(0xFF7DA89B)

@Composable
fun ModeratorFinishRoute(
    onBack: () -> Unit = {},
    onFinished: () -> Unit = {},
    viewModel: ModeratorFinishViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ModeratorFinishScreen(
        state = state,
        onBack = onBack,
        onConfirm = { viewModel.onConfirm(onFinished) }
    )
}

@Composable
fun ModeratorFinishScreen(
    state: ModeratorFinishUiState,
    onBack: () -> Unit = {},
    onConfirm: () -> Unit = {}
) {
    val publication = state.publication
    Scaffold(containerColor = BodyBg) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.common_back), tint = White)
                }
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.moderator_finish_title), color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Box(Modifier.weight(1f).fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                when {
                    state.isLoading -> CircularProgressIndicator(color = White)
                    publication == null -> Text(
                        stringResource(R.string.moderator_finish_empty),
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    else -> Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_task_alt),
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(64.dp).background(Color(0xFF1F3B35), CircleShape).padding(16.dp)
                        )
                        Text(stringResource(R.string.moderator_finish_heading), color = White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        val bodyPrefix = stringResource(R.string.moderator_finish_body_prefix, publication.title)
                        val bodyState = stringResource(R.string.moderator_finish_state)
                        val bodySuffix = stringResource(R.string.moderator_finish_body_suffix)
                        Text(
                            buildAnnotatedString {
                                append(bodyPrefix)
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append(bodyState) }
                                append(bodySuffix)
                            },
                            color = TextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            stringResource(
                                R.string.moderator_finish_info,
                                publication.category.label,
                                publication.town,
                                pluralStringResource(
                                    R.plurals.home_interested,
                                    publication.votes,
                                    publication.votes.formatThousands()
                                )
                            ),
                            color = White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceBg)
                                .padding(12.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onBack,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceBg)
                            ) { Text(stringResource(R.string.moderator_finish_cancel), color = White) }
                            Button(
                                onClick = onConfirm,
                                enabled = !state.isFinishing,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FinishGreen)
                            ) {
                                Icon(painterResource(R.drawable.ic_task_alt), null, Modifier.size(18.dp), tint = White)
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.moderator_finish_confirm), color = White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
