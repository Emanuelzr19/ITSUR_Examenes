package mx.itsur.exams.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import mx.itsur.exams.domain.model.Resultado
import mx.itsur.exams.util.AndroidContextProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

// ─────────────────────────────────────────────────────────────────────────────
// PDF — usa android.graphics.pdf.PdfDocument (API nativa, sin librerías)
// ─────────────────────────────────────────────────────────────────────────────

actual fun exportarPDF(examenNombre: String, resultados: List<Resultado>): String {
    return try {
        val context = AndroidContextProvider.get()
        val doc = PdfDocument()

        val paintHeaderBg  = Paint().apply { color = Color.parseColor("#2E7D32") }
        val paintSubtitulo = Paint().apply { textSize = 13f; color = Color.WHITE; isFakeBoldText = true }
        val paintSubtext   = Paint().apply { textSize = 10f; color = Color.parseColor("#A5D6A7") }
        val paintTitulo    = Paint().apply { textSize = 18f; isFakeBoldText = true; color = Color.parseColor("#2E7D32") }
        val paintStatBg    = Paint().apply { color = Color.parseColor("#F1F8E9") }
        val paintStatText  = Paint().apply { textSize = 10f; isFakeBoldText = true; color = Color.BLACK }
        val paintThBg      = Paint().apply { color = Color.parseColor("#388E3C") }
        val paintThText    = Paint().apply { textSize = 10f; color = Color.WHITE; isFakeBoldText = true }
        val paintTdTexto   = Paint().apply { textSize = 10f; color = Color.DKGRAY }
        val paintAprobado  = Paint().apply { textSize = 10f; color = Color.parseColor("#2E7D32"); isFakeBoldText = true }
        val paintReprobado = Paint().apply { textSize = 10f; color = Color.parseColor("#C62828"); isFakeBoldText = true }
        val paintLinea     = Paint().apply { color = Color.parseColor("#E0E0E0"); strokeWidth = 0.8f }
        val paintPageNum   = Paint().apply { textSize = 9f; color = Color.GRAY }
        val paintAltRow    = Paint().apply { color = Color.parseColor("#F9FBE7") }

        val W = 595; val H = 842
        val mg = 40f; val rowH = 26f
        val itemsPerPage = 22

        val promedio = if (resultados.isNotEmpty())
            resultados.sumOf { it.calificacion.toDouble() } / resultados.size else 0.0
        val aprobados  = resultados.count { it.calificacion >= 6f }
        val reprobados = resultados.size - aprobados
        val totalPages = maxOf(1, (resultados.size + itemsPerPage - 1) / itemsPerPage)

        for (p in 0 until totalPages) {
            val page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, p + 1).create())
            val cv: Canvas = page.canvas

            // Header verde
            cv.drawRect(0f, 0f, W.toFloat(), 52f, paintHeaderBg)
            cv.drawText("ITSUR — Sistema de Exámenes", mg, 22f, paintSubtitulo)
            cv.drawText("Reporte de Calificaciones", mg, 42f, paintSubtext)

            var y = 64f
            cv.drawText(examenNombre.take(60), mg, y + 14f, paintTitulo)
            y += 30f

            // Barra de estadísticas
            cv.drawRoundRect(RectF(mg, y, W - mg, y + 32f), 6f, 6f, paintStatBg)
            cv.drawText(
                "Total: ${resultados.size}   |   Promedio: ${"%.1f".format(promedio)}   |   Aprobados: $aprobados   |   Reprobados: $reprobados",
                mg + 10f, y + 21f, paintStatText
            )
            y += 44f

            // Cabecera de tabla
            cv.drawRect(mg, y, W - mg, y + rowH, paintThBg)
            cv.drawText("Alumno",       mg + 6f, y + 17f, paintThText)
            cv.drawText("No. Control",  252f,    y + 17f, paintThText)
            cv.drawText("Calif.",       386f,    y + 17f, paintThText)
            cv.drawText("Estado",       440f,    y + 17f, paintThText)
            cv.drawText("Tiempo",       510f,    y + 17f, paintThText)
            y += rowH

            // Filas de datos
            val ini = p * itemsPerPage
            val fin = minOf(ini + itemsPerPage, resultados.size)
            for (i in ini until fin) {
                val r = resultados[i]
                if (i % 2 == 0) cv.drawRect(mg, y, W - mg, y + rowH, paintAltRow)
                cv.drawText(r.alumnoNombre.ifEmpty { r.alumnoId }.take(26), mg + 6f, y + 17f, paintTdTexto)
                cv.drawText(r.numeroControl.take(14), 252f, y + 17f, paintTdTexto)
                val pCal = if (r.calificacion >= 6f) paintAprobado else paintReprobado
                cv.drawText("%.1f".format(r.calificacion), 390f, y + 17f, pCal)
                cv.drawText(if (r.calificacion >= 6f) "Aprobado" else "Reprobado", 440f, y + 17f, pCal)
                val m = r.tiempoUsadoSegundos / 60; val s = r.tiempoUsadoSegundos % 60
                cv.drawText("${m}m${s}s", 512f, y + 17f, paintTdTexto)
                cv.drawLine(mg, y + rowH, W - mg, y + rowH, paintLinea)
                y += rowH
            }

            cv.drawText("Página ${p + 1} de $totalPages", W - 95f, H - 18f, paintPageNum)
            doc.finishPage(page)
        }

        val safeName = examenNombre.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val fileName = "${safeName}_reporte.pdf"
        val uri = guardarArchivo(context, fileName, "application/pdf") { out -> doc.writeTo(out) }
        doc.close()
        if (uri != null) abrirArchivo(context, uri, "application/pdf")
        "PDF guardado en Descargas: $fileName"
    } catch (e: Exception) {
        "Error al generar PDF: ${e.message}"
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EXCEL — CSV con BOM UTF-8 (Excel lo abre correctamente con tildes y ñ)
// Sin ninguna librería externa, compatible con minSdk 24
// ─────────────────────────────────────────────────────────────────────────────

actual fun exportarExcel(examenNombre: String, resultados: List<Resultado>): String {
    return try {
        val context = AndroidContextProvider.get()

        val promedio = if (resultados.isNotEmpty())
            resultados.sumOf { it.calificacion.toDouble() } / resultados.size else 0.0
        val aprobados  = resultados.count { it.calificacion >= 6f }
        val reprobados = resultados.size - aprobados

        val sb = StringBuilder()
        // BOM UTF-8 para que Excel reconozca la codificación y muestre tildes correctamente
        sb.append('\uFEFF')
        sb.appendLine("Reporte: $examenNombre")
        sb.appendLine("Total: ${resultados.size},Promedio: ${"%.1f".format(promedio)},Aprobados: $aprobados,Reprobados: $reprobados")
        sb.appendLine()
        // Cabecera
        sb.appendLine("Alumno,No. Control,Calificación,Estado,Tiempo (s),Tiempo (min)")
        // Datos
        resultados.forEach { r ->
            val nombre     = "\"${r.alumnoNombre.ifEmpty { r.alumnoId }.replace("\"", "\"\"")}\""
            val control    = "\"${r.numeroControl.replace("\"", "\"\"")}\""
            val calificacion = "%.1f".format(r.calificacion)
            val estado     = if (r.calificacion >= 6f) "Aprobado" else "Reprobado"
            val tiempoSeg  = r.tiempoUsadoSegundos
            val tiempoMin  = "%.1f".format(r.tiempoUsadoSegundos / 60.0)
            sb.appendLine("$nombre,$control,$calificacion,$estado,$tiempoSeg,$tiempoMin")
        }

        val safeName = examenNombre.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        // .csv — Excel lo abre directo con doble clic
        val fileName = "${safeName}_reporte.csv"
        val uri = guardarArchivo(context, fileName, "text/csv") { out ->
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
        }
        if (uri != null) abrirArchivo(context, uri, "text/csv")
        "Excel (CSV) guardado en Descargas: $fileName"
    } catch (e: Exception) {
        "Error al generar Excel: ${e.message}"
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers compartidos
// ─────────────────────────────────────────────────────────────────────────────

private fun guardarArchivo(
    context: Context,
    fileName: String,
    mimeType: String,
    write: (OutputStream) -> Unit
): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Android 10+ — MediaStore, sin permisos de almacenamiento
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        uri?.let { context.contentResolver.openOutputStream(it)?.use { out -> write(out) } }
        uri
    } else {
        // Android 9 y menor — almacenamiento directo + FileProvider
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        dir.mkdirs()
        val file = File(dir, fileName)
        FileOutputStream(file).use { write(it) }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}

private fun abrirArchivo(context: Context, uri: Uri, mimeType: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try { context.startActivity(intent) } catch (_: Exception) { }
}
