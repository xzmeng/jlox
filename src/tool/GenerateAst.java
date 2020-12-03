package tool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        String outputFile = "src/lox/Expr.java";
        PrintWriter writer = new PrintWriter(outputFile);
        List<String> types = Arrays.asList(
                "Binary: Expr left, Token operator, Expr right",
                "Unary: Token operator, Expr right",
                "Grouping: Expr expression",
                "Literal: Object value"
        );
        defineAst(writer, "Expr", types);
        writer.close();
    }

    static void defineAst(PrintWriter writer, String baseName, List<String> types) {
        // type -> Binary : Expr left, Token operator, Expr right
        writer.println("package lox;");
        writer.println(String.format("abstract class %s {", baseName));
        // visitor interface
        defineVisitorInterface(writer, types);
        writer.println();
        // define accept method for visitor pattern
        writer.println("    abstract <R> R accept(Visitor<R> visitor);");
        writer.println();
        // define subclasses
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fieldList = type.split(":")[1].trim();
            defineType(writer, baseName, className, fieldList);
            writer.println();
        }
        writer.println("}");
    }

    static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        // class head
        writer.println(String.format("    static class %s extends %s {", className, baseName));
        // class fields
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            writer.println(String.format("        final %s;", field));
        }
        writer.println();

        // constructor
        writer.println(String.format("        %s(%s) {", className, fieldList));
        for (String field : fields) {
            String fieldName = field.split(" ")[1];
            writer.println(String.format("            this.%s = %s;", fieldName, fieldName));
        }
        writer.println("        }");
        writer.println();

        // accept method for visitor pattern
        defineAccept(writer, className);
        writer.println();
        writer.println("    }");
    }

    static void defineVisitorInterface(PrintWriter writer, List<String> types) {
        writer.println("    interface Visitor<R> {") ;
        // type -> Binary : Expr left, Token operator, Expr right
        for (String type : types) {
            String className = type.split(":")[0].trim();
            writer.println(String.format("        R visit%sExpr(%s expr);", className, className));
        }
        writer.println("    }");
    }

    static void defineAccept(PrintWriter writer, String className) {
        writer.println("        @Override");
        writer.println("        <R> R accept(Visitor<R> visitor) {");
        writer.println(String.format("            return visitor.visit%sExpr(this);", className));
        writer.println("        }");
    }
}
