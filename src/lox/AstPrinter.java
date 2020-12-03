package lox;

public class AstPrinter implements Expr.Visitor<String> {
    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return String.format("(%s %s %s)", expr.operator.lexeme, expr.left.accept(this),
                expr.right.accept(this));
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return String.format("(%s %s)", expr.operator.lexeme, expr.right.accept(this));
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return String.format("(group %s)", expr.expression.accept(this));
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return expr.value.toString();
    }

    public String print(Expr expr) {
        if (Lox.hasError) return "An error occured during parse. Cannot print the expression.";
        return expr.accept(this);
    }

    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(123)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Literal(45.67)));

        System.out.println(new AstPrinter().print(expression));
    }
}
