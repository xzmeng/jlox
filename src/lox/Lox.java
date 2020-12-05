package lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Lox {
    static boolean hasError = false;
    static Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            runPrompt();
        } else if (args.length == 1) {
            String filepath = args[0];
            runFile(filepath);
        }
    }

    public static void runPrompt() throws IOException {
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            line = reader.readLine();
            if (line == null) break;
            run(line);
            hasError = false;
        }
    }

    public static void runFile(String filepath) throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(filepath));
        String source = new String(bytes);
        run(source);
        if (hasError) {
            System.exit(65);
        }
    }

    public static void run(String source) {
        Scanner scanner = new Scanner(source);
        ArrayList<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (hasError) return;
        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        interpreter.interpret(statements);
    }

    static void error(int line_number, String msg) {
        report(line_number, "", msg);
    }

    static void report(int line_number, String where, String msg) {
        System.out.printf("[line %s] %s: %s%n", line_number, where, msg);
    }

    static void error(Token token, String msg) {
        if (token.type == TokenType.EOF) {
            report(token.line_number, " at end", msg);
        } else {
            report(token.line_number, " at " + token.lexeme, msg);
        }
    }

    static void runtimeError(Token token, String msg) {
        hasError = true;
        report(token.line_number, token.lexeme, msg);
    }
}
