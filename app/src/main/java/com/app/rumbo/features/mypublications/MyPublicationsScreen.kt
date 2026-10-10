package com.app.rumbo.features.mypublications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.data.local.SamplePublications
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.features.feed.PublicationCard

@Composable
fun MyPublicationsRoute(
    onBack: () -> Unit,
    onPublicationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyPublicationsViewModel = hiltViewModel()
) {
    val publications by viewModel.publications.collectAsStateWithLifecycle()
    MyPublicationsScreen(
        publications = publications,
        onBack = onBack,
        onPublicationClick = onPublicationClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPublicationsScreen(
    publications: List<Publication>,
    onBack: () -> Unit,
    onPublicationClick: (String) -> Unit,
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
        if (publications.isEmpty()) {
            Column(
                modifier = modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Aún no tienes publicaciones.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(publications, key = { it.id }) { publication ->
                    PublicationCard(
                        publication = publication,
                        onClick = { onPublicationClick(publication.id) },
                        onToggleInterest = {}
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyPublicationsScreenPreview() {
    RumboTheme {
        MyPublicationsScreen(
            publications = SamplePublications.create().take(1),
            onBack = {},
            onPublicationClick = {}
        )
    }
}
