package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();

    Interpreter() {
        environment.define("clock", new LoxCallable(){
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return System.currentTimeMillis() / 1000.0;
            }

            @Override
            public int arity() {
                return 0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

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
                return (double)left > (double)right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            }
            case LESS -> {
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            }
            case STAR -> {
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            }
            case PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                } else if (left instanceof String && right instanceof String) {
                    return left + (String)right;
                } else {
                    throw new RuntimeError(expr.operator, "Unexpected operands");
                }
            }
            case MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
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
                return -(double) value;
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
        if (!(value instanceof Double)) {
            throw new RuntimeError(operator, "Number expected.");
        }
    }

    void checkNumberOperands(Token operator, Object left, Object right) {
        if (!(left instanceof Double) || !(right instanceof Double)) {
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
        return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
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
        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }
        return value;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
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

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
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

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);
        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes");
        }

        LoxCallable function = (LoxCallable)callee;
        if (function.arity() != arguments.size()) {
            throw new RuntimeError(expr.paren, "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = evaluate(stmt.value);
        throw new Return(value);
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }
}
