package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Scanner {
    String source;
    int current = 0;
    int start = 0;
    int line_number = 1;
    ArrayList<Token> tokens = new ArrayList<>();
    static Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("for", TokenType.FOR);
        keywords.put("var", TokenType.VAR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("class", TokenType.CLASS);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("or", TokenType.OR);
        keywords.put("and", TokenType.AND);
        keywords.put("nil", TokenType.NIL);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
    }

    Scanner(String source) {
        this.source = source;
    }

    public ArrayList<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line_number));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG); break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS); break;
            case '/':
                if (match('/')) {
                    // eliminate comments
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            case '"':
                string();
                break;
            // eliminate spaces
            case ' ':
            case '\r':
            case '\t':
                break;
            // new line
            case '\n':
                line_number++;
                break;
            default:
                if (isNumeric(c)) {
                    number();
                } else if (isAlphabetUnderscore(c)) {
                    identifier();
                } else {
                    Loxy.hasError = true;
                    Loxy.error(line_number, "Unexpected character.");
                }
        }
    }

    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        tokens.add(new Token(type, source.substring(start, current), literal, line_number));
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }


    private boolean isNumeric(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlphabetUnderscore(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private void number() {
        // deal with the integer part
        while (isNumeric(peek())) advance();
        // deal with the double point part
        if (peek() == '.' && isNumeric(peekNext())) {
            advance();
        }
        while (isNumeric(peek())) advance();
        double literal = Double.parseDouble(source.substring(start, current));
        addToken(TokenType.NUMBER, literal);
    }

    private void identifier() {
        while (isAlphabetUnderscore(peek())) advance();
        TokenType type = keywords.get(source.substring(start, current));
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) advance();
        if (isAtEnd()) {
            Loxy.hasError = true;
            Loxy.error(line_number, "Unterminated string.");
            return;
        }
        advance(); // consume the right quote
        String literal = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, literal);
    }

}
