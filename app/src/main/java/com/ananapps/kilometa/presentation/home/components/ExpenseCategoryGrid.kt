// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/ExpenseCategoryGrid.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Cuadrícula ergonómica de 2 columnas para selección de conceptos de gasto operativo.
// Soporta categorías estándar y dinámicas con botón [ ➕ Nueva... ] (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.ExpenseConcept
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

data class ExpenseConceptItem(
    val id: String,
    val name: String,
    val icon: String,
    val isAdd: Boolean = false,
    val concept: ExpenseConcept? = null
)

@Composable
fun ExpenseCategoryGrid(
    selectedConcept: ExpenseConcept,
    selectedCustomName: String?,
    customCategories: List<Pair<String, String>>,
    onSelectConcept: (ExpenseConcept, String?) -> Unit,
    onAddNewClicked: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val allItems = ExpenseConcept.entries.map {
        ExpenseConceptItem(it.name, it.displayName, it.icon, false, it)
    } + customCategories.map {
        ExpenseConceptItem(it.first, it.first, it.second, false, ExpenseConcept.OTHER)
    } + listOf(ExpenseConceptItem("ADD_NEW", "Nueva...", "➕", true, null))

    val rows = allItems.chunked(2)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { pair ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { item ->
                    val isSelected = if (item.isAdd) {
                        false
                    } else if (item.concept != ExpenseConcept.OTHER) {
                        selectedConcept == item.concept
                    } else {
                        selectedConcept == ExpenseConcept.OTHER && (selectedCustomName == item.name || (selectedCustomName == null && item.id == "OTHER"))
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable {
                                if (item.isAdd) {
                                    onAddNewClicked()
                                } else {
                                    item.concept?.let { concept ->
                                        val customName = if (item.id != "OTHER" && concept == ExpenseConcept.OTHER) item.name else null
                                        onSelectConcept(concept, customName)
                                    }
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) accentColor.copy(alpha = 0.25f) else DeepObsidianSurfaceVariant,
                        border = if (isSelected) BorderStroke(1.5.dp, accentColor) else null
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(text = item.icon, fontSize = 18.sp, modifier = Modifier.padding(end = 6.dp))
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Color.White else if (item.isAdd) ElectricBluePrimary else TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
