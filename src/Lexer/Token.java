package Lexer;

public class Token {
    private TokenType type; // 类别码
    private int lineNumber; // 所在行号
    private String content; // 具体内容
    private int index;

    public Token(TokenType type, String content,int lineNumber,int index) {
        this.type = type;
        this.lineNumber = lineNumber;
        this.content = content;
        this.index=index;
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
    public int getIndex() {return index;}

    public String toString() {
        return type.toString()+" "+content+"\n";
    }
}
