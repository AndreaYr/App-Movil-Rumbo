package com.app.rumbo.features.mypublications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.core.util.formatThousands
import com.app.rumbo.data.local.SamplePublications
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.features.feed.PublicationCard

private val RejectedBackground = Color(0xFFFCE8E2)
private val RejectedBorder = Color(0xFFF0C4B6)
private val RejectedContent = Color(0xFF8E2F13)

@Composable
fun MyPublicationsRoute(
    onBack: () -> Unit,
    onPublicationClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyPublicationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MyPublicationsScreen(
        state = state,
        onBack = onBack,
        onPublicationClick = onPublicationClick,
        onEditClick = onEditClick,
        onTabSelected = viewModel::onTabSelected,
        onFinishRequested = viewModel::onFinishRequested,
        onFinishDismissed = viewModel::onFinishDismissed,
        onFinishConfirmed = viewModel::onFinishConfirmed,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPublicationsScreen(
    state: MyPublicationsUiState,
    onBack: () -> Unit,
    onPublicationClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onTabSelected: (MyPublicationsTab) -> Unit,
    onFinishRequested: (String) -> Unit,
    onFinishDismissed: () -> Unit,
    onFinishConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis publicaciones", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)
        ) {
            TabsRow(state, onTabSelected)
            val publications = state.visiblePublications
            if (publications.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        if (state.publications.isEmpty()) "Aún no tienes publicaciones." else "No hay publicaciones en este estado.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(publications, key = { it.id }) { publication ->
                        PublicationItem(
                            publication = publication,
                            onClick = { onPublicationClick(publication.id) },
                            onEditClick = { onEditClick(publication.id) },
                            onFinishClick = { onFinishRequested(publication.id) }
                        )
                    }
                }
            }
        }
    }

    state.publicationToFinish?.let { publication ->
        FinishPublicationSheet(publication, onConfirm = onFinishConfirmed, onDismiss = onFinishDismissed)
    }
}

@Composable
private fun TabsRow(state: MyPublicationsUiState, onTabSelected: (MyPublicationsTab) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(MyPublicationsTab.entries) { tab ->
            val selected = tab == state.selectedTab
            val shape = RoundedCornerShape(999.dp)
            val content = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
            Row(
                modifier = Modifier
                    .clip(shape)
                    .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                    .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, shape)
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(tab.label, style = MaterialTheme.typography.labelLarge, color = content)
                Text(state.countFor(tab).formatThousands(), style = MaterialTheme.typography.labelMedium, color = content)
            }
        }
    }
}

@Composable
private fun PublicationItem(
    publication: Publication,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PublicationCard(publication = publication, onClick = onClick, onToggleInterest = {})
        if (publication.status == PublicationStatus.REJECTED) {
            RejectionReason(publication)
            OutlinedButton(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(painterResource(R.drawable.ic_edit), null, Modifier.size(18.dp))
                Text("Editar y reenviar", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 8.dp))
            }
        }
        if (publication.status == PublicationStatus.VERIFIED) {
            OutlinedButton(
                onClick = onFinishClick,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(painterResource(R.drawable.ic_task_alt), null, Modifier.size(18.dp))
                Text("Finalizar", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun RejectionReason(publication: Publication) {
    val shape = MaterialTheme.shapes.medium
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Motivo del rechazo: ") }
            append(publication.rejectionReason ?: "Sin motivo registrado")
            if (!publication.rejectionDetail.isNullOrBlank()) append(" — ${publication.rejectionDetail}")
        },
        style = MaterialTheme.typography.bodyMedium,
        color = RejectedContent,
        modifier = Modifier
            .fillMaxWidth()
            .background(RejectedBackground, shape)
            .border(1.dp, RejectedBorder, shape)
            .padding(12.dp)
    )
}

/** Hoja inferior de confirmación: finalizar no se puede deshacer. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FinishPublicationSheet(publication: Publication, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp).padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painterResource(R.drawable.ic_task_alt),
                contentDescription = null,
                modifier = Modifier.size(56.dp).background(Color(0xFFECEEF1), CircleShape).padding(14.dp),
                tint = Color(0xFF4A5458)
            )
            Text("¿Marcar como finalizada?", style = MaterialTheme.typography.titleLarge)
            Text(
                buildAnnotatedString {
                    append("“${publication.title}” dejará de recibir votos y comentarios nuevos y se mostrará como ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Finalizada") }
                    append(". Podrás consultarla en tu historial, pero no volver a abrirla.")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.medium
                ) { Text("Cancelar") }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Sí, finalizar") }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyPublicationsScreenPreview() {
    RumboTheme {
        MyPublicationsScreen(
            state = MyPublicationsUiState(publications = SamplePublications.create().take(2)),
            onBack = {},
            onPublicationClick = {},
            onEditClick = {},
            onTabSelected = {},
            onFinishRequested = {},
            onFinishDismissed = {},
            onFinishConfirmed = {}
        )
    }
}
