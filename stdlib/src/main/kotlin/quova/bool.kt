package quova

fun Any?.bool() = this?.let { true } ?: false