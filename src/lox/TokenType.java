package lox;

public enum TokenType {
    // single-character tokens
    // (            )            {          }
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    // +    -     *     ,
    PLUS, MINUS, STAR, COMMA,
    // ;        .      /
    SEMICOLON, DOT, SLASH,
    // !   !=
    BANG, BANG_EQUAL,
    // =    ==           >        >=             <      <=
    EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
    // if  else  while for
    IF, ELSE, WHILE, FOR,
    // var fun class, return, or, and, nil, true, false, super, this
    VAR, FUN, CLASS, RETURN, OR, AND, NIL, TRUE, FALSE, SUPER, THIS,
    // he_123   "hello" 123
    IDENTIFIER, STRING, NUMBER,
    // print, end of file
    PRINT, EOF
}
