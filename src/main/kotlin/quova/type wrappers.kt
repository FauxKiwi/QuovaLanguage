package quova

// Inspired by Arrow's Union classes definition
sealed class TypeWrapper22<out T1, out T2, out T3, out T4, out T5, out T6, out T7, out T8, out T9, out T10, out T11, out T12, out T13, out T14, out T15, out T16, out T17, out T18, out T19, out T20, out T21, out T22> {
    @PublishedApi internal object IMPL: TypeWrapper22<Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing>()
}
typealias TypeWrapper21<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21> = TypeWrapper22<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, Nothing>
typealias TypeWrapper20<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20> = TypeWrapper21<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, Nothing>
typealias TypeWrapper19<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> = TypeWrapper20<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, Nothing>
typealias TypeWrapper18<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> = TypeWrapper19<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, Nothing>
typealias TypeWrapper17<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> = TypeWrapper18<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, Nothing>
typealias TypeWrapper16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> = TypeWrapper17<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, Nothing>
typealias TypeWrapper15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> = TypeWrapper16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, Nothing>
typealias TypeWrapper14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> = TypeWrapper15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, Nothing>
typealias TypeWrapper13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> = TypeWrapper14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, Nothing>
typealias TypeWrapper12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> = TypeWrapper13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, Nothing>
typealias TypeWrapper11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> = TypeWrapper12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, Nothing>
typealias TypeWrapper10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> = TypeWrapper11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, Nothing>
typealias TypeWrapper9<T1, T2, T3, T4, T5, T6, T7, T8, T9> = TypeWrapper10<T1, T2, T3, T4, T5, T6, T7, T8, T9, Nothing>
typealias TypeWrapper8<T1, T2, T3, T4, T5, T6, T7, T8> = TypeWrapper9<T1, T2, T3, T4, T5, T6, T7, T8, Nothing>
typealias TypeWrapper7<T1, T2, T3, T4, T5, T6, T7> = TypeWrapper8<T1, T2, T3, T4, T5, T6, T7, Nothing>
typealias TypeWrapper6<T1, T2, T3, T4, T5, T6> = TypeWrapper7<T1, T2, T3, T4, T5, T6, Nothing>
typealias TypeWrapper5<T1, T2, T3, T4, T5> = TypeWrapper6<T1, T2, T3, T4, T5, Nothing>
typealias TypeWrapper4<T1, T2, T3, T4> = TypeWrapper5<T1, T2, T3, T4, Nothing>
typealias TypeWrapper3<T1, T2, T3> = TypeWrapper4<T1, T2, T3, Nothing>
typealias TypeWrapper2<T1, T2> = TypeWrapper3<T1, T2, Nothing>
typealias TypeWrapper1<T1> = TypeWrapper2<T1, Nothing>

// Type "constructors". The unitx parameters are there to avoid `Overload Resolution Ambiguity` errors
// due to the fact that all of the functions are named "type"
inline fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit, unit4: Unit = Unit, unit5: Unit = Unit, unit6: Unit = Unit, unit7: Unit = Unit, unit8: Unit = Unit, unit9: Unit = Unit, unit10: Unit = Unit, unit11: Unit = Unit, unit12: Unit = Unit, unit13: Unit = Unit, unit14: Unit = Unit, unit15: Unit = Unit, unit16: Unit = Unit, unit17: Unit = Unit, unit18: Unit = Unit, unit19: Unit = Unit, unit20: Unit = Unit, unit21: Unit = Unit): TypeWrapper22<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit, unit4: Unit = Unit, unit5: Unit = Unit, unit6: Unit = Unit, unit7: Unit = Unit, unit8: Unit = Unit, unit9: Unit = Unit, unit10: Unit = Unit, unit11: Unit = Unit, unit12: Unit = Unit, unit13: Unit = Unit, unit14: Unit = Unit, unit15: Unit = Unit, unit16: Unit = Unit, unit17: Unit = Unit, unit18: Unit = Unit, unit19: Unit = Unit, unit20: Unit = Unit): TypeWrapper21<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit, unit4: Unit = Unit, unit5: Unit = Unit, unit6: Unit = Unit, unit7: Unit = Unit, unit8: Unit = Unit, unit9: Unit = Unit, unit10: Unit = Unit, unit11: Unit = Unit, unit12: Unit = Unit, unit13: Unit = Unit, unit14: Unit = Unit, unit15: Unit = Unit, unit16: Unit = Unit, unit17: Unit = Unit, unit18: Unit = Unit, unit19: Unit = Unit): TypeWrapper20<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit, unit4: Unit = Unit, unit5: Unit = Unit, unit6: Unit = Unit, unit7: Unit = Unit, unit8: Unit = Unit, unit9: Unit = Unit, unit10: Unit = Unit, unit11: Unit = Unit, unit12: Unit = Unit, unit13: Unit = Unit, unit14: Unit = Unit, unit15: Unit = Unit, unit16: Unit = Unit, unit17: Unit = Unit, unit18: Unit = Unit): TypeWrapper19<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit, unit4: Unit = Unit, unit5: Unit = Unit, unit6: Unit = Unit, unit7: Unit = Unit, unit8: Unit = Unit, unit9: Unit = Unit, unit10: Unit = Unit, unit11: Unit = Unit, unit12: Unit = Unit, unit13: Unit = Unit, unit14: Unit = Unit, unit15: Unit = Unit, unit16: Unit = Unit, unit17: Unit = Unit): TypeWrapper18<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit, unit4: Unit = Unit, unit5: Unit = Unit, unit6: Unit = Unit, unit7: Unit = Unit, unit8: Unit = Unit, unit9: Unit = Unit, unit10: Unit = Unit, unit11: Unit = Unit, unit12: Unit = Unit, unit13: Unit = Unit, unit14: Unit = Unit, unit15: Unit = Unit, unit16: Unit = Unit): TypeWrapper17<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit, unit4: Unit = Unit, unit5: Unit = Unit, unit6: Unit = Unit, unit7: Unit = Unit, unit8: Unit = Unit, unit9: Unit = Unit, unit10: Unit = Unit, unit11: Unit = Unit, unit12: Unit = Unit, unit13: Unit = Unit, unit14: Unit = Unit, unit15: Unit = Unit): TypeWrapper16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit, unit4: Unit = Unit, unit5: Unit = Unit, unit6: Unit = Unit, unit7: Unit = Unit, unit8: Unit = Unit, unit9: Unit = Unit, unit10: Unit = Unit, unit11: Unit = Unit, unit12: Unit = Unit, unit13: Unit = Unit, unit14: Unit = Unit): TypeWrapper15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit, unit4: Unit = Unit, unit5: Unit = Unit, unit6: Unit = Unit, unit7: Unit = Unit, unit8: Unit = Unit, unit9: Unit = Unit, unit10: Unit = Unit, unit11: Unit = Unit, unit12: Unit = Unit, unit13: Unit = Unit): TypeWrapper14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit, unit4: Unit = Unit, unit5: Unit = Unit, unit6: Unit = Unit, unit7: Unit = Unit, unit8: Unit = Unit, unit9: Unit = Unit, unit10: Unit = Unit, unit11: Unit = Unit, unit12: Unit = Unit): TypeWrapper13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit, unit4: Unit = Unit, unit5: Unit = Unit, unit6: Unit = Unit, unit7: Unit = Unit, unit8: Unit = Unit, unit9: Unit = Unit, unit10: Unit = Unit, unit11: Unit = Unit): TypeWrapper12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit, unit4: Unit = Unit, unit5: Unit = Unit, unit6: Unit = Unit, unit7: Unit = Unit, unit8: Unit = Unit, unit9: Unit = Unit, unit10: Unit = Unit): TypeWrapper11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit, unit4: Unit = Unit, unit5: Unit = Unit, unit6: Unit = Unit, unit7: Unit = Unit, unit8: Unit = Unit, unit9: Unit = Unit): TypeWrapper10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3, T4, T5, T6, T7, T8, T9> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit, unit4: Unit = Unit, unit5: Unit = Unit, unit6: Unit = Unit, unit7: Unit = Unit, unit8: Unit = Unit): TypeWrapper9<T1, T2, T3, T4, T5, T6, T7, T8, T9>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3, T4, T5, T6, T7, T8> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit, unit4: Unit = Unit, unit5: Unit = Unit, unit6: Unit = Unit, unit7: Unit = Unit): TypeWrapper8<T1, T2, T3, T4, T5, T6, T7, T8>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3, T4, T5, T6, T7> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit, unit4: Unit = Unit, unit5: Unit = Unit, unit6: Unit = Unit): TypeWrapper7<T1, T2, T3, T4, T5, T6, T7>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3, T4, T5, T6> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit, unit4: Unit = Unit, unit5: Unit = Unit): TypeWrapper6<T1, T2, T3, T4, T5, T6>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3, T4, T5> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit, unit4: Unit = Unit): TypeWrapper5<T1, T2, T3, T4, T5>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3, T4> type(unit1: Unit = Unit, unit2: Unit = Unit, unit3: Unit = Unit): TypeWrapper4<T1, T2, T3, T4>
        = TypeWrapper22.IMPL
inline fun <T1, T2, T3> type(unit1: Unit = Unit, unit2: Unit = Unit): TypeWrapper3<T1, T2, T3>
        = TypeWrapper22.IMPL
inline fun <T1, T2> type(unit1: Unit = Unit): TypeWrapper2<T1, T2>
        = TypeWrapper22.IMPL
inline fun <T1> type(): TypeWrapper1<T1>
        = TypeWrapper22.IMPL