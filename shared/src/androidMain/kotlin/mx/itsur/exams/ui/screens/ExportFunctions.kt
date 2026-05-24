package mx.itsur.exams.ui.screens

import mx.itsur.exams.domain.model.Resultado

actual fun exportarPDF(examenNombre: String, resultados: List<Resultado>): String {
    // Android: Full implementation would use iText/PDFBox or share as HTML
    // For now return informative message — full impl requires Android Context
    return "PDF: función disponible en versión desktop"
}

actual fun exportarExcel(examenNombre: String, resultados: List<Resultado>): String {
    return "Excel: función disponible en versión desktop"
}
