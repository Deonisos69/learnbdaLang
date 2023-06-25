@file:Suppress("LanguageDetectionInspection")

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import java.lang.Exception

sealed class Value() {
    data class Int(val value: kotlin.Int) : Value()
    data class Bool(val value: Boolean) : Value()
    data class Text(val value: String) : Value()
    data class Closure(val env: Env, val parameter: String, val body: Expression) : Value()
}

typealias Env = PersistentMap<String, Value>
val emptyEnv: Env = persistentMapOf()

fun closureEvaluate(prog: Prog): Value {
    return Evaluator(prog.defs).evaluate(emptyEnv, prog.expression)
}

class Evaluator(defs: List<Def>) {
    // Weiteres Environment über dem normalen Env
    // Wird verwendet um BuiltIn Funktionen hinzuzufügen und Defs im Env zu speichern.
    var topLevel: PersistentMap<String, Value>

    init {
        val topLevelMut = mutableMapOf<String, Value>()
        builtIns.forEach { builtIn ->
            // Für jede BuiltInFunction: Sobald die Arität größer als 2 ist werden Lambda Funktionen
            // um die BuiltIn Funktion geschachtelt. -> Currying um Funktionen mit mehreren Parametern
            // zu lösen. Wenn Arität kleiner als 2 ist, wird die BuiltInFunction an sich zurückgegeben
            val value = (2..builtIn.arity)
                .map { "param$it" }
                    // Fürs Erste nur Integer als Typ möglich
                .fold<String, Expression>(Expression.BuiltInFunction(builtIn.name)) { acc, param ->
                    Expression.Lambda(param, Type.Int, acc)
                }
            // Füge die einzelnen BuiltInFunctions mit ihren Lambda Schachtelungen in eine Closure
            // und dann in ein eigenes Environment hinzu. Noch gibt es keine Möglichkeit diese Closure auszurechnen
            topLevelMut[builtIn.name] = Value.Closure(emptyEnv, "param1", value)
        }
        topLevel = topLevelMut.toPersistentMap()
        defs.forEach {
            topLevel = topLevel.put(it.name, evaluate(emptyEnv, it.expression))
        }
    }

    fun evaluate(env: Env, expression: Expression): Value {
        return when (expression) {
            is Expression.App -> {
                when (val closure = evaluate(env, expression.func)) {
                    is Value.Closure -> {
                        val arg = evaluate(env, expression.arg)
                        val newEnv = closure.env.put(closure.parameter, arg)
                        evaluate(newEnv, closure.body)
                    }
                    is Value.Int, is Value.Bool, is Value.Text -> throw Exception("$closure is not a function")
                }
            }
            is Expression.Lambda -> {
                Value.Closure(env, expression.parameter, expression.body)
            }
            is Expression.Literal -> {
                return when (val primitive = expression.value) {
                    is Primitive.Bool -> Value.Bool(primitive.value)
                    is Primitive.Text -> Value.Text(primitive.value)
                    is Primitive.Int -> Value.Int(primitive.value)
                }
            }
            is Expression.Variable -> {
                env[expression.name]
                    ?: topLevel[expression.name]
                    ?: throw Exception("$expression is unbound")
            }
            is Expression.Binary -> {
                val left = evaluate(env, expression.left)
                val right = evaluate(env, expression.right)
                when (expression.operator) {
                    Operator.Add -> evaluateBinary<Value.Int>(left, right){ l, r -> Value.Int(l.value + r.value)}
                    Operator.Sub -> evaluateBinary<Value.Int>(left, right){ l, r -> Value.Int(l.value - r.value)}
                    Operator.Mul -> evaluateBinary<Value.Int>(left, right){ l, r -> Value.Int(l.value * r.value)}
                    Operator.Eq -> evaluateBinary<Value.Int>(left, right){ l, r -> Value.Bool(l.value == r.value)}
                    Operator.Or -> evaluateBinary<Value.Bool>(left, right){ l, r -> Value.Bool(l.value || r.value)}
                    Operator.And -> evaluateBinary<Value.Bool>(left, right){ l, r -> Value.Bool(l.value && r.value)}
                    Operator.Concat -> evaluateBinary<Value.Text>(left, right){ l, r -> Value.Text(l.value + r.value)}
                }
            }
            is Expression.If -> {
                if (evaluate(env, expression.condition) == Value.Bool(true))  {
                    evaluate(env, expression.thenBranch)
                }
                else if (evaluate(env, expression.condition)  == Value.Bool(false)) {
                    evaluate(env, expression.thenBranch)
                } else {
                    throw Exception("${expression.condition} has to be a Boolean")
                }

            }

            is Expression.Let -> {
                val bound = evaluate(env, expression.bound)
                evaluate(env.put(expression.name, bound), expression.body)
            }

            is Expression.BuiltInFunction ->
                return when(expression.name) {
                    "int_to_string" -> {
                        val int = env["param1"]!! as? Value.Int ?: throw Error("Expected an Int")
                        Value.Text(int.value.toString())
                    }
                    else -> throw Error("Unknown function: ${expression.name}")
                }

        }
    }
}

inline fun <reified T> evaluateBinary(left: Value, right: Value, f: (T, T) -> Value): Value {
    val leftCasted = left as? T ?: throw Error("Expected a ${T::class.simpleName} but got $left")
    val rightCasted = right as? T ?: throw Error("Expected a ${T::class.simpleName} but got $right")
    return f(leftCasted, rightCasted)
}