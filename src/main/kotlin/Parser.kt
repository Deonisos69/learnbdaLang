import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.RuleNode
import org.antlr.v4.runtime.tree.TerminalNode
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream


//fun parseFile(file: String): Prog {
//    val lexer = testLexer(CharStreams.fromFileName(file))
//    val tokens = CommonTokenStream(lexer)
//    val parser = testParser(tokens)
//    val tree = parser.init()
//    val defs = mutableListOf<Def>()
//    val expression = ExpressionVisitor(defs).visit(tree)
//    return Prog(defs, expression)
//}

class ExpressionVisitor() : learnbdaLangBaseVisitor<Expression>() {
    override fun visitBOOL(ctx: learnbdaLangParser.BOOLContext): Expression {
        return super.visitBOOL(ctx)
    }

    override fun visitINT(ctx: learnbdaLangParser.INTContext): Expression {
        val int = ctx.INT().text.toInt()
        return Expression.Literal(Primitive.Int(int))
    }

    override fun visitSTRING(ctx: learnbdaLangParser.STRINGContext): Expression {
        return Expression.Literal(Primitive.String(ctx.STRING().text.trimStart('"').trimEnd('"').replace("\\n", "\n")))
    }

    override fun visitVariable(ctx: learnbdaLangParser.VariableContext): Expression {
        return Expression.Variable(LocallyNamelessVariable.FreeVariable(ctx.NAME().text))
    }

    override fun visitBinary(ctx: learnbdaLangParser.BinaryContext): Expression {
        val left = this.visit(ctx.left)
        val operator = when (ctx.operator.text) {
            "+" -> Operator.PLUS
            "-" -> Operator.MINUS
            "*" -> Operator.MULTIPLY
            "/" -> Operator.DIVIDE
            "==" -> Operator.EQUALS
            "&&" -> Operator.AND
            "||" -> Operator.OR
            else -> throw Exception("Ik weiß nicht wat du von mir willst")
        }
        val right = this.visit(ctx.right)
        return Expression.Binary(left, operator, right)
    }

    override fun visitApp(ctx: learnbdaLangParser.AppContext): Expression {
        val function = this.visit(ctx.function)
        val argument = this.visit(ctx.argument)
        return Expression.App(function, argument)
    }

    override fun visitParenthesized(ctx: learnbdaLangParser.ParenthesizedContext): Expression {
        return this.visit(ctx.inner)
    }

    override fun visitLambda(ctx: learnbdaLangParser.LambdaContext): Expression {
        val parameter = ctx.param.text
        val body = this.visit(ctx.body)
        return Expression.Lambda(Expression.Variable(LocallyNamelessVariable.FreeVariable(parameter)), body)
    }

}
