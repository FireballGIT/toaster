import java.util.*;

class ASTNode {
    String type;
    String name;
    Map<String, Object> properties = new HashMap<>();
    List<ASTNode> children = new ArrayList<>();

    ASTNode(String type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        return type + " " + name + " " + properties + " " + children;
    }
}

public class ToasterParser {
    private final List<Token> tokens;
    private int current = 0;

    public ToasterParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<ASTNode> parse() {
        List<ASTNode> ast = new ArrayList<>();
        while (!isAtEnd()) {
            ast.add(parseStatement());
        }
        return ast;
    }

    private ASTNode parseStatement() {
        Token token = peek();

        switch (token.type) {
            case "TASK":
                return parseTask();
            case "SPREAD":
                return parseSpread();
            default:
                throw new RuntimeException("Unexpected token: " + token);
        }
    }

    private ASTNode parseTask() {
        Token taskToken = advance(); // bake, pack, clean, etc.
        Token nameToken = advance(); // task name or block

        ASTNode node = new ASTNode("TASK", taskToken.value);
        node.properties.put("target", nameToken.value);

        if (match("LBRACE")) {
            while (!check("RBRACE") && !isAtEnd()) {
                Token key = advance(); // key (e.g. version)
                consume("EQUAL", "Expected '=' after key.");
                Token value = advance(); // value (string or number)
                node.properties.put(key.value, value.value);
            }
            consume("RBRACE", "Expected '}' at end of block.");
        }

        return node;
    }

    private ASTNode parseSpread() {
        advance(); // skip spread
        consume("COLON", "Expected ':' after spread");
        Token spreadType = advance(); // jam, butter, etc.

        ASTNode node = new ASTNode("SPREAD", spreadType.value);
        return node;
    }

    // === Helper Methods ===

    private boolean match(String type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    private boolean check(String type) {
        if (isAtEnd()) return false;
        return peek().type.equals(type);
    }

    private Token consume(String type, String errorMessage) {
        if (check(type)) return advance();
        throw new RuntimeException("[Line " + peek().line + "] " + errorMessage);
    }

    private Token advance() {
        return tokens.get(current++);
    }

    private Token peek() {
        return tokens.get(current);
    }

    private boolean isAtEnd() {
        return current >= tokens.size();
    }
}
