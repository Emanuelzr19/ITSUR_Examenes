package mx.itsur.exams.ui.screens

import mx.itsur.exams.domain.model.Resultado
import java.awt.Desktop
import java.io.File
import java.io.FileWriter

actual fun exportarPDF(examenNombre: String, resultados: List<Resultado>): String {
    return try {
        val home = System.getProperty("user.home")
        val safeName = examenNombre.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val file = File("$home/Desktop/${safeName}_reporte.html")

        val promedio = if (resultados.isNotEmpty())
            resultados.sumOf { it.calificacion.toDouble() } / resultados.size else 0.0
        val aprobados = resultados.count { it.calificacion >= 6f }

        val rows = resultados.joinToString("") { r ->
            val color = if (r.calificacion >= 6f) "#E8F5E9" else "#FFEBEE"
            val estado = if (r.calificacion >= 6f) "Aprobado" else "Reprobado"
            val mins = r.tiempoUsadoSegundos / 60
            val secs = r.tiempoUsadoSegundos % 60
            "<tr style='background:$color'><td>${r.alumnoNombre.ifEmpty { r.alumnoId }}</td><td>${r.numeroControl}</td><td style='font-weight:bold'>${"%.1f".format(r.calificacion)}</td><td>$estado</td><td>${mins}m ${secs}s</td></tr>"
        }

        val html = """<!DOCTYPE html>
<html lang="es">
<head><meta charset="UTF-8"><title>Reporte — $examenNombre</title>
<style>
  body { font-family: Arial, sans-serif; margin: 40px; color: #333; }
  h1 { color: #2E7D32; } h2 { color: #388E3C; }
  .stats { display:flex; gap:24px; margin:16px 0; }
  .stat { background:#E8F5E9; padding:12px 20px; border-radius:8px; text-align:center; }
  .stat strong { font-size:24px; color:#2E7D32; display:block; }
  table { width:100%; border-collapse:collapse; margin-top:16px; }
  th { background:#2E7D32; color:white; padding:10px; text-align:left; }
  td { padding:8px 10px; border-bottom:1px solid #ddd; }
  @media print { button { display:none; } }
</style>
</head><body>
<h1>ITSUR — Sistema de Exámenes</h1>
<h2>Reporte: $examenNombre</h2>
<div class='stats'>
  <div class='stat'><strong>${resultados.size}</strong>Presentaron</div>
  <div class='stat'><strong>${"%.1f".format(promedio)}</strong>Promedio</div>
  <div class='stat'><strong>$aprobados</strong>Aprobados</div>
</div>
<table><tr><th>Alumno</th><th>No. Control</th><th>Calificación</th><th>Estado</th><th>Tiempo</th></tr>$rows</table>
<br><button onclick="window.print()">Imprimir / Guardar PDF</button>
</body></html>"""

        FileWriter(file).use { it.write(html) }

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(file.toURI())
        }
        "PDF generado en: ${file.name}"
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}

actual fun exportarExcel(examenNombre: String, resultados: List<Resultado>): String {
    return try {
        val home = System.getProperty("user.home")
        val safeName = examenNombre.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val file = File("$home/Desktop/${safeName}_reporte.csv")

        FileWriter(file, Charsets.UTF_8).use { writer ->
            writer.write("Examen: $examenNombre\n")
            writer.write("Alumno,No. Control,Calificacion,Estado,Tiempo (s)\n")
            resultados.forEach { r ->
                val estado = if (r.calificacion >= 6f) "Aprobado" else "Reprobado"
                writer.write("\"${r.alumnoNombre.ifEmpty { r.alumnoId }}\",\"${r.numeroControl}\",${
                    "%.1f".format(r.calificacion)},$estado,${r.tiempoUsadoSegundos}\n")
            }
        }

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file.parentFile)
        }
        "Excel (CSV) guardado en: ${file.name}"
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}
