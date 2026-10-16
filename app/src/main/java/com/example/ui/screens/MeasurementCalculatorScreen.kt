package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.data.model.WorkCategory
import com.example.ui.AppViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MeasurementCalculatorScreen(
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(WorkCategory.STEEL_RAILING) }

    // Inputs
    var lengthFeet by remember { mutableStateOf("20") }
    var lengthInches by remember { mutableStateOf("0") }
    var heightFeet by remember { mutableStateOf("3") }
    var heightInches by remember { mutableStateOf("0") }
    var ratePerUnitInput by remember { mutableStateOf("850") }
    var materialGrade by remember { mutableStateOf("SS 304 Grade") }
    var glassOption by remember { mutableStateOf("10mm Toughened") }
    var includeGst by remember { mutableStateOf(true) }

    // Dialogs
    var showSaveProjectDialog by remember { mutableStateOf(false) }
    var customerNameInput by remember { mutableStateOf("") }
    var customerPhoneInput by remember { mutableStateOf("") }
    var siteAddressInput by remember { mutableStateOf("") }
    var advancePaidInput by remember { mutableStateOf("5000") }

    // Calculations
    val lFeet = (lengthFeet.toDoubleOrNull() ?: 0.0) + ((lengthInches.toDoubleOrNull() ?: 0.0) / 12.0)
    val hFeet = (heightFeet.toDoubleOrNull() ?: 0.0) + ((heightInches.toDoubleOrNull() ?: 0.0) / 12.0)
    val rate = ratePerUnitInput.toDoubleOrNull() ?: 0.0

    val isRunningFeetMode = selectedCategory == WorkCategory.STEEL_RAILING

    val runningFeet = if (isRunningFeetMode) lFeet else 0.0
    val totalSqft = if (isRunningFeetMode) (lFeet * hFeet) else (lFeet * hFeet)

    // Subtotal
    val subtotal = if (isRunningFeetMode) {
        lFeet * rate
    } else {
        totalSqft * rate
    }

    // Material weight estimation (e.g. SS 304 pipe approx 2.5 kg per running foot for typical 2" + balusters)
    val estimatedWeightKg = when (selectedCategory) {
        WorkCategory.STEEL_RAILING -> lFeet * 2.8
        WorkCategory.ALUMINIUM_SECTION -> totalSqft * 1.4
        WorkCategory.UPVC_DOORS_WINDOWS -> totalSqft * 2.2
        else -> 0.0
    }

    val gstAmount = if (includeGst) subtotal * 0.18 else 0.0
    val finalTotal = subtotal + gstAmount

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("measurement_calculator_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Smart Measurement & Cost Calculator",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "माप से लागत व बिल अनुमान (Rft, Sqft, Weight & GST)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Category Switcher
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(WorkCategory.values()) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = cat
                            // Update default rates
                            ratePerUnitInput = when (cat) {
                                WorkCategory.STEEL_RAILING -> "850"
                                WorkCategory.ALUMINIUM_SECTION -> "520"
                                WorkCategory.WOODEN_BALCONY -> "1100"
                                WorkCategory.UPVC_DOORS_WINDOWS -> "750"
                                WorkCategory.HOME_INTERIOR -> "650"
                                WorkCategory.FURNITURE -> "1600"
                            }
                            materialGrade = when (cat) {
                                WorkCategory.STEEL_RAILING -> "SS 304 Jindal"
                                WorkCategory.ALUMINIUM_SECTION -> "Jindal Domal 27mm"
                                WorkCategory.WOODEN_BALCONY -> "Teak / WPC Deck"
                                WorkCategory.UPVC_DOORS_WINDOWS -> "UPVC 60mm 3-Track"
                                WorkCategory.HOME_INTERIOR -> "HDHMR & Louvers"
                                WorkCategory.FURNITURE -> "Marine Ply 710 + Acrylic"
                            }
                        },
                        label = { Text(cat.title, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Measurement Inputs Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Dimensions & Site Measurement",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Length Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = lengthFeet,
                            onValueChange = { lengthFeet = it },
                            label = { Text("Length (Feet)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = lengthInches,
                            onValueChange = { lengthInches = it },
                            label = { Text("Inches (इंच)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Height Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = heightFeet,
                            onValueChange = { heightFeet = it },
                            label = { Text("Height (Feet)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = heightInches,
                            onValueChange = { heightInches = it },
                            label = { Text("Inches (इंच)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rate Input
                    OutlinedTextField(
                        value = ratePerUnitInput,
                        onValueChange = { ratePerUnitInput = it },
                        label = {
                            Text(
                                if (isRunningFeetMode) "Rate per Running Foot (₹/Rft)"
                                else "Rate per Square Foot (₹/Sqft)"
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Material & Glass selection
                    OutlinedTextField(
                        value = materialGrade,
                        onValueChange = { materialGrade = it },
                        label = { Text("Material Grade / Specification") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = glassOption,
                        onValueChange = { glassOption = it },
                        label = { Text("Glass / Infill (कांच का प्रकार)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // GST Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Include 18% GST", fontWeight = FontWeight.SemiBold)
                            Text("Add 18% Goods & Services Tax", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = includeGst,
                            onCheckedChange = { includeGst = it }
                        )
                    }
                }
            }
        }

        // Live Calculation Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Cost & Quantity Summary",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Area (Sq Ft):", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            text = String.format(Locale.US, "%.2f sqft", totalSqft),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (isRunningFeetMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Running Length:", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = String.format(Locale.US, "%.2f Rft", lFeet),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (estimatedWeightKg > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Est. Metal Weight:", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = String.format(Locale.US, "≈ %.1f Kg", estimatedWeightKg),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Base Amount:", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            text = currencyFormat.format(subtotal),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (includeGst) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("GST (18%):", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = currencyFormat.format(gstAmount),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estimated Total (कुल):",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = currencyFormat.format(finalTotal),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { showSaveProjectDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_as_project_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save as Work Order (ऑर्डर सेव करें)")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareText = """
                                *SteelCraft Pro Quotation / Estimation*
                                Category: ${selectedCategory.title}
                                Material: $materialGrade
                                Infill: $glassOption
                                Length: ${String.format(Locale.US, "%.1f", lFeet)} ft, Height: ${String.format(Locale.US, "%.1f", hFeet)} ft
                                Total Area: ${String.format(Locale.US, "%.1f", totalSqft)} sqft
                                Rate: ₹$rate / ${if (isRunningFeetMode) "Rft" else "Sqft"}
                                Estimated Total: ${currencyFormat.format(finalTotal)} (incl. GST)
                                
                                For bookings & site measurement, contact SteelCraft Pro!
                            """.trimIndent()

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Estimate via WhatsApp / SMS"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Quote", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.createQuotation(
                                customerName = if (customerNameInput.isNotBlank()) customerNameInput else "Client Estimate",
                                customerPhone = customerPhoneInput,
                                siteAddress = siteAddressInput,
                                description = "${selectedCategory.title} ($materialGrade) - ${String.format(Locale.US, "%.1f", totalSqft)} Sqft",
                                subtotal = subtotal,
                                gstPercent = if (includeGst) 18.0 else 0.0,
                                advanceAmount = advancePaidInput.toDoubleOrNull() ?: 0.0
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Make Bill", fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // Save Project Dialog
    if (showSaveProjectDialog) {
        AlertDialog(
            onDismissRequest = { showSaveProjectDialog = false },
            title = { Text("Save Work Order") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = customerNameInput,
                        onValueChange = { customerNameInput = it },
                        label = { Text("Customer Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customerPhoneInput,
                        onValueChange = { customerPhoneInput = it },
                        label = { Text("Customer Phone (मोबाइल नंबर)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = siteAddressInput,
                        onValueChange = { siteAddressInput = it },
                        label = { Text("Site Address (साइट पता)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = advancePaidInput,
                        onValueChange = { advancePaidInput = it },
                        label = { Text("Advance Received (अग्रिम राशि ₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customerNameInput.isNotBlank()) {
                            viewModel.createProject(
                                customerName = customerNameInput,
                                customerPhone = customerPhoneInput,
                                siteAddress = siteAddressInput,
                                category = selectedCategory.id,
                                title = "${selectedCategory.title} - $customerNameInput",
                                totalSqft = totalSqft,
                                runningFeet = runningFeet,
                                ratePerUnit = rate,
                                advancePaid = advancePaidInput.toDoubleOrNull() ?: 0.0,
                                notes = "Material: $materialGrade, Glass/Infill: $glassOption"
                            )
                            showSaveProjectDialog = false
                        }
                    }
                ) {
                    Text("Save Order")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveProjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
