package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.CatalogItem
import com.example.data.model.Project
import com.example.data.model.Quotation
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ProjectRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    fun observeProjects(): Flow<List<Project>> = flow {
        val uid = requireUserId()
        val path = "projects"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot -> snapshot.toObjects<Project>() }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createProject(project: Project): Result<String> {
        val uid = requireUserId()
        val path = "projects"
        return try {
            val projectWithUser = project.copy(userId = uid)
            val docRef = db.collection(path).document()
            docRef.set(projectWithUser.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateProject(project: Project): Result<Unit> {
        val uid = requireUserId()
        require(project.id.isNotBlank()) { "Project ID must not be blank" }
        val docRef = db.collection("projects").document(project.id)
        return try {
            val payload = project.copy(userId = uid).toMap()
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteProject(projectId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("projects").document(projectId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getProjectById(projectId: String): Result<Project?> {
        val uid = requireUserId()
        val docRef = db.collection("projects").document(projectId)
        return try {
            val doc = docRef.get().await()
            val project = doc.toObject(Project::class.java)
            if (project != null && project.userId == uid) {
                Result.success(project)
            } else {
                Result.failure(SecurityException("Unauthorized access to project"))
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }
}

class CatalogRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    fun observeCatalog(): Flow<List<CatalogItem>> = flow {
        val uid = requireUserId()
        val path = "catalog"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot -> snapshot.toObjects<CatalogItem>() }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addCatalogItem(item: CatalogItem): Result<String> {
        val uid = requireUserId()
        val path = "catalog"
        return try {
            val docRef = db.collection(path).document()
            docRef.set(item.copy(userId = uid).toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun getDefaultCatalogItems(): List<CatalogItem> {
        return listOf(
            CatalogItem(
                id = "def_1",
                title = "SS 304 Glass Balcony Railing",
                category = "STEEL_RAILING",
                description = "Premium 304 Grade Stainless Steel post with 10mm toughened glass & round handrail",
                materialGrade = "Jindal SS 304 + 10mm Toughened Glass",
                priceRange = "₹750 - ₹950 / Rft",
                isFeatured = true
            ),
            CatalogItem(
                id = "def_2",
                title = "Aluminium Domal Section Sliding Window",
                category = "ALUMINIUM_SECTION",
                description = "3-Track heavy domal section sliding window with mosquito mesh and powder coating",
                materialGrade = "27mm / 40mm Domal Section Powder Coated",
                priceRange = "₹480 - ₹620 / Sqft",
                isFeatured = true
            ),
            CatalogItem(
                id = "def_3",
                title = "Wooden & Steel Balcony Pergola Deck",
                category = "WOODEN_BALCONY",
                description = "Weather-resistant teakwood finish balcony ceiling, wooden handrail & steel framing",
                materialGrade = "Composite Wood / Teak Wood + SS Support",
                priceRange = "₹850 - ₹1,400 / Sqft",
                isFeatured = true
            ),
            CatalogItem(
                id = "def_4",
                title = "UPVC Soundproof Casement Door",
                category = "UPVC_DOORS_WINDOWS",
                description = "Multi-point locking German hardware UPVC balcony casement door with double glazing",
                materialGrade = "Heavy UPVC 60mm Profile + 5mm+6A+5mm DGU Glass",
                priceRange = "₹650 - ₹900 / Sqft",
                isFeatured = true
            ),
            CatalogItem(
                id = "def_5",
                title = "Modular Kitchen Acrylic & Marine Ply",
                category = "FURNITURE",
                description = "Waterproof BWP Marine Plywood base with high gloss acrylic finish & soft close hinges",
                materialGrade = "710 Grade Marine Plywood + 1.5mm Acrylic Sheet",
                priceRange = "₹1,400 - ₹2,200 / Sqft",
                isFeatured = true
            ),
            CatalogItem(
                id = "def_6",
                title = "Modern TV Unit & Fluted Panel Decor",
                category = "HOME_INTERIOR",
                description = "Charcoal fluted wall paneling with LED backlighting and floating console drawer unit",
                materialGrade = "HDHMR Board + Fluted PVC Louvers",
                priceRange = "₹550 - ₹850 / Sqft",
                isFeatured = true
            )
        )
    }

    suspend fun seedDefaultsIfEmpty() {
        val uid = requireUserId()
        val path = "catalog"
        try {
            val snapshot = db.collection(path).whereEqualTo("userId", uid).limit(1).get().await()
            if (snapshot.isEmpty) {
                val defaults = getDefaultCatalogItems()
                for (item in defaults) {
                    addCatalogItem(item)
                }
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
        }
    }
}

class QuotationRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    fun observeQuotations(): Flow<List<Quotation>> = flow {
        val uid = requireUserId()
        val path = "quotations"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot -> snapshot.toObjects<Quotation>() }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createQuotation(quotation: Quotation): Result<String> {
        val uid = requireUserId()
        val path = "quotations"
        return try {
            val docRef = db.collection(path).document()
            docRef.set(quotation.copy(userId = uid).toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteQuotation(quotationId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("quotations").document(quotationId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
