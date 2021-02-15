package quova

inline class decimal(val long: Long) {

}

@Suppress("nothing_to_inline")
inline fun Double.toDecimal(): decimal {
    val lsb = toInt()
    val msb = 0
    return decimal((lsb.toLong() shl 32) or msb.toLong())
}