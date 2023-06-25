data class Prog(val defs: List<Def>, val expression: Expression)
data class Def(val name: String, val expression: Expression, val type: Type)

sealed class Expression() {
    // App ist das Anwenden eines Wertes auf eine Funktion
    data class App(val func: Expression, val arg: Expression): Expression()
    data class Lambda(val parameter: String, val type: Type, val body: Expression): Expression()
    data class Literal(val value: Primitive): Expression()
    data class Variable(val name: String): Expression()
    data class Binary(val left: Expression, val operator: Operator, val right: Expression): Expression()
    data class If(val condition: Expression, val thenBranch: Expression, val elseBranch: Expression): Expression()
    data class Let(val name: String, val bound: Expression, val body: Expression): Expression()
    data class BuiltInFunction(val name: String): Expression()
}

enum class Operator {
    Add, Sub, Mul, Eq, Or, And, Concat
}

sealed class Primitive() {
    data class Int(val value: kotlin.Int) : Primitive()
    data class Bool(val value: Boolean) : Primitive()
    data class Text(val value: String) : Primitive()
}

sealed class Type {
    // Das Keyword object legt fest, dass der Typ ein Singleton ist. Kann also nur eine Instanz davon geben
    object Int: Type()
    object Bool: Type()
    object Text: Type()
    data class Function(val argumentType: Type, val resultType: Type): Type()

    fun print(): String = printInner(false)

    private fun printInner(nested: Boolean): String {
        return when(this) {
            Int -> "Int"
            Bool -> "Bool"
            Text -> "Text"
            is Function -> {
                // Wenn der Typ des Objektes eine geschachtelte Funktion ist, dann benutze Klammern. Ansonsten nicht.
                val inner = "${argumentType.printInner(true)} -> ${resultType.printInner(false)}"
                if (nested) "($inner)" else inner
            }
        }
    }
}