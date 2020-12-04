package lox;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment();

    public void interpret(List<Stmt> statements) {
        if (Lox.hasError) {
            System.out.println("[interpreter]An error occurred during previous stage.");
            return;
        }
        for (Stmt stmt : statements) {
            try {
                execute(stmt);
            } catch(RuntimeError e) {
                Lox.runtimeError(e.token, e.toString());
            }
        }
    }

    void execute(Stmt stmt) {
        stmt.accept(this);
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
                    return left + (String)right;
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
                    return !(Boolean)value;
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


    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(value);
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null){
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        try {
            this.environment = new Environment(environment);
            for (Stmt statement : stmt.statements) {
                execute(statement);
            }
        } finally{
            this.environment = this.environment.enclosing;
        }
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        Object condition = evaluate(stmt.condition);
        if (isTruthy(condition)) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null){
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }
        return evaluate(expr.right);
    }

    Boolean isTruthy(Object value) {
        if (value instanceof Boolean) {
            return (Boolean)value;
        } else {
            return value != null;
        }
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }
}
