import java.lang.Exception

class Evaluator() {
    fun evaluate(env: Env, expression: Expression): Value {
        return when (expression) {
            is Expression.App -> {
                when (val closure = evaluate(env, expression.func)) {
                    is Value.Closure -> {
                        val arg = evaluate(env, expression.arg)
                        val newEnv = closure.env.put(closure.parameter, arg)
                        evaluate(newEnv, closure.body)
                    }
                    is Value.Int, is Value.Bool -> throw Exception("$expression is not a function")
                }
            }
            is Expression.Lambda -> {
                Value.Closure(env, expression.parameter, expression.body)
            }
            is Expression.Literal -> {
                expression.value
            }
            is Expression.Variable -> {
                env[expression.name] ?: throw Exception("$expression is unbound")
            }
            is Expression.Binary -> {
                val left = evaluate(env, expression.left)
                val right = evaluate(env, expression.right)
                when (expression.operator) {
                    Operator.Add -> evaluateBinary<Value.Int>(left, right){ l, r -> Value.Int(l.value + r.value)}
                    Operator.Sub -> evaluateBinary<Value.Int>(left, right){ l, r -> Value.Int(l.value - r.value)}
                    Operator.Mul -> evaluateBinary<Value.Int>(left, right){ l, r -> Value.Int(l.value * r.value)}
                    Operator.Eq -> evaluateBinary<Value.Bool>(left, right){ l, r -> Value.Bool(l.value == r.value)}
                    Operator.Or -> evaluateBinary<Value.Bool>(left, right){ l, r -> Value.Bool(l.value || r.value)}
                    Operator.And -> evaluateBinary<Value.Bool>(left, right){ l, r -> Value.Bool(l.value && r.value)}
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
        }
    }
}

inline fun <reified T> evaluateBinary(left: Value, right: Value, f: (T, T) -> Value): Value {
    val leftCasted = left as? T ?: throw Error("Expected a ${T::class.simpleName} but got $left")
    val rightCasted = right as? T ?: throw Error("Expected a ${T::class.simpleName} but got $right")
    return f(leftCasted, rightCasted)
}