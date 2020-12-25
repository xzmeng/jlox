package lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {

    static class ParseException extends RuntimeException {
    }

    List<Token> tokens;
    int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        try {
            while (!isAtEnd() && peek().type != TokenType.EOF) {
                statements.add(declaration());
            }
        } catch(ParseException e) {
            Loxy.hasError = true;
        }
        return statements;
    }

    Stmt declaration() {
        if (match(TokenType.VAR)) {
            return varDecl();
        }
        if (match(TokenType.FUN)) {
            return function("function");
        }
        return statement();
    }

    Stmt function(String kind) {
        Token name = consume(TokenType.IDENTIFIER, "Expected IDENTIFIER.");
        consume(TokenType.LEFT_PAREN, "Expected '('.");
        List<Token> parameters = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }
                parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");

        consume(TokenType.LEFT_BRACE, "Expect '{'");
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }

    Stmt varDecl() {
        Token name = consume(TokenType.IDENTIFIER, "Expected IDENTIFIER.");
        Expr initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }
        consume(TokenType.SEMICOLON, "Expected SEMICOLON.");
        return new Stmt.Var(name, initializer);
    }

    Stmt statement() {
        if (match(TokenType.PRINT)) {
            return printStatement();
        }
        if (match(TokenType.RETURN)) {
            return returnStatement();
        }
        if (match(TokenType.LEFT_BRACE)) {
            return new Stmt.Block(block());
        }
        if (match(TokenType.IF)) {
            return ifStatement();
        }
        if (match(TokenType.WHILE)) {
            return whileStatement();
        }
        if (match(TokenType.FOR)) {
            return forStatement();
        }
        return expressionStatement();
    }

    Stmt returnStatement() {
        Token keyword = previous();
        if (check(TokenType.SEMICOLON)) {
            Expr value = new Expr.Literal(null);
        }
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';'.");
        return new Stmt.Return(keyword, value);
    }

    Stmt ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '('");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '('.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')'.");
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    Stmt forStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '('.");
        Stmt initializer;
        if (match(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (match(TokenType.VAR)) {
            initializer = varDecl();
        } else {
            initializer = expressionStatement();
        }

        Expr condition;
        if (match(TokenType.SEMICOLON)) {
            condition = new Expr.Literal("true");
        } else {
            condition = expression();
            consume(TokenType.SEMICOLON, "Expected ';'.");
        }

        Expr increment;
        if (check(TokenType.RIGHT_PAREN)) {
            increment = null;
        } else {
            increment = expression();
        }
        consume(TokenType.RIGHT_PAREN, "Expected ')'.");

        Stmt body = statement();
        if (increment != null) {
            Stmt incrementStmt = new Stmt.Expression(increment);
            body = new Stmt.Block(Arrays.asList(body, incrementStmt));
        }
        Stmt whileLoop = new Stmt.While(condition, body);

        if (initializer != null) {
            return new Stmt.Block(Arrays.asList(initializer, whileLoop));
        } else {
            return whileLoop;
        }
    }

    List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd() && !check(TokenType.RIGHT_BRACE)) {
            statements.add(declaration());
        }
        consume(TokenType.RIGHT_BRACE, "Expected '}'.");
        return statements;
    }

    Stmt printStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expected ';'");
        return new Stmt.Print(expr);
    }

    Stmt expressionStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expected ';'");
        return new Stmt.Expression(expr);
    }

    Expr expression() {
        return assignment();
    }

    Expr assignment() {
        Expr expr = or();
        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr right = or();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, right);
            }

            throw new RuntimeError(equals, "Left value is not variable");
        }
        return expr;
    }

    Expr or() {
        Expr expr = and();
        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    Expr and() {
        Expr expr = equality();
        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    Expr equality() {
        Expr expr = comparison();
        while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    Expr comparison() {
        Expr expr = addition();
        while (match(TokenType.GREATER, TokenType.LESS, TokenType.GREATER_EQUAL,
                TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    Expr addition() {
        Expr expr = multiplication();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    Expr multiplication() {
        Expr expr = unary();
        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return call();
    }

    Expr call() {
        Expr expr = primary();
        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr);
            } else {
                break;
            }
        }
        return expr;
    }

    Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while(match(TokenType.COMMA));
        }
        Token paren = consume(TokenType.RIGHT_PAREN, "Expected ')'.");
        return new Expr.Call(callee, paren, arguments);
    }

    Expr primary() {
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(TokenType.TRUE)) return new Expr.Literal(true);
        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if (match(TokenType.NIL)) return new Expr.Literal(null);
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = equality();
            consume(TokenType.RIGHT_PAREN, "Expected ')'");
            return expr;
        }
        if (match(TokenType.IDENTIFIER)) return new Expr.Variable(previous());

        throw error(peek(), "Unknown token for primary.");
    }

    boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return (peek().type == type);
    }

    boolean isAtEnd() {
        return current >= tokens.size();
    }

    Token peek() {
        return tokens.get(current);
    }

    Token previous() {
        return tokens.get(current - 1);
    }

    Token advance() {
        current++;
        return tokens.get(current - 1);
    }

    Token consume(TokenType type, String msg) {
        if (check(type)) {
            return advance();
        } else {
            throw error(peek(), msg);
        }
    }

    ParseException error(Token token, String msg) {
        Loxy.error(token, msg);
        return new ParseException();
    }
}

















