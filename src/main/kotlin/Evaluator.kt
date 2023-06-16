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
            is Expression.Lambda -> Value.Closure(env, expression.parameter, expression.body)
            is Expression.Literal -> expression.value
            is Expression.Variable -> env[expression.name] ?: throw Exception("$expression is unbound")
        }
    }
}