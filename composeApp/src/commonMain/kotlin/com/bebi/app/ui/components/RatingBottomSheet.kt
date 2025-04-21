package com.bebi.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.bebi.app.model.MediaOpinion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingBottomSheet(
    opinion: MediaOpinion,
    onDismiss: () -> Unit,
    onRatingSubmit: (MediaOpinion, Int) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    // Iniciar sin calificación seleccionada (0)
    var selectedRating by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp) // Aumentar el espaciado entre elementos
        ) {
            // Título
            Text(
                text = "Calificar",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // Nombre de la película/serie
            Text(
                text = opinion.title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Estrellas para calificar - Usar un contenedor con padding para dar más espacio
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StarRating(
                    rating = selectedRating.toFloat(),
                    maxRating = 10,
                    onRatingChanged = { selectedRating = it.toInt() },
                    modifier = Modifier.fillMaxWidth(0.9f) // Limitar el ancho para mejor centrado
                )
            }

            // Valor numérico de la calificación
            Text(
                text = "$selectedRating/10",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón para enviar la calificación
            Button(
                onClick = {
                    onRatingSubmit(opinion, selectedRating)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(0.7f),
                // Deshabilitar el botón si no hay calificación seleccionada
                enabled = selectedRating > 0
            ) {
                Text("Calificar")
            }

            // Espacio al final para mejor apariencia
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
