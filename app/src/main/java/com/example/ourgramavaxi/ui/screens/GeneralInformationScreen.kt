package com.example.ourgramavaxi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ourgramavaxi.R

data class InformationSection(
    val titleResId: Int,
    val contentResId: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralInformationScreen(navController: NavHostController) {
    val sections = listOf(
        InformationSection(R.string.info_about_app, R.string.info_about_app_content),
        InformationSection(R.string.info_what_is_vaccine, R.string.info_what_is_vaccine_content),
        InformationSection(R.string.info_types_of_vaccines, R.string.info_types_of_vaccines_content),
        InformationSection(R.string.info_advantages, R.string.info_advantages_content),
        InformationSection(R.string.info_important_points, R.string.info_important_points_content),
        InformationSection(R.string.info_vaccine_failure, R.string.info_vaccine_failure_content),
        InformationSection(R.string.info_ppr_disease, R.string.info_ppr_disease_content),
        InformationSection(R.string.info_sheep_pox_disease, R.string.info_sheep_pox_disease_content),
        InformationSection(R.string.info_goat_pox_disease, R.string.info_goat_pox_disease_content),
        InformationSection(R.string.info_fmd_disease, R.string.info_fmd_disease_content),
        InformationSection(R.string.info_hs_disease, R.string.info_hs_disease_content),
        InformationSection(R.string.info_anthrax_disease, R.string.info_anthrax_disease_content),
        InformationSection(R.string.info_enterotoxemia_disease, R.string.info_enterotoxemia_disease_content),
        InformationSection(R.string.info_when_not_to_vaccinate, R.string.info_when_not_to_vaccinate_content)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.general_information)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sections) { section ->
                ExpandableInformationCard(section)
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.info_source_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.info_source_credit),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ExpandableInformationCard(section: InformationSection) {
    var isExpanded by remember { mutableStateOf(false) }
    val contentText = stringResource(section.contentResId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(section.titleResId),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // ALIGNMENT FIX: Use FormattedContent instead of a single Text block.
            // This splits the content by newlines and renders each line with
            // proper visual styling — headers bold, list items indented, etc.
            if (isExpanded) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                FormattedContent(
                    text = contentText,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

/**
 * ALIGNMENT FIX: Renders multi-line content strings with proper visual hierarchy.
 *
 * Rules applied to each line:
 *  • Blank line          → small spacer
 *  • ALL CAPS line       → section header (bold, primary color, larger)
 *  • Starts with number  → numbered point (semi-bold)
 *  • Starts with ✓ ❌ -  → indented list item
 *  • Anything else       → normal body text
 */
@Composable
fun FormattedContent(text: String, modifier: Modifier = Modifier) {
    // Pre-processing to handle the user's request for automatic line breaks
    // and better alignment across languages.
    val processedText = text
        // 1. Break after full stop, question mark, or exclamation followed by space (Sentence splitting)
        .replace(Regex("([.!?])\\s+"), "$1\n")
        // 2. Break before list markers (✓, ❌, ✗, -, ⚠️) if they are preceded by text
        .replace(Regex("(?<=\\S)\\s*([✓❌✗\\-⚠️])"), "\n$1")
        // 3. Break before numbered points (e.g., " 2.", " ೨.") if they are preceded by text
        .replace(Regex("(?<=\\S)\\s*([0-9\u0CE6-\u0CEF]+\\.)"), "\n$1")

    val lines = processedText.split("\n")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        lines.forEach { raw ->
            val line = raw.trim()
            
            // Empty line → breathing space
            if (line.isEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                return@forEach
            }

            // Header detection:
            // - ALL CAPS English (at least 1 Latin letter)
            // - Or ends with a colon or dash (common in both English and Kannada headers)
            val isHeader = (line.length > 3 && line == line.uppercase() && line.any { it in 'A'..'Z' }) ||
                    line.endsWith(":") ||
                    line.endsWith("：") ||
                    line.endsWith("-")

            // Numbered points: Latin or Kannada digits, followed by . or )
            val isNumbered = line.matches(Regex("^[0-9\u0CE6-\u0CEF]+[\\.)].*"))

            // List items
            val isListItem = line.startsWith("✓") || line.startsWith("❌") || line.startsWith("✗") || 
                             line.startsWith("-") || line.startsWith("⚠️")

            when {
                isHeader -> {
                    Text(
                        text = line,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }

                isNumbered -> {
                    Text(
                        text = line,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 21.sp,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }

                isListItem -> {
                    Text(
                        text = line,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(start = if (line.startsWith("-")) 16.dp else 4.dp)
                    )
                }

                // Indented sub-detail lines (e.g. lines starting with spaces in strings.xml)
                raw.startsWith("  ") || raw.startsWith("\t") -> {
                    Text(
                        text = line,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                // Regular body text
                else -> {
                    Text(
                        text = line,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}