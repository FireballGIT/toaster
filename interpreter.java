// ToasterInterpreter.java
// BAG Studios' Gradle-style build tool: Toaster

import java.util.*;

// Token class
class Token {
    public final String type;
    public final String value;

    public Token(String type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
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

// AST Base
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

    @Override
    public void accept(Visitor visitor) throws Exception {
        visitor.visitTaskNode(this);
    }
}

// Visitor interface
interface Visitor {
    void visitTaskNode(TaskNode node) throws Exception;
}

// Parser with syntax error handling
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
    public ParseException(String message) {
        super(message);
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
        for (ToasterPlugin plugin : plugins) plugin.onBeforeExecution();
    }

    public void runAfterExecution() {
        for (ToasterPlugin plugin : plugins) plugin.onAfterExecution();
    }
}

// Interpreter
class ToasterInterpreter implements Visitor {
    private final PluginManager pluginManager = new PluginManager();

    public void registerPlugin(ToasterPlugin plugin) {
        pluginManager.register(plugin);
    }

    public void execute(ASTNode ast) throws Exception {
        pluginManager.runBeforeExecution();
        ast.accept(this);
        pluginManager.runAfterExecution();
    }

    @Override
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

// Runner
public class ToasterRunner {
    public static void main(String[] args) {
        String input = "bake app butter";
        ToasterLexer lexer = new ToasterLexer();
        ToasterParser parser;
        ToasterInterpreter interpreter = new ToasterInterpreter();

        try {
            List<Token> tokens = lexer.tokenize(input);
            parser = new ToasterParser(tokens);
            ASTNode tree = parser.parse();
            interpreter.execute(tree);
        } catch (ParseException pe) {
            System.err.println("[Syntax Error] " + pe.getMessage());
        } catch (Exception e) {
            System.err.println("[Execution Error] " + e.getMessage());
        }
    }
}
