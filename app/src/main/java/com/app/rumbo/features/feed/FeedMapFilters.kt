package com.app.rumbo.features.feed

import com.app.rumbo.core.util.label
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.app.rumbo.R
import androidx.compose.ui.unit.dp
import java.text.Normalizer
import com.app.rumbo.core.component.SelectField
import com.app.rumbo.data.local.Department
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.Publication

/** Filtros del mapa de Explorar. Mientras no haya ninguno activo, el mapa no muestra publicaciones. */
data class MapFilters(
    val categories: Set<Category> = emptySet(),
    val interested: Boolean = false,
    val visited: Boolean = false,
    val department: String? = null,
    val town: String? = null
) {
    val isActive: Boolean get() = categories.isNotEmpty() || interested || visited || department != null || town != null

    /** Cantidad de criterios activos; se muestra como insignia en el botón de filtro. */
    val count: Int get() = (if (categories.isNotEmpty()) 1 else 0) + (if (interested) 1 else 0) +
        (if (visited) 1 else 0) + (if (department != null) 1 else 0) + (if (town != null) 1 else 0)

    /**
     * @param departmentTowns municipios del departamento elegido, ya normalizados con [normalizeName].
     * Solo se usa con publicaciones antiguas que no guardan su departamento.
     */
    private fun departmentMatches(publication: Publication, publicationTown: String, departmentTowns: Set<String>): Boolean =
        // Si la publicación ya trae su departamento se compara directo; si no, se deduce por el municipio
        if (publication.department.isNotBlank()) normalizeName(publication.department) == normalizeName(department.orEmpty())
        else publicationTown in departmentTowns

    fun matches(publication: Publication, departmentTowns: Set<String> = emptySet()): Boolean {
        val publicationTown = normalizeName(publication.town)
        return (categories.isEmpty() || publication.category in categories) &&
            (!interested || publication.interested) &&
            (!visited || publication.visited) &&
            (department == null || departmentMatches(publication, publicationTown, departmentTowns)) &&
            (town == null || publicationTown == normalizeName(town))
    }
}

/** Minúsculas y sin tildes, para comparar nombres de municipios sin que importe cómo estén escritos. */
internal fun normalizeName(name: String): String =
    Normalizer.normalize(name, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").trim().lowercase()

/** Hoja con los filtros del mapa: categorías, Me interesa, Visitado y ciudad. Se aplican al tocar "Aplicar". */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun MapFilterSheet(
    current: MapFilters,
    departments: List<Department>,
    onApply: (MapFilters) -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember { mutableStateOf(current) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Filtrar lugares", style = MaterialTheme.typography.titleLarge)

            val municipalities = departments.firstOrNull { it.name == draft.department }?.municipalities.orEmpty()
            SelectField(
                label = "Departamento",
                placeholder = "Todos los departamentos",
                selected = draft.department,
                options = departments.map { it.name },
                // Al cambiar de departamento la ciudad elegida deja de valer
                onSelected = { draft = draft.copy(department = it, town = null) }
            )
            SelectField(
                label = "Ciudad o municipio",
                placeholder = if (draft.department == null) "Elige primero un departamento" else "Todos los municipios",
                selected = draft.town,
                options = municipalities,
                enabled = draft.department != null,
                onSelected = { draft = draft.copy(town = it) }
            )

            FilterGroup("Categoría") {
                Category.entries.forEach { category ->
                    FilterChip(
                        selected = category in draft.categories,
                        onClick = {
                            val updated = if (category in draft.categories) draft.categories - category else draft.categories + category
                            draft = draft.copy(categories = updated)
                        },
                        label = { Text(category.label) }
                    )
                }
            }

            FilterGroup("Mis lugares") {
                FilterChip(
                    selected = draft.interested,
                    onClick = { draft = draft.copy(interested = !draft.interested) },
                    label = { Text("Me interesa") }
                )
                FilterChip(
                    selected = draft.visited,
                    onClick = { draft = draft.copy(visited = !draft.visited) },
                    label = { Text("Visitado") }
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onApply(MapFilters()) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.medium
                ) { Text("Limpiar") }
                Button(
                    onClick = { onApply(draft) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.medium
                ) { Text("Aplicar") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) { content() }
    }
}
