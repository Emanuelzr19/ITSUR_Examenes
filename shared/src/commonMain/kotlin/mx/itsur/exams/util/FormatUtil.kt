package mx.itsur.exams.util

fun Float.format1f(): String {
    val rounded = kotlin.math.round(this * 10) / 10.0
    val intPart = rounded.toInt()
    val decPart = kotlin.math.round((rounded - intPart) * 10).toInt()
    return "${intPart}.${decPart}"
}

fun Double.format1f(): String = this.toFloat().format1f()
