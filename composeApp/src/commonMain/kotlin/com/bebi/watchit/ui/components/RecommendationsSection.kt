package com.bebi.watchit.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.bebi.watchit.data.remote.model.TmdbMediaItem
import com.bebi.watchit.ui.util.formatWithOneDecimal
import com.mohamedrejeb.calf.ui.progress.AdaptiveCircularProgressIndicator
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.loading_recommendation
import moviesseriesshare.composeapp.generated.resources.loading_recommendation_issue
import org.jetbrains.compose.resources.stringResource
import placeholder

@Composable
fun RecommendationsSection(
    recommendationsMedia: List<TmdbMediaItem>,
    isLoadingRecommendationsMedia: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoadingRecommendationsMedia) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdaptiveCircularProgressIndicator()
                Text(
                    text = stringResource(Res.string.loading_recommendation),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    } else if (recommendationsMedia.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.loading_recommendation_issue),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = recommendationsMedia,
                key = { it.id }
            ) { recommendation ->
                RecommendationItem(recommendation = recommendation)
            }
        }
    }
}

@Composable
private fun RecommendationItem(
    recommendation: TmdbMediaItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagen del poster
        if (recommendation.posterPath != null) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w185${recommendation.posterPath}",
                contentDescription = recommendation.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(60.dp)
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ),
            )
        } else {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(90.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = placeholder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = recommendation.title ?: recommendation.name ?: "",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                StarRating(
                    rating = recommendation.voteAverage?.toFloat() ?: 0f,
                    maxRating = 1
                )

                Text(
                    text = (recommendation.voteAverage?.toString() ?: "0.0"),
                    style = MaterialTheme.typography.bodyMedium
                )

                if (recommendation.releaseDate != null) {
                    Text(
                        text = " • ${extractYearFromDate(recommendation.releaseDate)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (recommendation.overview != null) {
                Text(
                    text = recommendation.overview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Extrae solo el año de una fecha con formato YYYY-MM-DD
 */
private fun extractYearFromDate(dateString: String): String {
    return if (dateString.contains("-") && dateString.length >= 4) {
        dateString.split("-")[0]
    } else {
        dateString
    }
}