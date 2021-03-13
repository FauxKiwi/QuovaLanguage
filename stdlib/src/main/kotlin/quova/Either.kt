package quova

sealed class Either<out A, out B> {
    class A<out Self, out Other>(val value: Self) : Either<Self, Other>() {
        operator fun component1() = value
        override fun toString(): String = value.toString()
    }
    class B<out Self, out Other>(val value: Self) : Either<Other, Self>() {
        operator fun component1() = value
        override fun toString(): String = value.toString()
    }

    inline fun <AR, BR, R> either(ifA: (A) -> AR, ifB: (B) -> BR): R where AR : R, BR : R =
        if (this is Either.A)
            ifA(value)
        else
            ifB((this as Either.B).value)
}

fun <A, B> Either(a: A?, b: B?) = a?.let { Either.A<A, B>(it) } ?: Either.B(b!!)

inline fun <A, B> eitherCatchingA(a: A?, b: B?, catch: () -> A) = a?.let { Either.A<A, B>(it) } ?: b?.let { Either.B(it) } ?: Either.A(catch())