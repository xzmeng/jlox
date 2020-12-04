package tool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws FileNotFoundException {
        String exprOutput = "src/lox/Expr.java";
        String stmtOutput = "src/lox/Stmt.java";
        List<String> exprTypes = Arrays.asList(
                "Assign     :   Token name, Expr value",
                "Binary     :   Expr left, Token operator, Expr right",
                "Unary      :   Token operator, Expr right",
                "Grouping   :   Expr expression",
                "Literal    :   Object value",
                "Variable   :   Token name"
        );
        List<String> stmtTypes = Arrays.asList(
                "Expression :   Expr expression",
                "Print      :   Expr expression",
                "Var        :   Token name, Expr initializer",
                "Block      :   List<Stmt> statements"
        );

        defineAst(exprOutput, "Expr", exprTypes);
        defineAst(stmtOutput, "Stmt", stmtTypes);

//        defineAst("", "Expr", exprTypes);
//        defineAst("", "Stmt", stmtTypes);
    }

    static void defineAst(String outputFile, String baseName, List<String> types) throws FileNotFoundException{
        PrintWriter writer;
        if (outputFile == null || outputFile.equals("")) {
            writer = new PrintWriter(System.out);
        } else{
            writer = new PrintWriter(outputFile);
        }
        // type -> Binary : Expr left, Token operator, Expr right
        writer.println("package lox;");
        writer.println("import java.util.List;");
        writer.println(String.format("abstract class %s {", baseName));
        // visitor interface
        defineVisitorInterface(writer, types, baseName);
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
        writer.flush();
        if (outputFile != null && !outputFile.equals("")){
            writer.close();
        }
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
        defineAccept(writer, className, baseName);
        writer.println();
        writer.println("    }");
    }

    static void defineVisitorInterface(PrintWriter writer, List<String> types, String baseName) {
        writer.println("    interface Visitor<R> {") ;
        // type -> Binary : Expr left, Token operator, Expr right
        for (String type : types) {
            String className = type.split(":")[0].trim();
            writer.println(String.format("        R visit%1$s%2$s(%1$s %3$s);", className, baseName, baseName.toLowerCase()));
        }
        writer.println("    }");
    }

    static void defineAccept(PrintWriter writer, String className, String baseName) {
        writer.println("        @Override");
        writer.println("        <R> R accept(Visitor<R> visitor) {");
        writer.println(String.format("            return visitor.visit%s%s(this);", className, baseName));
        writer.println("        }");
    }
}
