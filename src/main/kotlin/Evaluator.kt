@file:Suppress("LanguageDetectionInspection")

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import java.lang.IllegalArgumentException

typealias Env = PersistentMap<LocallyNamelessVariable, Value>
val emptyEnv: Env = persistentMapOf()

typealias ExpressionEnv = PersistentMap<LocallyNamelessVariable, Expression>
val emptyExpressionEnv: Env = persistentMapOf()

typealias  namelessMap = PersistentMap<LocallyNamelessVariable.FreeVariable, LocallyNamelessVariable.BoundVariable>
val emptyNamelessMap : namelessMap = persistentMapOf()

sealed class Value() {
    // Eine Value ist was am Ende von der Berechnung herauskommt
    data class Int(val value: kotlin.Int) : Value()
    data class Bool(val value: Boolean) : Value()
    data class String(val value: kotlin.String) : Value()
    data class Closure(val environment: Env, val variable: Expression.Variable, val body: Expression) : Value()
//    data class FreeVariable(val locallyNamelessVariable: LocallyNamelessVariable): Value()
}

class Evaluator() {

    fun evaluate(environment: Env, expression: Expression, depth: Int) : Value {

        return when (val self = expression) {
            is Expression.App -> {
                val left = evaluate(environment ,self.func, depth)
                if (left is Value.Closure) {
                    val right = evaluate(environment, self.arg, depth)

                    val newEnvironment = shift(environment).put(LocallyNamelessVariable.BoundVariable(0), right)
                    evaluate(newEnvironment, left.body, depth + 1)
                } else {
                    throw IllegalArgumentException("$self is not a function. Can only apply functions")
                }
            }
            is Expression.Lambda -> Value.Closure(environment, Expression.Variable(LocallyNamelessVariable.FreeVariable("hey")), self.body)
            is Expression.Literal -> when (self.primitive) {
                is Primitive.Bool -> Value.Bool(self.primitive.value)
                is Primitive.Int -> Value.Int(self.primitive.value)
                is Primitive.String -> Value.String(self.primitive.value)
            }
            is Expression.Variable -> (
                when (self.variable) {
                    is LocallyNamelessVariable.BoundVariable -> {
                        environment[LocallyNamelessVariable.BoundVariable(depth)]
                            ?: throw Exception("Variable not found")
                    }
                    is LocallyNamelessVariable.FreeVariable -> throw Exception("Can't evaluate a free variable!")
                }
            )

            is Expression.Binary -> {
                val left = evaluate(environment, self.left, depth)
                val right = evaluate(environment, self.right, depth)
                when (self.operator) {
                    Operator.PLUS -> evaluateBinary<Value.Int>(left, right) { l, r -> Value.Int(l.value + r.value) }
                    Operator.MINUS -> evaluateBinary<Value.Int>(left, right) { l, r -> Value.Int(l.value - r.value) }
                    Operator.MULTIPLY -> evaluateBinary<Value.Int>(left, right) { l, r -> Value.Int(l.value * r.value) }
                    Operator.DIVIDE -> evaluateBinary<Value.Int>(left, right) { l, r -> Value.Int(l.value / r.value) }
                    Operator.EQUALS -> evaluateBinary<Value.Bool>(left, right) { l, r -> Value.Bool(l.value == r.value) }
                    Operator.AND -> evaluateBinary<Value.Bool>(left, right) { l, r -> Value.Bool(l.value && r.value) }
                    Operator.OR -> evaluateBinary<Value.Bool>(left, right) { l, r -> Value.Bool(l.value || r.value) }

                }
            }
        }
    }


    inline fun <reified T> evaluateBinary(left: Value, right: Value, f: (T, T) -> Value): Value = f(left as T, right as T)


    fun toDeBrujin(expression: Expression, indexes: List<LocallyNamelessVariable.FreeVariable> = listOf()) : Expression {
        return when (val self = expression) {
            is Expression.App -> {
                Expression.App(toDeBrujin(self.func, indexes), toDeBrujin(self.arg, indexes))
            }
            is Expression.Binary -> Expression.Binary(toDeBrujin(self.left, indexes), self.operator, toDeBrujin(self.right, indexes))
            is Expression.Lambda -> {
                val newIndexes = indexes.plusElement(self.variable.variable as LocallyNamelessVariable.FreeVariable)
                val goDeeper = toDeBrujin(self.body, indexes)
                Expression.Lambda(Expression.Variable(LocallyNamelessVariable.BoundVariable(newIndexes.size - 1)), goDeeper)
            }
            is Expression.Literal -> self
            is Expression.Variable -> when (self.variable) {
                is LocallyNamelessVariable.BoundVariable -> self
                is LocallyNamelessVariable.FreeVariable -> {
                    if (indexes.contains(self.variable)) Expression.Variable(LocallyNamelessVariable.BoundVariable(indexes.reversed().indexOf(self.variable))) else self
                }
            }
        }
    }

    fun toNameless(expression: Expression, depth: Int, hasReachedFirstApp: Boolean) : Expression {
        return when (val self = expression) {
            is Expression.App -> {
                Expression.App(toNameless(self.func, depth, true), toNameless(self.arg, depth, true))
            }
            is Expression.Binary -> Expression.Binary(toNameless(self.left, depth, true), self.operator, toNameless(self.right, depth, true))
            is Expression.Lambda -> {
                // Solange die erste App noch nicht erreicht wurde soll jedes Lambda als Free returned werden
                if (!hasReachedFirstApp) { //Solange der nächste auch noch ein Lambda ist
                    if (self.body is Expression.Lambda) {
                        Expression.Lambda(Expression.Variable(LocallyNamelessVariable.FreeVariable(depth.toString())), toNameless(self.body, depth + 1, false))
                    } else toNameless(self.body, depth + 1, true)
            } else {
                Expression.Lambda(self.variable, toNameless(self.body, depth + 1, true))
                }
            }
            is Expression.Literal -> self
            is Expression.Variable -> {
                self.variable as LocallyNamelessVariable.BoundVariable
                val pointsTo = self.variable.index - depth
                if (pointsTo == 0) Expression.Variable(
                    LocallyNamelessVariable.FreeVariable((depth - self.variable.index).toString())
                ) else self
            }
        }
    }
    //
    fun locallyNamelessEvaluate(environment: ExpressionEnv, expression: Expression, depth: Int): Expression {
        return when (val self = expression) {
            is Expression.App -> {
                if (self.func is Expression.Lambda) {
                    val right = locallyNamelessEvaluate(environment, self.arg, depth)
                    val newEnvironment : ExpressionEnv = environment.put(self.func.variable.variable, right)
                    locallyNamelessEvaluate(newEnvironment, self.func.body, depth)
                    } else throw Exception("${self.func} is not a lambda")
                }
            is Expression.Lambda -> {

            }


            is Expression.Literal -> self
            is Expression.Variable -> when (self.variable) {
                is LocallyNamelessVariable.BoundVariable -> environment[]
                is LocallyNamelessVariable.FreeVariable -> self
            }

            is Expression.Binary ->

        }
            }

    fun shift(environment: Env): Env {
        val newEnvironment = mutableMapOf<LocallyNamelessVariable, Value>()
        for (variable in environment) {
            val locallyNamelessVariable = variable.key
            if (locallyNamelessVariable is LocallyNamelessVariable.BoundVariable) {
                newEnvironment[LocallyNamelessVariable.BoundVariable(locallyNamelessVariable.index + 1)] =
                    variable.value
            } else {
                newEnvironment[variable.key] = variable.value
            }
        }
        return newEnvironment.toPersistentMap()
    }

    fun getBoundVariableValueByIndex(environment: Env, index: Int) : Value {
        return environment.entries.first { (key, value) ->
            key is LocallyNamelessVariable.BoundVariable && key.index == index
         }.value
    }
    }

