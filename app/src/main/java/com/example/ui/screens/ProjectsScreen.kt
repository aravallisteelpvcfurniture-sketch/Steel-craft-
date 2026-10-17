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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Project
import com.example.data.model.WorkCategory
import com.example.ui.AppViewModel
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusFabrication
import com.example.ui.theme.StatusInstalled
import com.example.ui.theme.StatusMeasurement
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProjectsScreen(
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    val projects by viewModel.projects.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog state
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProjectForDetail by remember { mutableStateOf<Project?>(null) }

    val filterOptions = listOf(
        "ALL" to "All (${projects.size})",
        "MEASUREMENT" to "Measurement",
        "IN_FABRICATION" to "Fabrication",
        "INSTALLED" to "Installed",
        "COMPLETED" to "Completed"
    )

    val filteredProjects = projects.filter { project ->
        val matchesStatus = if (selectedFilter == "ALL") true else project.status == selectedFilter
        val matchesSearch = if (searchQuery.isBlank()) true else {
            project.customerName.contains(searchQuery, ignoreCase = true) ||
            project.title.contains(searchQuery, ignoreCase = true) ||
            project.siteAddress.contains(searchQuery, ignoreCase = true)
        }
        matchesStatus && matchesSearch
    }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("projects_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Work Orders & Projects (कार्य प्रगति)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Track customer orders, advance payments, fabrication & installation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Search Box
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search customer, site or work...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterOptions) { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { selectedFilter = key },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }

            if (filteredProjects.isEmpty()) {
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
                            Text(
                                text = "No orders found for selected filter",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredProjects) { project ->
                    ProjectDetailCard(
                        project = project,
                        currencyFormat = currencyFormat,
                        onCardClick = { selectedProjectForDetail = project },
                        onCallClick = {
                            if (project.customerPhone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${project.customerPhone}"))
                                context.startActivity(intent)
                            }
                        },
                        onWhatsAppClick = {
                            if (project.customerPhone.isNotBlank()) {
                                val cleanPhone = project.customerPhone.filter { it.isDigit() }
                                val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=Hello%20${project.customerName},%20update%20regarding%20your%20${project.title}%20work%20order."
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        },
                        onStatusAdvance = { nextStatus ->
                            viewModel.updateProjectStatus(project, nextStatus)
                        }
                    )
                }
            }

            // Bottom spacing for FAB
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_project_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Order")
        }
    }

    // New Project Dialog
    if (showAddDialog) {
        AddProjectDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, phone, address, cat, title, sqft, rft, rate, adv, notes ->
                viewModel.createProject(name, phone, address, cat, title, sqft, rft, rate, adv, notes)
                showAddDialog = false
            }
        )
    }

    // Project Detail & Status Dialog
    selectedProjectForDetail?.let { project ->
        ProjectStatusDialog(
            project = project,
            currencyFormat = currencyFormat,
            onDismiss = { selectedProjectForDetail = null },
            onUpdateStatus = { status, newAdv ->
                viewModel.updateProjectStatus(project, status, newAdv)
                selectedProjectForDetail = null
            },
            onDelete = {
                viewModel.deleteProject(project.id)
                selectedProjectForDetail = null
            }
        )
    }
}

@Composable
fun ProjectDetailCard(
    project: Project,
    currencyFormat: NumberFormat,
    onCardClick: () -> Unit,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onStatusAdvance: (String) -> Unit
) {
    val (statusColor, statusText) = when (project.status) {
        "MEASUREMENT" -> StatusMeasurement to "Measurement (माप)"
        "QUOTATION" -> MaterialTheme.colorScheme.primary to "Quotation"
        "IN_FABRICATION" -> StatusFabrication to "In Fabrication (कार्य प्रगति)"
        "INSTALLED" -> StatusInstalled to "Installed"
        "COMPLETED" -> StatusCompleted to "Completed (पूर्ण)"
        else -> MaterialTheme.colorScheme.outline to project.status
    }

    val balanceDue = project.totalAmount - project.advancePaid

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.customerName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }
            }

            if (project.siteAddress.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = project.siteAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing summary row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Amount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(currencyFormat.format(project.totalAmount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column {
                    Text("Advance Paid", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(currencyFormat.format(project.advancePaid), fontWeight = FontWeight.Bold, color = StatusCompleted)
                }
                Column {
                    Text("Balance Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        currencyFormat.format(if (balanceDue > 0) balanceDue else 0.0),
                        fontWeight = FontWeight.Bold,
                        color = if (balanceDue > 0) MaterialTheme.colorScheme.error else StatusCompleted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Actions: Call, WhatsApp, Next Step
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (project.customerPhone.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clickable(onClick = onCallClick)
                                .size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusCompleted.copy(alpha = 0.15f),
                            modifier = Modifier
                                .clickable(onClick = onWhatsAppClick)
                                .size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "WhatsApp", tint = StatusCompleted, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                // Status advance button
                val nextActionText = when (project.status) {
                    "MEASUREMENT" -> "Send Quote" to "QUOTATION"
                    "QUOTATION" -> "Start Fabrication" to "IN_FABRICATION"
                    "IN_FABRICATION" -> "Mark Installed" to "INSTALLED"
                    "INSTALLED" -> "Mark Completed" to "COMPLETED"
                    else -> null
                }

                if (nextActionText != null) {
                    OutlinedButton(
                        onClick = { onStatusAdvance(nextActionText.second) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(nextActionText.first, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddProjectDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, address: String, cat: String, title: String, sqft: Double, rft: Double, rate: Double, adv: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(WorkCategory.STEEL_RAILING) }
    var title by remember { mutableStateOf("") }
    var rft by remember { mutableStateOf("0") }
    var sqft by remember { mutableStateOf("0") }
    var rate by remember { mutableStateOf("850") }
    var advance by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Work Order (नया आर्डर)") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Customer Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Site Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
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
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = rft,
                            onValueChange = { rft = it },
                            label = { Text("Running Ft") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sqft,
                            onValueChange = { sqft = it },
                            label = { Text("Sq Ft") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = rate,
                            onValueChange = { rate = it },
                            label = { Text("Rate (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = advance,
                            onValueChange = { advance = it },
                            label = { Text("Advance (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Specifications & Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name,
                            phone,
                            address,
                            category.id,
                            if (title.isNotBlank()) title else "${category.title} - $name",
                            sqft.toDoubleOrNull() ?: 0.0,
                            rft.toDoubleOrNull() ?: 0.0,
                            rate.toDoubleOrNull() ?: 0.0,
                            advance.toDoubleOrNull() ?: 0.0,
                            notes
                        )
                    }
                }
            ) {
                Text("Create Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProjectStatusDialog(
    project: Project,
    currencyFormat: NumberFormat,
    onDismiss: () -> Unit,
    onUpdateStatus: (String, Double?) -> Unit,
    onDelete: () -> Unit
) {
    var newAdvanceInput by remember { mutableStateOf(project.advancePaid.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(project.customerName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Project: ${project.title}", fontWeight = FontWeight.Bold)
                if (project.siteAddress.isNotBlank()) {
                    Text("Site: ${project.siteAddress}", style = MaterialTheme.typography.bodySmall)
                }
                Text("Total: ${currencyFormat.format(project.totalAmount)}", style = MaterialTheme.typography.bodyMedium)

                OutlinedTextField(
                    value = newAdvanceInput,
                    onValueChange = { newAdvanceInput = it },
                    label = { Text("Update Advance Paid (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Change Status (स्थिति बदलें):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                val statuses = listOf("MEASUREMENT", "QUOTATION", "IN_FABRICATION", "INSTALLED", "COMPLETED")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(statuses) { st ->
                        FilterChip(
                            selected = project.status == st,
                            onClick = {
                                onUpdateStatus(st, newAdvanceInput.toDoubleOrNull())
                            },
                            label = { Text(st, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdateStatus(project.status, newAdvanceInput.toDoubleOrNull())
                }
            ) {
                Text("Update Payment")
            }
        },
        dismissButton = {
            Row {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}
