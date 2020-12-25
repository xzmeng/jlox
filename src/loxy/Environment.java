package loxy;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    private final Map<String, Object> values = new HashMap<>();

    final Environment enclosing;

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    void define(String name, Object value) {
        // here we allowed to redefine a variable
        // just follow the scheme's way
        // (Scheme allows redefining variables at the top level)
        values.put(name, value);
    }

    void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    Environment ancestor(int distance) {
        Environment env = this;
        while (distance > 0) {
            env = env.enclosing;
            distance --;
        }
        return env;
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        if (enclosing != null) {
            return enclosing.get(name);
        }
        // here we make it a runtime error when a variable is not found
        // it helps us to define recursive function easily
        // (if we make it a syntax error, it will be hard to write mutually
        // recursive function )
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}
