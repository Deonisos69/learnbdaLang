
fun main() {
    val eval = Evaluator()
    val term = Expression.App(
        Expression.Lambda(
            Expression.Variable(LocallyNamelessVariable.FreeVariable("a")), Expression.Variable(LocallyNamelessVariable.FreeVariable("a"))
        ), Expression.Literal(Primitive.Int(2))
    )
    println(term)
    println(eval.toDeBrujin(term))
//    eval.locallyNamelessEvaluate(
//        eval.toDeBrujin(
//
//        )
//    )
}