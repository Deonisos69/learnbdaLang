
fun main() {
    val e = Evaluator()
    println(e.evaluate(emptyEnv ,parseExpression("2 + 2")))
}