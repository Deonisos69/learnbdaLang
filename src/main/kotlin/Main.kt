fun main() {
    val e = Evaluator()
    println(
        e.evaluate(emptyEnv,
            Expression.App(
                Expression.Lambda("x", Expression.Variable("x")),
                Expression.Literal(Value.Int(2))
            )
        )
    )
}