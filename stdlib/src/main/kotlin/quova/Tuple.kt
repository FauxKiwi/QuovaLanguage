package quova

class Tuple<A : S, B : S, S> {
    private val a: A
    private val b: Tuple<B, out S, S>?

    constructor(vararg elements: S) {
        a = elements[0] as A
        b = if (elements.isNotEmpty()) Tuple(*elements[1..elements.size]) else null
    }

    operator fun get(i: Int): S = if (i == 0) a else b!![i - 1] as B

    operator fun component1() = a
    operator fun component2() = b!!.a
    operator fun component3() = b!!.b!!.a
    operator fun component4() = b!!.b!!.b!!.a
    operator fun component5() = b!!.b!!.b!!.b!!.a
    operator fun component6() = b!!.b!!.b!!.b!!.b!!.a
    operator fun component7() = b!!.b!!.b!!.b!!.b!!.b!!.a
    operator fun component8() = b!!.b!!.b!!.b!!.b!!.b!!.b!!.a
    operator fun component9() = b!!.b!!.b!!.b!!.b!!.b!!.b!!.b!!.a
    operator fun component10() = b!!.b!!.b!!.b!!.b!!.b!!.b!!.b!!.b!!.a

}

operator fun <T> Array<T>.get(slice: IntRange): Array<T> = sliceArray(slice)

fun test() {
    val (c12, c123, c13) = Tuple<C12, C123, I1>(C12(), C123(), C13())
}

interface I1 { fun i1() = 1 }
interface I2 { fun i2() = 2 }
interface I3 { fun i3() = 3 }
class C12 : I1, I2
class C123 : I1, I2, I3
class C13 : I1, I3