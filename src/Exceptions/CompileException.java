package Exceptions;

import Lexer.Token;
import Lexer.TokenType;

public class CompileException extends Exception {
    private final int line;
    private final TokenType type;
//    private final Token now;
//    private final Token last;
    public CompileException(int line, TokenType expected_type) {
        this.line=line;
        this.type=expected_type;
    }
    public String getMessage() {
        return "Compile Exception("+line+"): Expected "+type.toString();
    }
}
