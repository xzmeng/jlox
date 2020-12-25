package lox;

import java.util.List;

public interface LoxyCallable {
    Object call(Interpreter interpreter, List<Object> arguments);
    int arity();
}
