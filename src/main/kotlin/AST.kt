import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

typealias Env = PersistentMap<String, Value>
val emptyEnv: Env = persistentMapOf()

sealed class Expression() {
    data class App(val func: Expression, val arg: Expression): Expression()
    data class Lambda(val parameter: String, val body: Expression): Expression()
    data class Literal(val value: Value): Expression()
    data class Variable(val name: String): Expression()
}

sealed class Value() {
    data class Int(val value: kotlin.Int) : Value()
    data class Bool(val value: Boolean) : Value()
    data class Closure(val env: Env, val parameter: String, val body: Expression) : Value()
}