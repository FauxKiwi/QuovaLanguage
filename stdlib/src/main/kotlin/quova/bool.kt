package quova

inline fun <reified N : Number> N.bool() = this != (0 as N)

fun Any?.bool() = this != null