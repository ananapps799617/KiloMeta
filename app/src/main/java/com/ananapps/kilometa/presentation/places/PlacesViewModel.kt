// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/places/PlacesViewModel.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ViewModel para la gestión reactiva de lugares favoritos (gasolineras, talleres).
// Controla el filtrado por categoría y las mutaciones de creación/eliminación.
// ==============================================================================

package com.ananapps.kilometa.presentation.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ananapps.kilometa.domain.model.Place
import com.ananapps.kilometa.domain.model.PlaceCategory
import com.ananapps.kilometa.domain.usecase.ManagePlacesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val managePlacesUseCase: ManagePlacesUseCase
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<PlaceCategory?>(null)
    val selectedCategory: StateFlow<PlaceCategory?> = _selectedCategory

    private val _allPlaces = managePlacesUseCase.getAllPlaces()

    val filteredPlaces: StateFlow<List<Place>> = combine(_allPlaces, _selectedCategory) { places, category ->
        if (category == null) {
            places
        } else {
            places.filter { it.category == category }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectCategory(category: PlaceCategory?) {
        _selectedCategory.value = category
    }

    fun savePlace(
        name: String,
        category: PlaceCategory,
        address: String,
        notes: String?,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            managePlacesUseCase.savePlace(
                name = name,
                category = category,
                address = address,
                notes = notes,
                latitude = latitude,
                longitude = longitude
            )
        }
    }

    fun deletePlace(placeId: String) {
        viewModelScope.launch {
            managePlacesUseCase.deletePlace(placeId)
        }
    }
}
