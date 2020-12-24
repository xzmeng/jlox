package lox;

class Token {
  final TokenType type;
  // type of token
  final String lexeme;
  // token string
  final Object literal;
  //
  final int line; // [location]
  // line number

  // constructor
  Token(TokenType type, String lexeme, Object literal, int line) {
    this.type = type;
    this.lexeme = lexeme;
    this.literal = literal;
    this.line = line;
  }

  public String toString() {
    return type + " " + lexeme + " " + literal;
  }
}
