package lox;

enum TokenType {
  // Single-character tokens.
  LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
  // (           )              {            }
  COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,
  // ,    .     -      +      ;         /      *

  // One or two character tokens.
  BANG, BANG_EQUAL,
  // !     !=
  EQUAL, EQUAL_EQUAL,
  // =       ==
  GREATER, GREATER_EQUAL,
  // >          >=
  LESS, LESS_EQUAL,
  // <       <=
  // Literals.
  IDENTIFIER, STRING, NUMBER,
  // he_12a    hello   123

  // Keywords.
  AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
  // and class  else  false fun  for if  nil   or
  PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,
  // print  return  super  this true var while

  EOF

}
