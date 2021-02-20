package quova

@Suppress("BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER")
inline fun <reified T1, reified T2, reified R> Any.asIntersection(
    type: TypeWrapper2<T1, T2>, unit1: Unit = Unit
): R? where R : T1, R : T2 {
    return if (this is T1 && this is T2) this as R else null
}

@Suppress("BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER")
inline fun <reified T1, reified T2, reified T3, reified R> Any.asIntersection(
    type: TypeWrapper3<T1, T2, T3>, unit1: Unit = Unit, unit2: Unit = Unit
): R? where R : T1, R : T2, R : T3 {
    return if(this is T1 && this is T2 && this is T3) this as R else null
}