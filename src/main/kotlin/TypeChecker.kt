import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentHashMapOf

typealias Context = PersistentMap<String, Type>

class Typechecker {

    // Liste von Errors
    val errors: MutableList<String> = mutableListOf()

    // Fügt einen Error den Errors hinzu und gibt die Liste der Errors zurück
    fun error(message: String) = errors.add(message)

    // Vergleicht zwei Typen und fügt einen Error hinzu wenn die Typen ungleich sind
    fun equalType(message: String, actualType: Type, expectedType: Type) {
        if (actualType != expectedType) {
            error("TYPE-ERROR: $message, Expected ${expectedType.print()} but got ${actualType.print()}")
        }
    }

    // Geht das Programm durch und überprüft alle Typen
    fun inferProg(prog: Prog): Pair<Type, List<String>> {
        // Fügt die einzelnen Built In Funktionen mit ihren Typen zum BuiltInContext hinzu
        val builtinCtx: Context = builtIns.fold(persistentHashMapOf()) { acc, def ->
            acc.put(def.name, def.type)
        }
        // Fügt die einzelnen Defs mit ihren Typen zum normalen Context hinzu
        val ctx: Context = prog.defs.fold(builtinCtx) { acc, def ->
            acc.put(def.name, def.type)
        }
        // Für jedes Def Wird überprüft ob der inferierte Typ mit dem im Context angegebenen Typ übereinstimmt
        prog.defs.forEach { def ->
            val expressionType = infer(ctx, def.expression)
            equalType("When inferring a definition", expressionType, def.type)
        }
        // Gibt eine Liste mit Typen und zugehörigen Errors aus
        return infer(ctx, prog.expression) to errors
    }

    // Inferiert einen Typen einer Expression in einem gegebenen Context
    fun infer(ctx: Context, expression: Expression): Type {
        return when (expression) {
            // Linker Baum einer App muss eine Funktion sein, sonst gibt es ein Error.
            // Rechter Baum wird mit angegebenen Ausgabetyp des linken Baums verglichen.
            is Expression.App -> {
                val functionType = infer(ctx, expression.func)
                val argumentType = infer(ctx, expression.arg)
                when (functionType) {
                    is Type.Function -> {
                        equalType("when applying a function", argumentType, functionType.argumentType)
                        functionType.resultType
                    }

                    else -> throw Error("${functionType.print()} is not a function")
                }
            }

            is Expression.Binary -> {
                when (expression.operator) {
                    // Add, Sub, Mul und Equals müssen auf beiden Seiten Ints haben zum funktionieren. Wenn nicht
                    // wird ein Error hinzugefügt.
                    Operator.Add,
                    Operator.Sub,
                    Operator.Mul,
                    Operator.Eq -> {
                        val leftType = infer(ctx, expression.left)
                        val rightType = infer(ctx, expression.right)
                        equalType("as the left operand of ${expression.operator}", leftType, Type.Int)
                        equalType("as the right operand of ${expression.operator}", rightType, Type.Int)
                        Type.Int
                    }

                    // Or und And müssen auf beiden Seiten Bools haben
                    Operator.Or,
                    Operator.And -> {
                        val leftType = infer(ctx, expression.left)
                        val rightType = infer(ctx, expression.right)
                        equalType("as the left operand of ${expression.operator}", leftType, Type.Bool)
                        equalType("as the right operand of ${expression.operator}", rightType, Type.Bool)
                        Type.Bool
                    }

                    // Concat muss auf beiden Seiten Strings haben
                    Operator.Concat -> {
                        val leftType = infer(ctx, expression.left)
                        val rightType = infer(ctx, expression.right)
                        equalType("as the left operand of ${expression.operator}", leftType, Type.Text)
                        equalType("as the right operand of ${expression.operator}", rightType, Type.Text)
                        Type.Text
                    }
                }
            }

            // Wenn eine BuiltInFunction inferiert wird ist was schief gelaufen.
            is Expression.BuiltInFunction -> throw Error("Should not need to infer a Builtin")
            // In einer If Bedingung muss die Kondition ein Bool sein. Außerdem müssen then und else den gleichen Typ
            // haben um einen korrekten Ausgabewert der If Expression zu gewährleisten
            is Expression.If -> {
                val conditionType = infer(ctx, expression.condition)
                equalType("In an If condition", conditionType, Type.Bool)
                val thenType = infer(ctx, expression.thenBranch)
                val elseType = infer(ctx, expression.elseBranch)
                equalType("In if branches", elseType, thenType)
                thenType
            }

            // Jede Lambda Funktion wird in den Context gepackt und dann werden die Typen verglichen
            is Expression.Lambda -> {
                val parameterType = expression.type
                val newCtx = ctx.put(expression.parameter, parameterType)
                val bodyType = infer(newCtx, expression.body)
                Type.Function(parameterType, bodyType)
            }

            // Jedes Let wird mit dem Typ in den Context gepackt und es wird der Typ der gebundenen Funktion ausgegeben
            is Expression.Let -> {
                val boundType = infer(ctx, expression.bound)
                val newCtx = ctx.put(expression.name, boundType)
                val bodyType = infer(newCtx, expression.body)
                bodyType
            }

            // Der Typ der Primitive wird ausgegeben
            is Expression.Literal -> when (expression.value) {
                is Primitive.Bool -> Type.Bool
                is Primitive.Int -> Type.Int
                is Primitive.Text -> Type.Text
            }

            // Der Typ einer Variable(Sowohl Builtin, als auch def, als auch Let und Lambda) wird aus dem Context
            // gelesen
            is Expression.Variable -> ctx[expression.name] ?: throw Error("Unknown variable ${expression.name}")
        }
    }

}