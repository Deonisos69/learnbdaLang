
fun main() {
    val parsedFile = parseFile("src/test.story")
    val (type, errors) = Typechecker().inferProg(parsedFile)
    errors.forEach { println(it) }
    println("${closureEvaluate(parsedFile)} : ${type.print()}")
}