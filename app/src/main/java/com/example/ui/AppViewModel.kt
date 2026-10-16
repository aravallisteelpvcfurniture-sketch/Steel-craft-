package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CatalogItem
import com.example.data.model.Project
import com.example.data.model.Quotation
import com.example.data.model.WorkCategory
import com.example.data.remote.GeminiResult
import com.example.data.remote.GeminiService
import com.example.data.remote.GeneratedImageResult
import com.example.data.remote.MapsGroundingResult
import com.example.data.repository.CatalogRepository
import com.example.data.repository.ProjectRepository
import com.example.data.repository.QuotationRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    PROJECTS,
    CALCULATOR,
    CATALOG,
    AI_STUDIO
}

data class UiNotification(val message: String, val isError: Boolean = false)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val projectRepo = ProjectRepository(application)
    private val catalogRepo = CatalogRepository(application)
    private val quotationRepo = QuotationRepository(application)

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _catalog = MutableStateFlow<List<CatalogItem>>(emptyList())
    val catalog: StateFlow<List<CatalogItem>> = _catalog.asStateFlow()

    private val _quotations = MutableStateFlow<List<Quotation>>(emptyList())
    val quotations: StateFlow<List<Quotation>> = _quotations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _notification = MutableStateFlow<UiNotification?>(null)
    val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

    // Gemini AI Studio states
    private val _aiGeneratedImage = MutableStateFlow<GeneratedImageResult?>(null)
    val aiGeneratedImage: StateFlow<GeneratedImageResult?> = _aiGeneratedImage.asStateFlow()

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage: StateFlow<Boolean> = _isGeneratingImage.asStateFlow()

    private val _mapsResult = MutableStateFlow<MapsGroundingResult?>(null)
    val mapsResult: StateFlow<MapsGroundingResult?> = _mapsResult.asStateFlow()

    private val _isQueryingMaps = MutableStateFlow(false)
    val isQueryingMaps: StateFlow<Boolean> = _isQueryingMaps.asStateFlow()

    private val _aiAdvice = MutableStateFlow<String?>(null)
    val aiAdvice: StateFlow<String?> = _aiAdvice.asStateFlow()

    private val _isQueryingAdvice = MutableStateFlow(false)
    val isQueryingAdvice: StateFlow<Boolean> = _isQueryingAdvice.asStateFlow()

    init {
        loadData()
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun clearNotification() {
        _notification.value = null
    }

    fun loadData() {
        val user = com.google.firebase.Firebase.auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                try {
                    catalogRepo.seedDefaultsIfEmpty()
                } catch (_: Exception) {}

                launch {
                    projectRepo.observeProjects()
                        .catch { emit(emptyList()) }
                        .collect { list ->
                            _projects.value = list.sortedByDescending { it.createdAt }
                        }
                }

                launch {
                    catalogRepo.observeCatalog()
                        .catch { emit(emptyList()) }
                        .collect { list ->
                            _catalog.value = list
                        }
                }

                launch {
                    quotationRepo.observeQuotations()
                        .catch { emit(emptyList()) }
                        .collect { list ->
                            _quotations.value = list.sortedByDescending { it.createdAt }
                        }
                }
            }
        } else {
            // Guest mode: populate initial showcase catalog and demo orders so app is immediately usable
            if (_catalog.value.isEmpty()) {
                _catalog.value = listOf(
                    CatalogItem(
                        id = "cat_1",
                        title = "SS 304 Glass Balcony Railing",
                        category = "STEEL_RAILING",
                        description = "Premium 304 Grade Stainless Steel post with 10mm toughened glass & round handrail",
                        materialGrade = "Jindal SS 304 + 10mm Toughened Glass",
                        priceRange = "₹750 - ₹950 / Rft",
                        isFeatured = true
                    ),
                    CatalogItem(
                        id = "cat_2",
                        title = "Aluminium Domal Section Sliding Window",
                        category = "ALUMINIUM_SECTION",
                        description = "3-Track heavy domal section sliding window with mosquito mesh and powder coating",
                        materialGrade = "27mm / 40mm Domal Section Powder Coated",
                        priceRange = "₹480 - ₹620 / Sqft",
                        isFeatured = true
                    ),
                    CatalogItem(
                        id = "cat_3",
                        title = "Wooden & Steel Balcony Pergola Deck",
                        category = "WOODEN_BALCONY",
                        description = "Weather-resistant teakwood finish balcony ceiling, wooden handrail & steel framing",
                        materialGrade = "Composite Wood / Teak Wood + SS Support",
                        priceRange = "₹850 - ₹1,400 / Sqft",
                        isFeatured = true
                    ),
                    CatalogItem(
                        id = "cat_4",
                        title = "UPVC Soundproof Casement Door",
                        category = "UPVC_DOORS_WINDOWS",
                        description = "Multi-point locking German hardware UPVC balcony casement door with double glazing",
                        materialGrade = "Heavy UPVC 60mm Profile + 5mm+6A+5mm DGU Glass",
                        priceRange = "₹650 - ₹900 / Sqft",
                        isFeatured = true
                    ),
                    CatalogItem(
                        id = "cat_5",
                        title = "Modular Kitchen Acrylic & Marine Ply",
                        category = "FURNITURE",
                        description = "Waterproof BWP Marine Plywood base with high gloss acrylic finish & soft close hinges",
                        materialGrade = "710 Grade Marine Plywood + 1.5mm Acrylic Sheet",
                        priceRange = "₹1,400 - ₹2,200 / Sqft",
                        isFeatured = true
                    ),
                    CatalogItem(
                        id = "cat_6",
                        title = "Modern TV Unit & Fluted Panel Decor",
                        category = "HOME_INTERIOR",
                        description = "Charcoal fluted wall paneling with LED backlighting and floating console drawer unit",
                        materialGrade = "HDHMR Board + Fluted PVC Louvers",
                        priceRange = "₹550 - ₹850 / Sqft",
                        isFeatured = true
                    )
                )
            }
            if (_projects.value.isEmpty()) {
                _projects.value = listOf(
                    Project(
                        id = "sample_p1",
                        customerName = "Sharma Residence",
                        customerPhone = "+91 9876543210",
                        siteAddress = "Plot 12, Civil Lines",
                        category = "STEEL_RAILING",
                        title = "Front Balcony SS 304 Railing",
                        status = "IN_FABRICATION",
                        totalSqft = 120.0,
                        runningFeet = 40.0,
                        ratePerUnit = 850.0,
                        totalAmount = 34000.0,
                        advancePaid = 15000.0,
                        notes = "10mm toughened glass infill with Jindal pipe"
                    ),
                    Project(
                        id = "sample_p2",
                        customerName = "Gupta Apartments",
                        customerPhone = "+91 9812345678",
                        siteAddress = "Flat 402, Royal Heights",
                        category = "UPVC_DOORS_WINDOWS",
                        title = "Balcony 3-Track UPVC Sliding Door",
                        status = "MEASUREMENT",
                        totalSqft = 96.0,
                        runningFeet = 0.0,
                        ratePerUnit = 750.0,
                        totalAmount = 72000.0,
                        advancePaid = 20000.0,
                        notes = "Soundproof double glass 5mm+6A+5mm"
                    )
                )
            }
        }
    }

    fun createProject(
        customerName: String,
        customerPhone: String,
        siteAddress: String,
        category: String,
        title: String,
        totalSqft: Double,
        runningFeet: Double,
        ratePerUnit: Double,
        advancePaid: Double,
        notes: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val total = if (totalSqft > 0 && ratePerUnit > 0) {
                totalSqft * ratePerUnit
            } else if (runningFeet > 0 && ratePerUnit > 0) {
                runningFeet * ratePerUnit
            } else 0.0

            val project = Project(
                customerName = customerName,
                customerPhone = customerPhone,
                siteAddress = siteAddress,
                category = category,
                title = title.ifBlank { "$customerName - $category" },
                status = "MEASUREMENT",
                totalSqft = totalSqft,
                runningFeet = runningFeet,
                ratePerUnit = ratePerUnit,
                totalAmount = total,
                advancePaid = advancePaid,
                notes = notes
            )

            if (com.google.firebase.Firebase.auth.currentUser != null) {
                val result = projectRepo.createProject(project)
                _isLoading.value = false
                if (result.isSuccess) {
                    _notification.value = UiNotification("Project saved to Cloud!")
                    _currentScreen.value = AppScreen.PROJECTS
                } else {
                    _notification.value = UiNotification("Failed to save: ${result.exceptionOrNull()?.message}", true)
                }
            } else {
                val localProject = project.copy(id = java.util.UUID.randomUUID().toString())
                _projects.value = listOf(localProject) + _projects.value
                _isLoading.value = false
                _notification.value = UiNotification("Project saved locally!")
                _currentScreen.value = AppScreen.PROJECTS
            }
        }
    }

    fun updateProjectStatus(project: Project, newStatus: String, newAdvance: Double? = null) {
        viewModelScope.launch {
            val updated = project.copy(
                status = newStatus,
                advancePaid = newAdvance ?: project.advancePaid
            )
            _projects.value = _projects.value.map { if (it.id == project.id) updated else it }
            if (com.google.firebase.Firebase.auth.currentUser != null) {
                projectRepo.updateProject(updated)
            }
            _notification.value = UiNotification("Project updated to $newStatus")
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            _projects.value = _projects.value.filter { it.id != projectId }
            if (com.google.firebase.Firebase.auth.currentUser != null) {
                projectRepo.deleteProject(projectId)
            }
            _notification.value = UiNotification("Project deleted")
        }
    }

    fun addCatalogDesign(
        title: String,
        category: String,
        materialGrade: String,
        priceRange: String,
        description: String
    ) {
        viewModelScope.launch {
            val item = CatalogItem(
                title = title,
                category = category,
                materialGrade = materialGrade,
                priceRange = priceRange,
                description = description,
                isFeatured = true
            )
            val result = catalogRepo.addCatalogItem(item)
            if (result.isSuccess) {
                _notification.value = UiNotification("Design added to portfolio!")
            } else {
                _notification.value = UiNotification("Failed to add design", true)
            }
        }
    }

    fun createQuotation(
        customerName: String,
        customerPhone: String,
        siteAddress: String,
        description: String,
        subtotal: Double,
        gstPercent: Double,
        advanceAmount: Double
    ) {
        viewModelScope.launch {
            val total = subtotal + (subtotal * gstPercent / 100.0)
            val balance = total - advanceAmount
            val quotation = Quotation(
                customerName = customerName,
                customerPhone = customerPhone,
                siteAddress = siteAddress,
                workDescription = description,
                subtotal = subtotal,
                gstPercent = gstPercent,
                totalAmount = total,
                advanceAmount = advanceAmount,
                balanceDue = balance,
                status = "SENT"
            )
            val result = quotationRepo.createQuotation(quotation)
            if (result.isSuccess) {
                _notification.value = UiNotification("Quotation generated successfully!")
            }
        }
    }

    // --- Gemini AI Actions ---

    fun generateAiDesignImage(prompt: String, size: String) {
        viewModelScope.launch {
            _isGeneratingImage.value = true
            when (val res = GeminiService.generateDesignImage(prompt, size)) {
                is GeminiResult.Success -> {
                    _aiGeneratedImage.value = res.data
                }
                is GeminiResult.Error -> {
                    _notification.value = UiNotification(res.message, true)
                }
            }
            _isGeneratingImage.value = false
        }
    }

    fun searchSuppliersWithMaps(query: String, city: String) {
        viewModelScope.launch {
            _isQueryingMaps.value = true
            when (val res = GeminiService.queryWithMapsGrounding(query, city)) {
                is GeminiResult.Success -> {
                    _mapsResult.value = res.data
                }
                is GeminiResult.Error -> {
                    _notification.value = UiNotification(res.message, true)
                }
            }
            _isQueryingMaps.value = false
        }
    }

    fun askAiAssistant(query: String) {
        viewModelScope.launch {
            _isQueryingAdvice.value = true
            when (val res = GeminiService.getFabricationAdvice(query, isFast = false)) {
                is GeminiResult.Success -> {
                    _aiAdvice.value = res.data
                }
                is GeminiResult.Error -> {
                    _notification.value = UiNotification(res.message, true)
                }
            }
            _isQueryingAdvice.value = false
        }
    }
}
