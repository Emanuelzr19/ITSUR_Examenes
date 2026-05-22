package mx.itsur.exams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.itsur.exams.ui.theme.*

@Composable
fun ITSURLogo(size: Int = 80) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFFF0F8E8))
                )
            )
            .border(
                width = (size * 0.04f).dp,
                brush = Brush.sweepGradient(listOf(ITSURDorado, ITSURVerde, ITSURDorado)),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "ITSUR",
                fontSize = (size * 0.20f).sp,
                fontWeight = FontWeight.ExtraBold,
                color = ITSURNavy,
                letterSpacing = (size * 0.01f).sp
            )
            if (size >= 60) {
                Box(
                    modifier = Modifier
                        .width((size * 0.55f).dp)
                        .height((size * 0.04f).dp)
                        .background(ITSURDorado, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}
