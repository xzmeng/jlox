package loxy;

import java.util.List;

public class LoxyFunction implements LoxyCallable {

    private final Stmt.Function declaration;
    private final Environment closure;

    LoxyFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++){
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch(Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
