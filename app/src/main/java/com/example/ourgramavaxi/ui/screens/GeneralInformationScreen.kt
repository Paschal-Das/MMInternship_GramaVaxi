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
    val lines = text.split("\n")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        lines.forEach { raw ->
            val line = raw.trim()
            when {
                // Empty line → breathing space
                line.isEmpty() -> {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // ALL CAPS line = section header (e.g. "VACCINE DETAILS", "SPECIAL NOTES")
                line.length > 3
                        && line == line.uppercase()
                        && line.first().isLetter() -> {
                    Text(
                        text = line,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }

                // Numbered points: "1. Something", "2. Something"
                line.matches(Regex("^\\d+\\..*")) -> {
                    Text(
                        text = line,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Check / cross / dash list items — indent them
                line.startsWith("✓") || line.startsWith("❌") || line.startsWith("✗") -> {
                    Text(
                        text = line,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                line.startsWith("-") -> {
                    Text(
                        text = line,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                // Indented sub-detail lines (e.g. lines starting with spaces)
                raw.startsWith("  ") || raw.startsWith("\t") -> {
                    Text(
                        text = line,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                // Regular body text
                else -> {
                    Text(
                        text = line,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}