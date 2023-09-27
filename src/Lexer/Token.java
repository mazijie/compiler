package Lexer;

public class Token {
    private TokenType type; // 类别码
    private int lineNumber; // 所在行号
    private String content; // 具体内容

    public Token(TokenType type, String content,int lineNumber) {
        this.type = type;
        this.lineNumber = lineNumber;
        this.content = content;
    }

    public TokenType getType() {
        return type;
    }

    public int getLine() {
        return lineNumber;
    }

    public String getContent() {
        return content;
    }

    public String toString() {
        return type.toString()+" "+content+"\n";
    }
}
