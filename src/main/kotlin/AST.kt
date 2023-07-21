sealed class Expression() {
    // Eine Expression ist ein Knoten im AST Baum
    // App ist das Anwenden einer Funktion auf einen Wert
    data class App(val func: Expression, val arg: Expression): Expression()
    data class Lambda(val variable: Variable, val body: Expression): Expression()
    data class Literal(val primitive: Primitive): Expression()
    data class Variable(val variable: LocallyNamelessVariable): Expression()
    data class Binary(val left: Expression, val operator: Operator, val right: Expression): Expression()
}

enum class Operator {
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    EQUALS,
    AND,
    OR
}

sealed class LocallyNamelessVariable() {
    data class FreeVariable(val name: String): LocallyNamelessVariable()
    data class BoundVariable(val index: Int): LocallyNamelessVariable()
}


sealed class Primitive() {
    data class Int(val value: kotlin.Int) : Primitive()
    data class Bool(val value: Boolean) : Primitive()
    data class String(val value: kotlin.String) : Primitive()
}