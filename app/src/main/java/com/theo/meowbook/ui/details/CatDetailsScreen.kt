package com.theo.meowbook.ui.details

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.SubcomposeAsyncImage
import com.theo.meowbook.R
import com.theo.meowbook.model.CatDetails
import com.theo.meowbook.ui.components.ErrorAndEmptyView
import com.theo.meowbook.ui.components.ShimmerBox
import com.theo.meowbook.utils.select


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatDetailsScreen(
    viewModel: CatDetailsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    id: String
) {
    val catDetailsState by viewModel.catDetailsFLow.collectAsState()

    LaunchedEffect(id) {
        viewModel.loadCatDetails(id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cat Details") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            catDetailsState.select(
                onLoading = { CircularProgressIndicator() },
                onFailed = { throwable ->
                    ErrorAndEmptyView(
                        message = stringResource(R.string.cat_details_error_message),
                        onRetry = { viewModel.loadCatDetails(id) }
                    )
                },
                onSuccess = { catDetails ->
                    CatDetailsContent(catDetails = catDetails)
                }
            )
        }
    }
}


@Composable
private fun CatDetailsContent(
    catDetails: CatDetails,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SubcomposeAsyncImage(
            model = catDetails.url,
            contentDescription = catDetails.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp)),
            loading = { ShimmerBox(modifier = Modifier.fillMaxSize()) }
        )

        Text(text = catDetails.name, style = MaterialTheme.typography.headlineSmall)

        InfoItem(
            label = stringResource(R.string.cat_details_origin_label),
            value = catDetails.origin
        )
        InfoItem(
            label = stringResource(R.string.cat_temperament_label),
            value = catDetails.temperament
        )
        InfoItem(
            label = stringResource(R.string.cat_details_life_span_label), value = stringResource(
                R.string.cat_details_life_years, catDetails.lifeSpan
            )
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        Text(text = "Characteristics", style = MaterialTheme.typography.headlineSmall)
        StarRatingItem(
            label = stringResource(R.string.cat_details_affection_label),
            rating = catDetails.affectionLevel
        )
        StarRatingItem(
            label = stringResource(R.string.cat_details_intelligence_label),
            rating = catDetails.intelligence
        )
        StarRatingItem(
            label = stringResource(R.string.cat_details_child_friendly_label),
            rating = catDetails.childFriendly
        )
        StarRatingItem(
            label = stringResource(R.string.cat_details_social_needs_label),
            rating = catDetails.socialNeeds
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        Text(
            text = catDetails.description,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            catDetails.wikipediaUrl?.let { url ->
                ExternalLinkChip(text = stringResource(R.string.generic_wikipedia), url = url)
            }
            catDetails.vetStreetUrl?.let { url ->
                ExternalLinkChip(text = stringResource(R.string.generic_vetstreet), url = url)
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            modifier = Modifier.fillMaxSize(0.5f),
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun StarRatingItem(
    label: String,
    rating: Int,
    maxRating: Int = 5
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.titleMedium)

        Row {
            for (i in 1..maxRating) {
                val icon = if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ExternalLinkChip(text: String, url: String) {
    val context = LocalContext.current
    SuggestionChip(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        },
        label = { Text(text) }
    )
}