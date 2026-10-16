package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Project(
    @DocumentId val id: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val siteAddress: String = "",
    val category: String = "STEEL_RAILING",
    val title: String = "",
    val status: String = "MEASUREMENT",
    val totalSqft: Double = 0.0,
    val runningFeet: Double = 0.0,
    val ratePerUnit: Double = 0.0,
    val totalAmount: Double = 0.0,
    val advancePaid: Double = 0.0,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "customerName" to customerName,
            "customerPhone" to customerPhone,
            "siteAddress" to siteAddress,
            "category" to category,
            "title" to title,
            "status" to status,
            "totalSqft" to totalSqft,
            "runningFeet" to runningFeet,
            "ratePerUnit" to ratePerUnit,
            "totalAmount" to totalAmount,
            "advancePaid" to advancePaid,
            "notes" to notes,
            "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return map
    }
}

@IgnoreExtraProperties
data class CatalogItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val category: String = "STEEL_RAILING",
    val description: String = "",
    val materialGrade: String = "SS 304 Grade",
    val priceRange: String = "₹650 - ₹1,200 / Rft",
    val imageUrl: String = "",
    val isFeatured: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "title" to title,
            "category" to category,
            "description" to description,
            "materialGrade" to materialGrade,
            "priceRange" to priceRange,
            "imageUrl" to imageUrl,
            "isFeatured" to isFeatured,
            "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}

@IgnoreExtraProperties
data class Quotation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val projectId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val siteAddress: String = "",
    val workDescription: String = "",
    val subtotal: Double = 0.0,
    val gstPercent: Double = 18.0,
    val totalAmount: Double = 0.0,
    val advanceAmount: Double = 0.0,
    val balanceDue: Double = 0.0,
    val status: String = "SENT",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "projectId" to projectId,
            "customerName" to customerName,
            "customerPhone" to customerPhone,
            "siteAddress" to siteAddress,
            "workDescription" to workDescription,
            "subtotal" to subtotal,
            "gstPercent" to gstPercent,
            "totalAmount" to totalAmount,
            "advanceAmount" to advanceAmount,
            "balanceDue" to balanceDue,
            "status" to status,
            "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}

enum class WorkCategory(val id: String, val title: String, val hindiLabel: String) {
    STEEL_RAILING("STEEL_RAILING", "Steel Railing Work", "स्टील रेलिंग कार्य"),
    ALUMINIUM_SECTION("ALUMINIUM_SECTION", "Aluminium Fabrication", "एल्युमिनियम सेक्शन कार्य"),
    WOODEN_BALCONY("WOODEN_BALCONY", "Wooden Balcony & Deck", "लकड़ी बालकनी वर्क"),
    UPVC_DOORS_WINDOWS("UPVC_DOORS_WINDOWS", "UPVC Doors & Windows", "यूपीवीसी दरवाजे व खिड़की"),
    HOME_INTERIOR("HOME_INTERIOR", "Home Interior & Decor", "होम इंटीरियर डिजाइन"),
    FURNITURE("FURNITURE", "Custom Furniture", "फर्नीचर व मॉड्यूलर किचन")
}
