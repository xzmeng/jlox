package lox;

public class Interpreter implements Expr.Visitor<Object> {

    public void interpret(Expr expr) {
        if (Lox.hasError) {
            System.out.println("[interpreter]An error occurred during previous stage.");
            return;
        }
        try {
            Object value = evaluate(expr);
            System.out.println(value);
        } catch(RuntimeError e) {
            Lox.runtimeError(e.token, e.toString());
        }
    }

    Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case EQUAL_EQUAL -> {
                return left.equals(right);
            }
            case BANG_EQUAL -> {
                return !left.equals(right);
            }
            case GREATER -> {
                checkNumberOperands(expr.operator, left, right);
                return (float)left > (float)right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (float)left >= (float)right;
            }
            case LESS -> {
                checkNumberOperands(expr.operator, left, right);
                return (float)left < (float)right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (float)left <= (float)right;
            }
            case STAR -> {
                checkNumberOperands(expr.operator, left, right);
                return (float)left * (float)right;
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                return (float)left / (float)right;
            }
            case PLUS -> {
                if (left instanceof Float && right instanceof Float) {
                    return (float)left + (float)right;
                } else if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                } else {
                    throw new RuntimeError(expr.operator, "Unexpected operands");
                }
            }
            case MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                return (float)left - (float)right;
            }
        }
        throw new RuntimeError(expr.operator, "Unknown operation.");
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object value = evaluate(expr.right);
        switch (expr.operator.type) {
            case MINUS -> {
                checkNumberOperand(expr.operator, value);
                return -(float) value;
            }
            case BANG -> {
                if (value instanceof Boolean) {
                    return value;
                } else {
                    return value == null;
                }
            }
            default -> throw new RuntimeError(expr.operator, "Unexpected operator.");
        }
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    void checkNumberOperand(Token operator, Object value) {
        if (!(value instanceof Float)) {
            throw new RuntimeError(operator, "Number expected.");
        }
    }

    void checkNumberOperands(Token operator, Object left, Object right) {
        if (!(left instanceof Float) || !(right instanceof Float)) {
            throw new RuntimeError(operator, "Number expected.");
        }
    }

}
