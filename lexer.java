import java.util.*;

class Token {
    public final String type;
    public final String value;
    public final int line;

    public Token(String type, String value, int line) {
        this.type = type;
        this.value = value;
        this.line = line;
    }

    public String toString() {
        return "[" + line + "] " + type + "(" + value + ")";
    }
}

public class ToasterLexer {

    private final String source;
    private int position = 0;
    private int line = 1;
    private final List<Token> tokens = new ArrayList<>();

    public ToasterLexer(String source) {
        this.source = source;
    }

    public List<Token> tokenize() {
        while (!isAtEnd()) {
            skipWhitespace();
            if (isAtEnd()) break;

            char current = peek();

            if (Character.isLetter(current)) {
                tokens.add(identifier());
            } else if (Character.isDigit(current)) {
                tokens.add(number());
            } else if (current == '"') {
                tokens.add(string());
            } else if (current == '#') {
                singleLineComment();
            } else if (peekAhead(3).equals("/#/")) {
                multiLineComment();
            } else {
                switch (current) {
                    case ':' -> addToken("COLON", ":");
                    case '=' -> addToken("EQUAL", "=");
                    case '+' -> addToken("PLUS", "+");
                    case '-' -> addToken("MINUS", "-");
                    case '{' -> addToken("LBRACE", "{");
                    case '}' -> addToken("RBRACE", "}");
                    case '(' -> addToken("LPAREN", "(");
                    case ')' -> addToken("RPAREN", ")");
                    case ',' -> addToken("COMMA", ",");
                    default -> addToken("UNKNOWN", String.valueOf(current));
                }
                advance();
            }
        }

        return tokens;
    }

    private Token identifier() {
        StringBuilder result = new StringBuilder();
        while (!isAtEnd() && (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            result.append(advance());
        }

        String word = result.toString();

        return switch (word.toLowerCase()) {
            case "bake", "clean", "pack", "runtimetest" -> new Token("TASK", word, line);
            case "butter", "jam" -> new Token("SPREAD", word, line); // NO 👏 CREAM 👏 CHEESE
            case "alpha", "beta", "stable", "delta", "predelta" -> new Token("SUBJECT", word, line);
            default -> new Token("IDENTIFIER", word, line);
        };
    }

    private Token number() {
        StringBuilder result = new StringBuilder();
        while (!isAtEnd() && (Character.isDigit(peek()) || peek() == '.')) {
            result.append(advance());
        }
        return new Token("NUMBER", result.toString(), line);
    }

    private Token string() {
        advance(); // skip opening "
        StringBuilder result = new StringBuilder();
        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\n') line++;
            result.append(advance());
        }
        advance(); // skip closing "
        return new Token("STRING", result.toString(), line);
    }

    private void singleLineComment() {
        while (!isAtEnd() && peek() != '\n') advance();
    }

    private void multiLineComment() {
        for (int i = 0; i < 3; i++) advance(); // skip "/#/"
        while (!isAtEnd() && !peekAhead(3).equals("###")) {
            if (peek() == '\n') line++;
            advance();
        }
        for (int i = 0; i < 3 && !isAtEnd(); i++) advance(); // skip "###"
    }

    private void skipWhitespace() {
        while (!isAtEnd() && Character.isWhitespace(peek())) {
            if (peek() == '\n') line++;
            advance();
        }
    }

    private char advance() {
        return source.charAt(position++);
    }

    private char peek() {
        return source.charAt(position);
    }

    private String peekAhead(int length) {
        if (position + length > source.length()) return "";
        return source.substring(position, position + length);
    }

    private boolean isAtEnd() {
        return position >= source.length();
    }

    private void addToken(String type, String value) {
        tokens.add(new Token(type, value, line));
    }
