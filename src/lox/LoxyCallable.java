package lox;

import java.util.List;

interface LoxyCallable {
  int arity();
  Object call(Interpreter interpreter, List<Object> arguments);
}
