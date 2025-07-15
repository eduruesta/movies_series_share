package com.bebi.watchit.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bebi.watchit.model.MediaOpinion
import com.bebi.watchit.ui.util.formatWithOneDecimal
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.delete_from_recommendations
import moviesseriesshare.composeapp.generated.resources.opinion_count
import moviesseriesshare.composeapp.generated.resources.save_to_recommendations
import org.jetbrains.compose.resources.stringResource

@Composable
fun MediaInfoSection(
    opinion: MediaOpinion,
    isSaved: Boolean,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = opinion.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            StarRating(
                rating = opinion.averageRating,
                maxRating = 1,
            )
            val ratingText = if (opinion.ratingCount > 0) {
                val formattedRating = opinion.averageRating.formatWithOneDecimal()
                stringResource(
                    Res.string.opinion_count,
                    formattedRating,
                    opinion.ratingCount.toString()
                )
            } else {
                "${opinion.rating}"
            }

            Text(
                text = ratingText,
                style = MaterialTheme.typography.bodyMedium
            )

            if (opinion.year.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { },
                        modifier = Modifier.height(32.dp).padding(start = 8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            extractYearFromDate(opinion.year),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    if (!opinion.username.isNullOrEmpty()) {
                        FilledTonalButton(
                            onClick = { },
                            modifier = Modifier.height(32.dp).padding(start = 8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = opinion.username,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Icono de bookmark
                    Icon(
                        imageVector = if (isSaved) bookmarkCheck else bookmark,
                        contentDescription = if (isSaved)
                            stringResource(Res.string.delete_from_recommendations) else
                            stringResource(Res.string.save_to_recommendations),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(start = 8.dp)
                            .clickable { onBookmarkClick() }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (opinion.genre.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 32.dp)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = opinion.genre,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                if (opinion.platform.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = { },
                        modifier = Modifier.defaultMinSize(minHeight = 32.dp)
                    ) {
                        Text(
                            text = opinion.platform,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
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