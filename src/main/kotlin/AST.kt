data class Prog(val defs: List<Def>, val expression: Expression)
data class Def(val name: String, val expression: Expression)

sealed class Expression() {
    data class App(val func: Expression, val arg: Expression): Expression()
    data class Lambda(val parameter: String, val body: Expression): Expression()
    data class Literal(val value: Primitive): Expression()
    data class Variable(val name: String): Expression()
    data class Binary(val left: Expression, val operator: Operator, val right: Expression): Expression()
    data class If(val condition: Expression, val thenBranch: Expression, val elseBranch: Expression): Expression()
    data class Let(val name: String, val bound: Expression, val body: Expression): Expression()
    data class BuiltInFunction(val name: String): Expression()
}

enum class Operator {
    Add, Sub, Mul, Eq, Or, And
}

sealed class Primitive() {
    data class Int(val value: kotlin.Int) : Primitive()
    data class Bool(val value: Boolean) : Primitive()
    data class Text(val value: String) : Primitive()
}