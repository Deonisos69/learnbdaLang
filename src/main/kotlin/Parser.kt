import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream


fun parseExpression(input: String): Expression {
    val lexer = testLexer(CharStreams.fromString(input))
    val tokens = CommonTokenStream(lexer)
    val parser = testParser(tokens)
    val expressions = mutableListOf<Expression>()

    val tree = parser.init()
    return ExpressionVisitor(expressions).visit(tree)
}

class ExpressionVisitor(val expressions: MutableList<Expression>): testBaseVisitor<Expression>() {
    override fun visitApp(ctx: testParser.AppContext): Expression {
        val function = this.visit(ctx.func)
        val argument = this.visit(ctx.arg)
        return Expression.App(function, argument)
    }

    override fun visitLambda(ctx: testParser.LambdaContext): Expression {
        val binder = ctx.NAME().text
        val body = this.visit(ctx.expression())
        return Expression.Lambda(binder, body)
    }

    override fun visitVar(ctx: testParser.VarContext): Expression {
        return Expression.Variable(ctx.text)
    }

    override fun visitIntLit(ctx: testParser.IntLitContext): Expression {
        return Expression.Literal(Value.Int(ctx.text.toInt()))
    }

    override fun visitBoolLit(ctx: testParser.BoolLitContext): Expression {
        val bool = ctx.BOOL().text == "true"
        return Expression.Literal(Value.Bool(bool))
    }

    override fun visitBinary(ctx: testParser.BinaryContext): Expression {
        val left = this.visit(ctx.left)
        val operator = when(ctx.getChild(1).text) {
            "+" -> Operator.Add
            "-" -> Operator.Sub
            "*" -> Operator.Mul
            "==" -> Operator.Eq
            "||" -> Operator.Or
            "&&" -> Operator.And
            else -> throw Error("Unknown operator")
        }
        val right = this.visit(ctx.right)
        return Expression.Binary(left, operator, right)
    }

    override fun visitIf(ctx: testParser.IfContext): Expression {
        val condition = this.visit(ctx.condition)
        val then = this.visit(ctx.then)
        val else_ = this.visit(ctx.else_)
        return Expression.If(condition, then, else_)
    }


}