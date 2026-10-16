package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel

@Composable
fun AiStudioScreen(
    viewModel: AppViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("AI Design Studio", "Find Suppliers (Maps)", "AI Consultant")

    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf(com.example.data.remote.GeminiService.getApiKey(context)) }
    var isKeySet by remember { mutableStateOf(com.example.data.remote.GeminiService.isKeyConfigured(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_studio_screen")
    ) {
        // API Key Status Banner
        Surface(
            color = if (isKeySet) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showApiKeyDialog = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isKeySet) Icons.Default.AutoAwesome else Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isKeySet) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isKeySet) "Gemini API Key: Active & Ready" else "Tap here to enter GEMINI_API_KEY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isKeySet) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                Text(
                    text = if (isKeySet) "Change Key" else "Set Key",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (selectedTab) {
            0 -> AiImageGeneratorTab(viewModel)
            1 -> MapsGroundingTab(viewModel)
            2 -> AiConsultantTab(viewModel)
        }
    }

    if (showApiKeyDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("Gemini API Key Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter your Google AI Studio Gemini API Key for image generation, maps grounding, and AI consultant:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("GEMINI_API_KEY") },
                        placeholder = { Text("AIzaSy...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text(
                        text = "Tip: You can get a free key from aistudio.google.com/app/apikey or configure it in the AI Studio Secrets panel.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (apiKeyInput.isNotBlank()) {
                            com.example.data.remote.GeminiService.setCustomApiKey(context, apiKeyInput)
                            isKeySet = true
                            showApiKeyDialog = false
                        }
                    }
                ) {
                    Text("Save Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AiImageGeneratorTab(viewModel: AppViewModel) {
    val generatedResult by viewModel.aiGeneratedImage.collectAsState()
    val isGenerating by viewModel.isGeneratingImage.collectAsState()

    var promptText by remember { mutableStateOf("Ultra-modern luxury apartment balcony with stainless steel 304 railing, 12mm clear toughened glass, teak wood deck floor, and ambient LED profile lights, architectural photography, hyper-realistic") }
    var selectedResolution by remember { mutableStateOf("1K") }

    val resolutions = listOf("1K", "2K", "4K")
    val samplePrompts = listOf(
        "Modern SS 304 balcony glass railing" to "Ultra-modern luxury apartment balcony with stainless steel 304 railing, 12mm clear toughened glass, teak wood deck floor, and ambient LED profile lights, architectural photography",
        "UPVC sliding balcony door" to "Floor-to-ceiling 3-track soundproof UPVC sliding balcony doors in anthracite grey finish opening to a scenic city terrace, bright daylight",
        "Aluminium partition with fluted glass" to "Slim black powder-coated aluminium partition frame with fluted frosted glass panels in an open-concept living room, minimalist design",
        "Wooden balcony pergola" to "Weather-resistant teakwood balcony pergola ceiling with hanging lanterns and glass balustrades",
        "Modular kitchen acrylic" to "Modern L-shaped modular kitchen with high-gloss champagne acrylic cabinets, quartz countertop, and under-cabinet warm LED lighting"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Gemini AI Design Visualizer",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Generate photorealistic designs for client presentations with gemini-3-pro-image-preview",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Image Size Affordance (1K, 2K, 4K)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Image Resolution (छवि गुणवत्ता):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        resolutions.forEach { res ->
                            FilterChip(
                                selected = selectedResolution == res,
                                onClick = { selectedResolution = res },
                                label = { Text("$res Resolution", fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }
            }
        }

        // Prompt Input
        item {
            OutlinedTextField(
                value = promptText,
                onValueChange = { promptText = it },
                label = { Text("Design Prompt (डिजाइन का विवरण)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )
        }

        // Quick Preset Prompts
        item {
            Text("Suggested Inspiration:", style = MaterialTheme.typography.labelSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(samplePrompts) { (label, prompt) ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { promptText = prompt }
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Generate Action Button
        item {
            Button(
                onClick = {
                    if (promptText.isNotBlank()) {
                        viewModel.generateAiDesignImage(promptText, selectedResolution)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("generate_image_button"),
                shape = RoundedCornerShape(12.dp),
                enabled = !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rendering in $selectedResolution...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Design ($selectedResolution)")
                }
            }
        }

        // Output Display Card
        item {
            if (generatedResult != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Text(
                                text = "Rendered Output (${generatedResult?.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "gemini-3-pro-image-preview",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val bitmap = generatedResult?.bitmap
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Generated Design",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            Text(
                                text = generatedResult?.textDescription ?: "Design generated.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapsGroundingTab(viewModel: AppViewModel) {
    val mapsResult by viewModel.mapsResult.collectAsState()
    val isQuerying by viewModel.isQueryingMaps.collectAsState()

    var searchQuery by remember { mutableStateOf("Find Jindal aluminium sections and stainless steel railing hardware dealers") }
    var cityLocation by remember { mutableStateOf("Jaipur, Rajasthan") }

    val quickSearches = listOf(
        "Stainless Steel 304 Pipe Suppliers",
        "Jindal Aluminium Profile Wholesalers",
        "Toughened Glass Processing Factory",
        "UPVC Hardware & Profile Dealers",
        "Plywood & Marine Ply 710 Wholesalers"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Supplier & Raw Material Finder",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Powered by Gemini 3.8 Flash with Google Maps Grounding for real-time location data",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Location & Query Inputs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = cityLocation,
                        onValueChange = { cityLocation = it },
                        label = { Text("City / Area (आपका शहर या क्षेत्र)") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Material or Supplier Type") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                viewModel.searchSuppliersWithMaps(searchQuery, cityLocation)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("search_maps_button"),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isQuerying
                    ) {
                        if (isQuerying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Grounding with Google Maps...")
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Find Local Suppliers")
                        }
                    }
                }
            }
        }

        // Quick Suggestions
        item {
            Text("Popular Material Searches:", style = MaterialTheme.typography.labelSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickSearches) { item ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { searchQuery = item }
                    ) {
                        Text(
                            text = item,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // Grounded Results Display
        item {
            if (mapsResult != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Grounded Places & Suppliers",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        if (mapsResult!!.placesFound.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Verified Places from Google Maps:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            mapsResult!!.placesFound.forEach { place ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = place, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = mapsResult!!.replyText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiConsultantTab(viewModel: AppViewModel) {
    val advice by viewModel.aiAdvice.collectAsState()
    val isQuerying by viewModel.isQueryingAdvice.collectAsState()

    var question by remember { mutableStateOf("What is the difference between SS 304 and SS 316 for exterior balcony railings in Indian climate?") }

    val presetQuestions = listOf(
        "SS 304 vs SS 316 difference" to "What is the difference between SS 304 and SS 316 for exterior balcony railings in Indian climate?",
        "Standard glass thickness" to "What is the standard glass thickness required for a high-rise balcony railing for wind load safety?",
        "Domal aluminium vs standard" to "Explain Jindal Domal 27mm vs 40mm series for soundproof sliding windows.",
        "Marine ply 710 vs commercial" to "Why is IS 710 Marine Plywood necessary for modular kitchens?"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Fabrication & Engineering Consultant",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Ask technical questions on standards, formulas, material grades and IS specifications",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                label = { Text("Your Technical Question (प्रश्न पूछें)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }

        item {
            Text("Sample Topics:", style = MaterialTheme.typography.labelSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(presetQuestions) { (title, q) ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { question = q }
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (question.isNotBlank()) {
                        viewModel.askAiAssistant(question)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                enabled = !isQuerying
            ) {
                if (isQuerying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Consulting Gemini AI...")
                } else {
                    Icon(Icons.Default.Psychology, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Get Technical Advice")
                }
            }
        }

        if (advice != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Consultant Recommendation:",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = advice!!,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
