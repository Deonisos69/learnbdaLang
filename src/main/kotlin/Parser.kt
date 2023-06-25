import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream


fun parseFile(file: String): Prog {
    val lexer = testLexer(CharStreams.fromFileName(file))
    val tokens = CommonTokenStream(lexer)
    val parser = testParser(tokens)
    val tree = parser.init()
    val defs = mutableListOf<Def>()
    val expression = ExpressionVisitor(defs).visit(tree)
    return Prog(defs, expression)
}

class ExpressionVisitor(val defs: MutableList<Def>): testBaseVisitor<Expression>() {

    override fun visitDef(ctx: testParser.DefContext): Expression {
        defs.add(Def(ctx.NAME().text, this.visit(ctx.expression())))
        return super.visitDef(ctx)
    }

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
        return Expression.Variable(ctx.NAME().text)
    }

    override fun visitIntLit(ctx: testParser.IntLitContext): Expression {
        return Expression.Literal(Primitive.Int(ctx.INT().text.toInt()))
    }

    override fun visitBoolLit(ctx: testParser.BoolLitContext): Expression {
        val bool = ctx.BOOL().text == "true"
        return Expression.Literal(Primitive.Bool(bool))
    }

    override fun visitTextLit(ctx: testParser.TextLitContext): Expression {
        val text = ctx.TEXT().text
        return Expression.Literal(Primitive.Text(text))
    }

    override fun visitParenthesized(ctx: testParser.ParenthesizedContext): Expression {
        return this.visit(ctx.inner)
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

    override fun visitLet(ctx: testParser.LetContext): Expression {
        val name = ctx.NAME().text
        val bound = this.visit(ctx.bound)
        val body = this.visit(ctx.body)
        return Expression.Let(name, bound, body)
    }


}