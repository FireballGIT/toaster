// File: Toaster.java

import java.io.*;
import java.util.*;

// Token class
class Token {
    public final String type;
    public final String value;

    public Token(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String toString() {
        return type + ":" + value;
    }
}

// Lexer
class ToasterLexer {
    public List<Token> tokenize(String source) {
        List<Token> tokens = new ArrayList<>();
        String[] parts = source.trim().split("\\s+");
        for (String part : parts) {
            tokens.add(new Token("WORD", part));
        }
        return tokens;
    }
}

// AST Node
abstract class ASTNode {
    public abstract void accept(Visitor visitor) throws Exception;
}

class TaskNode extends ASTNode {
    public final String taskName;
    public final List<String> args;

    public TaskNode(String taskName, List<String> args) {
        this.taskName = taskName;
        this.args = args;
    }

    public void accept(Visitor visitor) throws Exception {
        visitor.visitTaskNode(this);
    }
}

// Visitor interface
interface Visitor {
    void visitTaskNode(TaskNode node) throws Exception;
}

// Parser
class ToasterParser {
    private final List<Token> tokens;
    private int current = 0;

    public ToasterParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ASTNode parse() throws ParseException {
        if (tokens.isEmpty()) throw new ParseException("No tokens to parse.");

        Token taskToken = consume("WORD", "Expected a task name.");
        String taskName = taskToken.value;

        List<String> args = new ArrayList<>();
        while (!isAtEnd()) {
            Token arg = consume("WORD", "Expected an argument.");
            args.add(arg.value);
        }

        Set<String> validTasks = Set.of("bake", "clean", "pack", "runtimetest");
        if (!validTasks.contains(taskName.toLowerCase())) {
            throw new ParseException("Unknown task: " + taskName);
        }

        return new TaskNode(taskName, args);
    }

    private boolean isAtEnd() {
        return current >= tokens.size();
    }

    private Token consume(String expectedType, String errorMsg) throws ParseException {
        if (isAtEnd()) throw new ParseException(errorMsg);
        Token token = tokens.get(current);
        if (!token.type.equals(expectedType)) throw new ParseException(errorMsg);
        current++;
        return token;
    }
}

class ParseException extends Exception {
    public ParseException(String msg) {
        super(msg);
    }
}

// Plugin system
interface ToasterPlugin {
    void onBeforeExecution();
    void onAfterExecution();
}

class PluginManager {
    private final List<ToasterPlugin> plugins = new ArrayList<>();

    public void register(ToasterPlugin plugin) {
        plugins.add(plugin);
    }

    public void runBeforeExecution() {
        for (ToasterPlugin p : plugins) p.onBeforeExecution();
    }

    public void runAfterExecution() {
        for (ToasterPlugin p : plugins) p.onAfterExecution();
    }
}

// Context class
class ToasterContext {
    private final PluginManager pluginManager = new PluginManager();

    public PluginManager getPluginManager() {
        return pluginManager;
    }
}

// Interpreter
class ToasterInterpreter implements Visitor {
    private final ToasterContext context;

    public ToasterInterpreter(ToasterContext context) {
        this.context = context;
    }

    public void execute(ASTNode ast) throws Exception {
        context.getPluginManager().runBeforeExecution();
        ast.accept(this);
        context.getPluginManager().runAfterExecution();
    }

    public void visitTaskNode(TaskNode node) throws Exception {
        switch (node.taskName.toLowerCase()) {
            case "bake": bake(node.args); break;
            case "clean": clean(node.args); break;
            case "pack": pack(node.args); break;
            case "runtimetest": runtimeTest(node.args); break;
            default: throw new Exception("Unknown task: " + node.taskName);
        }
    }

    private void bake(List<String> args) {
        System.out.println("[Toaster] Baking: " + String.join(", ", args));
    }

    private void clean(List<String> args) {
        System.out.println("[Toaster] Cleaning: " + String.join(", ", args));
    }

    private void pack(List<String> args) {
        System.out.println("[Toaster] Packing: " + String.join(", ", args));
    }

    private void runtimeTest(List<String> args) {
        System.out.println("[Toaster] Running tests: " + String.join(", ", args));
    }
}

// Main
public class Toaster {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java Toaster <file.toast>");
            return;
        }

        String fileName = args[0];
        StringBuilder source = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                source.append(line).append(" ");
            }
        } catch (IOException e) {
            System.out.println("Failed to read .toast file: " + e.getMessage());
            return;
        }

        ToasterLexer lexer = new ToasterLexer();
        List<Token> tokens = lexer.tokenize(source.toString());

        ToasterParser parser = new ToasterParser(tokens);
        ASTNode ast;
        try {
            ast = parser.parse();
        } catch (ParseException e) {
            System.out.println("Parse error: " + e.getMessage());
            return;
        }

        ToasterContext context = new ToasterContext();
        context.getPluginManager().register(new ExamplePlugin());

        ToasterInterpreter interpreter = new ToasterInterpreter(context);
        try {
            interpreter.execute(ast);
        } catch (Exception e) {
            System.out.println("Runtime error: " + e.getMessage());
        }
    }
}
