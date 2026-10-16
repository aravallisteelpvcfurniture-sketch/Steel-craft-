package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Roofing
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CatalogItem
import com.example.data.model.WorkCategory
import com.example.ui.AppViewModel
import com.example.ui.theme.StatusCompleted

@Composable
fun CatalogScreen(
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    val catalog by viewModel.catalog.collectAsState()

    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showAddDesignDialog by remember { mutableStateOf(false) }

    val filteredList = if (selectedCategoryFilter == "ALL") {
        catalog
    } else {
        catalog.filter { it.category == selectedCategoryFilter }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("catalog_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Photo Gallery & Design Portfolio",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Showcase of premium steel railings, aluminium, wooden balcony & interiors for customers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Category Filter Row
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == "ALL",
                            onClick = { selectedCategoryFilter = "ALL" },
                            label = { Text("All Designs (${catalog.size})", fontSize = 12.sp) }
                        )
                    }
                    items(WorkCategory.values()) { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat.id,
                            onClick = { selectedCategoryFilter = cat.id },
                            label = { Text(cat.title, fontSize = 12.sp) }
                        )
                    }
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No designs in this category yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Tap the + button to add your workshop portfolio photos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredList) { item ->
                    CatalogDesignCard(
                        item = item,
                        onInquire = {
                            val msg = "Hello! I am interested in getting a quotation for: ${item.title} (${item.materialGrade}) with estimated range ${item.priceRange}."
                            val url = "https://api.whatsapp.com/send?text=${Uri.encode(msg)}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        FloatingActionButton(
            onClick = { showAddDesignDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_catalog_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Design")
        }
    }

    if (showAddDesignDialog) {
        AddDesignDialog(
            onDismiss = { showAddDesignDialog = false },
            onSave = { title, cat, grade, price, desc ->
                viewModel.addCatalogDesign(title, cat, grade, price, desc)
                showAddDesignDialog = false
            }
        )
    }
}

@Composable
fun CatalogDesignCard(
    item: CatalogItem,
    onInquire: () -> Unit
) {
    val gradientColors = when (item.category) {
        "STEEL_RAILING" -> listOf(Color(0xFF1E293B), Color(0xFF334155))
        "ALUMINIUM_SECTION" -> listOf(Color(0xFF0369A1), Color(0xFF0284C7))
        "WOODEN_BALCONY" -> listOf(Color(0xFF78350F), Color(0xFF92400E))
        "UPVC_DOORS_WINDOWS" -> listOf(Color(0xFF0F766E), Color(0xFF0D9488))
        "HOME_INTERIOR" -> listOf(Color(0xFF4C1D95), Color(0xFF6D28D9))
        else -> listOf(Color(0xFFB45309), Color(0xFFD97706))
    }

    val categoryIcon = when (item.category) {
        "STEEL_RAILING" -> Icons.Default.Handyman
        "ALUMINIUM_SECTION" -> Icons.Default.Roofing
        "WOODEN_BALCONY" -> Icons.Default.Weekend
        "UPVC_DOORS_WINDOWS" -> Icons.Default.DoorFront
        "HOME_INTERIOR" -> Icons.Default.Star
        else -> Icons.Default.Weekend
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Visual Banner Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Brush.horizontalGradient(gradientColors))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = item.category.replace("_", " "),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Specs & Price Details
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Specification / Grade", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(item.materialGrade, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Estimated Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(item.priceRange, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }

                if (item.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onInquire,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusCompleted,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Inquire / WhatsApp Quotation", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddDesignDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, grade: String, price: String, desc: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(WorkCategory.STEEL_RAILING) }
    var grade by remember { mutableStateOf("SS 304 Grade") }
    var price by remember { mutableStateOf("₹750 - ₹950 / Rft") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Portfolio Design") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Design Title (उदा. 304 Balcony Railing)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category:", style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(WorkCategory.values()) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.title, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    label = { Text("Material Grade (सामग्री विवरण)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price Range (उदा. ₹800/sqft)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Features & Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, category.id, grade, price, desc)
                    }
                }
            ) {
                Text("Add Design")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
