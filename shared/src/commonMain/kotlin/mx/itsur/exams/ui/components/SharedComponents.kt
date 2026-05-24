package mx.itsur.exams.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.itsur.exams.domain.model.Resultado
import mx.itsur.exams.ui.theme.*
import mx.itsur.exams.util.format1f
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

@Composable
fun ITSURHeader(
    title: String,
    subtitle: String = "",
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(ITSURVerdeOscuro, ITSURVerde)
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
            Row(content = actions)
        }
    }
}

@Composable
fun CalificacionBadge(calificacion: Float, modifier: Modifier = Modifier) {
    val color = when {
        calificacion >= 9f -> Color(0xFF2E7D32)
        calificacion >= 7f -> ITSURVerde
        calificacion >= 6f -> ITSURDorado
        else -> ITSURError
    }
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = calificacion.format1f(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun ExamenCard(
    titulo: String,
    descripcion: String,
    numPreguntas: Int,
    duracionMinutos: Int,
    activo: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ITSURTexto
                    )
                    if (descripcion.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ITSURGrisMedio,
                            maxLines = 2
                        )
                    }
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ITSURError)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipInfo(label = "$numPreguntas preguntas")
                ChipInfo(label = "$duracionMinutos min")
                if (!activo) ChipInfo(label = "Inactivo", color = ITSURError.copy(alpha = 0.15f), textColor = ITSURError)
            }
        }
    }
}

@Composable
fun ChipInfo(
    label: String,
    color: Color = ITSURVerdeClaro,
    textColor: Color = ITSURVerdeOscuro
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = textColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ResultadoCard(resultado: Resultado, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalificacionBadge(calificacion = resultado.calificacion)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                val titulo = resultado.examenTitulo.ifEmpty { resultado.alumnoNombre }
                Text(text = titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                val sub = resultado.alumnoNombre.ifEmpty { resultado.numeroControl }
                if (sub.isNotEmpty()) {
                    Text(text = sub, style = MaterialTheme.typography.bodyMedium, color = ITSURGrisMedio)
                }
            }
            val mins = resultado.tiempoUsadoSegundos / 60
            val secs = resultado.tiempoUsadoSegundos % 60
            Text(
                text = "${mins}m ${secs}s",
                style = MaterialTheme.typography.labelSmall,
                color = ITSURGrisMedio
            )
        }
    }
}

@Composable
fun QRResultado(resultado: Resultado, modifier: Modifier = Modifier) {
    val qrData = "ITSUR|alumno:${resultado.alumnoId}|examen:${resultado.examenId}|cal:${resultado.calificacion.format1f()}"
    val qrPainter = rememberQrCodePainter(qrData)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.Image(
            painter = qrPainter,
            contentDescription = "QR Resultado",
            modifier = Modifier.size(180.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Calificación: ${resultado.calificacion.format1f()}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ITSURVerdeOscuro
        )
        Text(
            text = "Escanea para verificar",
            style = MaterialTheme.typography.bodyMedium,
            color = ITSURGrisMedio
        )
    }
}

@Composable
fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = ITSURVerde)
            Spacer(Modifier.height(12.dp))
            Text("Cargando...", color = ITSURGrisMedio)
        }
    }
}

@Composable
fun EmptyState(mensaje: String, icon: ImageVector = Icons.Default.List) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = ITSURGrisMedio, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(12.dp))
            Text(mensaje, style = MaterialTheme.typography.bodyLarge, color = ITSURGrisMedio, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ITSURButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    secondary: Boolean = false,
    icon: ImageVector? = null
) {
    if (secondary) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ITSURVerde),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ITSURVerde)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ITSURVerde,
                contentColor = Color.White,
                disabledContainerColor = ITSURGrisClaro
            )
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ITSURTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    isPassword: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    onKeyEnter: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth().then(
            if (onKeyEnter != null) Modifier.onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp && event.key == Key.Enter) {
                    onKeyEnter()
                    true
                } else false
            } else Modifier
        ),
        singleLine = singleLine,
        maxLines = maxLines,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ITSURVerde,
            focusedLabelColor = ITSURVerde,
            cursorColor = ITSURVerde
        ),
        visualTransformation = if (isPassword)
            PasswordVisualTransformation()
        else
            VisualTransformation.None,
        leadingIcon = leadingIcon
    )
}
