package com.bebi.watchit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bebi.watchit.model.MediaOpinion
import com.mohamedrejeb.calf.ui.progress.AdaptiveCircularProgressIndicator
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.qualifying
import moviesseriesshare.composeapp.generated.resources.rate_action
import org.jetbrains.compose.resources.stringResource

/**
 * BottomSheet para calificar una película o serie
 * 
 * @param opinion La opinión a calificar
 * @param onDismiss Callback para cuando se cierra el BottomSheet
 * @param onRatingSubmit Callback para cuando se envía una calificación
 * @param isLoading Indica si se está procesando la calificación
 * @param sheetState Estado del BottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingBottomSheet(
    opinion: MediaOpinion,
    onDismiss: () -> Unit,
    onRatingSubmit: (MediaOpinion, Int) -> Unit,
    isLoading: Boolean = false,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    var selectedRating by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = { if (!isLoading) onDismiss() },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(Res.string.rate_action),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = opinion.title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StarRating(
                    rating = selectedRating.toFloat(),
                    maxRating = 10,
                    onRatingChanged = { if (!isLoading) selectedRating = it.toInt() },
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }

            Text(
                text = "$selectedRating/10",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onRatingSubmit(opinion, selectedRating) },
                modifier = Modifier.fillMaxWidth(0.7f),
                enabled = selectedRating > 0 && !isLoading
            ) {
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AdaptiveCircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(Res.string.qualifying))
                    }
                } else {
                    Text(stringResource(Res.string.rate_action))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
