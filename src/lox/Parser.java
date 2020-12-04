package lox;

import java.util.ArrayList;
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
            Lox.hasError = true;
        }
        return statements;
    }

    Stmt declaration() {
        if (match(TokenType.VAR)) {
            return varDecl();
        }
        return statement();
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
        if (match(TokenType.LEFT_BRACE)) {
            return new Stmt.Block(block());
        }
        return expressionStatement();
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
        Expr expr = equality();
        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr right = assignment();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, right);
            }

            throw new RuntimeError(equals, "Left value is not variable");
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
        return primary();
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
        Lox.error(token, msg);
        return new ParseException();
    }
}

















