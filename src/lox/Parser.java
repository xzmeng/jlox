package lox;

import java.util.List;

public class Parser {
    static class ParseException extends RuntimeException{ }

    List<Token> tokens;
    int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return equality();
        } catch(ParseException e) {
            Lox.hasError = true;
            return null;
        }
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
            consume(TokenType.RIGHT_PAREN);
            return expr;
        }

        throw error(peek(), "Unkown token for primary.");
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

    void advance() {
        current++;
    }

    void consume(TokenType type) {
        if (check(type)) {
            advance();
        } else {
            throw error(peek(), "Unexpected token.");
        }
    }

    ParseException error(Token token, String msg) {
        Lox.error(token, msg);
        return new ParseException();
    }
}

















