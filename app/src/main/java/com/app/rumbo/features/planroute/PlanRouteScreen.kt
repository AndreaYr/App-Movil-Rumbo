package com.app.rumbo.features.planroute

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R
import com.app.rumbo.core.theme.Coral
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.core.theme.TealContainer
import com.app.rumbo.core.theme.TealDark
import com.app.rumbo.core.util.formatKm
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.RoutePreferences
import com.app.rumbo.domain.model.RoutePriority
import com.app.rumbo.domain.model.Transport

@Composable
fun PlanRouteRoute(
    onBack: () -> Unit,
    onViewRouteClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlanRouteViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PlanRouteScreen(
        state = state,
        onBack = onBack,
        onHoursChange = viewModel::onHoursChange,
        onBudgetChange = viewModel::onBudgetChange,
        onCategoryToggle = viewModel::onCategoryToggle,
        onTransportChange = viewModel::onTransportChange,
        onPriorityChange = viewModel::onPriorityChange,
        onViewRouteClick = onViewRouteClick,
        modifier = modifier
    )
}

@Composable
fun PlanRouteScreen(
    state: PlanRouteUiState,
    onBack: () -> Unit,
    onHoursChange: (Int) -> Unit,
    onBudgetChange: (PriceLevel) -> Unit,
    onCategoryToggle: (Category) -> Unit,
    onTransportChange: (Transport) -> Unit,
    onPriorityChange: (RoutePriority) -> Unit,
    onViewRouteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val preferences = state.preferences
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 6.dp)
    ) {
        Header(onBack)
        SectionLabel("Punto de inicio", Modifier.padding(top = 20.dp))
        StartCard(state.city)

        TimeSection(preferences.hours, onHoursChange)

        SectionLabel("Presupuesto por persona", Modifier.padding(top = 20.dp), value = preferences.budget.label)
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            PriceLevel.entries.forEach { level ->
                OptionBox(
                    selected = level == preferences.budget,
                    onClick = { onBudgetChange(level) },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.weight(1f)
                ) { tint ->
                    Text(level.label, style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp), color = tint, maxLines = 1)
                }
            }
        }

        SectionLabel("Categorías de interés", Modifier.padding(top = 20.dp))
        CategoryChips(preferences.categories, onCategoryToggle)

        SectionLabel("Medio de transporte", Modifier.padding(top = 20.dp))
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Transport.entries.forEach { transport ->
                OptionBox(
                    selected = transport == preferences.transport,
                    onClick = { onTransportChange(transport) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(1f)
                ) { tint ->
                    Icon(painterResource(transport.iconRes), null, Modifier.size(21.dp), tint = tint)
                    Text(
                        transport.label,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                        color = tint,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        SectionLabel("Prioriza", Modifier.padding(top = 20.dp))
        PrioritySelector(preferences.priority, onPriorityChange)

        PlanSummary(state, Modifier.padding(top = 22.dp))
        ViewRouteButton(enabled = state.plan.stops.isNotEmpty(), onClick = onViewRouteClick)
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    val shape = MaterialTheme.shapes.small
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, shape)
                .clickable(role = Role.Button, onClickLabel = "Atrás", onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_arrow_back), "Atrás", Modifier.size(20.dp))
        }
        Column {
            Text("Planifica tu ruta", style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp))
            Text(
                "Ajusta y la ruta se recalcula al instante",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier, value: String? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Text(text, style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp), color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun StartCard(city: String) {
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(painterResource(R.drawable.ic_my_location), null, Modifier.size(19.dp), tint = MaterialTheme.colorScheme.primary)
        Column {
            Text("Mi ubicación", style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.5.sp))
            if (city.isNotBlank()) {
                Text(
                    city,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun TimeSection(hours: Int, onHoursChange: (Int) -> Unit) {
    SectionLabel("Tiempo disponible", Modifier.padding(top = 22.dp), value = "$hours horas")
    Slider(
        value = hours.toFloat(),
        onValueChange = { onHoursChange(it.toInt()) },
        valueRange = RoutePreferences.MIN_HOURS.toFloat()..RoutePreferences.MAX_HOURS.toFloat(),
        steps = RoutePreferences.MAX_HOURS - RoutePreferences.MIN_HOURS - 1,
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.surface,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.outline,
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent
        )
    )
    Row(Modifier.fillMaxWidth()) {
        val style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp)
        Text("${RoutePreferences.MIN_HOURS} h", style = style, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text("Todo el día", style = style, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(selected: Set<Category>, onToggle: (Category) -> Unit) {
    FlowRow(
        modifier = Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Category.entries.forEach { category ->
            val isSelected = category in selected
            OptionBox(
                selected = isSelected,
                onClick = { onToggle(category) },
                shape = RoundedCornerShape(percent = 50),
                horizontal = true
            ) { tint ->
                Icon(
                    painterResource(if (isSelected) R.drawable.ic_check_circle else R.drawable.ic_add),
                    null,
                    Modifier.size(15.dp),
                    tint = tint
                )
                Text(category.label, style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp), color = tint, modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}

@Composable
private fun PrioritySelector(selected: RoutePriority, onChange: (RoutePriority) -> Unit) {
    Row(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .background(Color(0xFFEFE9DE), MaterialTheme.shapes.medium)
            .padding(4.dp)
    ) {
        RoutePriority.entries.forEach { priority ->
            val isSelected = priority == selected
            Text(
                priority.label,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable(role = Role.RadioButton, onClick = { onChange(priority) })
                    .padding(vertical = 11.dp)
            )
        }
    }
}

@Composable
private fun PlanSummary(state: PlanRouteUiState, modifier: Modifier = Modifier) {
    val plan = state.plan
    val shape = MaterialTheme.shapes.large
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TealContainer, shape)
            .border(1.dp, Color(0xFFCFE3DD), shape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(painterResource(R.drawable.ic_bolt), null, Modifier.size(22.dp), tint = TealDark)
        Column {
            Text(
                if (plan.stops.isEmpty()) "Sin paradas con estos parámetros"
                else "Ruta lista: ${plan.stops.size} ${if (plan.stops.size == 1) "parada" else "paradas"} · ${plan.distanceKm.formatKm()}",
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.5.sp),
                color = TealDark
            )
            Text(
                if (plan.stops.isEmpty()) "Prueba con más tiempo, otro presupuesto o más categorías"
                else "Se recalcula cada vez que cambias un parámetro",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color(0xFF3F5B52),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ViewRouteButton(enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(top = 14.dp, bottom = 24.dp)
            .fillMaxWidth()
            .height(54.dp)
            .clip(MaterialTheme.shapes.large)
            .background(if (enabled) Coral else Coral.copy(alpha = 0.4f))
            .then(if (enabled) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(painterResource(R.drawable.ic_route), null, Modifier.size(21.dp), tint = Color.White)
        Text("Ver mi ruta", style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.5.sp), color = Color.White, modifier = Modifier.padding(start = 9.dp))
    }
}

/** Caja seleccionable (presupuesto, categoría, transporte): el contenido recibe el color según el estado. */
@Composable
private fun OptionBox(
    selected: Boolean,
    onClick: () -> Unit,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier,
    horizontal: Boolean = false,
    content: @Composable (tint: Color) -> Unit
) {
    val tint = if (selected) TealDark else MaterialTheme.colorScheme.onSurfaceVariant
    val padding = if (horizontal) Modifier.padding(horizontal = 12.dp, vertical = 10.dp) else Modifier.padding(vertical = 12.dp)
    val boxModifier = modifier
        .clip(shape)
        .background(if (selected) TealContainer else MaterialTheme.colorScheme.surface)
        .border(1.5.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, shape)
        .clickable(role = Role.Button, onClick = onClick)
        .then(padding)
    if (horizontal) {
        Row(boxModifier, verticalAlignment = Alignment.CenterVertically) { content(tint) }
    } else {
        Column(boxModifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { content(tint) }
    }
}

@get:DrawableRes
private val Transport.iconRes: Int
    get() = when (this) {
        Transport.WALK -> R.drawable.ic_directions_walk
        Transport.BIKE -> R.drawable.ic_pedal_bike
        Transport.CAR -> R.drawable.ic_directions_car
        Transport.JEEP -> R.drawable.ic_local_taxi
    }

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun PlanRouteScreenPreview() {
    RumboTheme {
        PlanRouteScreen(
            state = PlanRouteUiState(isLoading = false, city = "Armenia, Quindío"),
            onBack = {},
            onHoursChange = {},
            onBudgetChange = {},
            onCategoryToggle = {},
            onTransportChange = {},
            onPriorityChange = {},
            onViewRouteClick = {}
        )
    }
}
