package loxy;

public class Token {
    TokenType type; // used for the parser
    String lexeme;  // the the word from the source code
    Object literal; // such as integer or string
    int line_number;

    Token(TokenType type, String lexeme, Object literal, int line_number) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;

        this.line_number = line_number;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
