package quova

@Suppress("nothing_to_inline")
inline fun <T> List(vararg elements: T) = listOf(*elements)

@Suppress("nothing_to_inline")
inline fun <A, B> HashMap(vararg elements: Pair<A, B>) = hashMapOf(*elements)