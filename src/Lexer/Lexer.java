package Lexer;

import Errors.ErrorDetect;
import Errors.ErrorRecord;
import Errors.ErrorType;
import config.Config;
import utils.Classifier;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Lexer {

    private static Map<String, TokenType> keywords = new HashMap<String, TokenType>() {{
        put("main", TokenType.MAINTK);
        put("const", TokenType.CONSTTK);
        put("int", TokenType.INTTK);
        put("break", TokenType.BREAKTK);
        put("continue", TokenType.CONTINUETK);
        put("if", TokenType.IFTK);
        put("else", TokenType.ELSETK);
        put("!",TokenType.NOT);
        put("&&", TokenType.AND);
        put("||", TokenType.OR);
        put("for", TokenType.FORTK);
        put("getint", TokenType.GETINTTK);
        put("printf", TokenType.PRINTFTK);
        put("return",TokenType.RETURNTK);
        put("+",TokenType.PLUS);
        put("-",TokenType.MINU);
        put("void", TokenType.VOIDTK);
        put("*", TokenType.MULT);
        put("/",TokenType.DIV);
        put("%",TokenType.MOD);
        put("<",TokenType.LSS);
        put("<=",TokenType.LEQ);
        put(">",TokenType.GRE);
        put(">=",TokenType.GEQ);
        put("==",TokenType.EQL);
        put("!=",TokenType.NEQ);
        put("=",TokenType.ASSIGN);
        put(";",TokenType.SEMICN);
        put(",",TokenType.COMMA);
        put("(",TokenType.LPARENT);
        put(")",TokenType.RPARENT);
        put("[",TokenType.LBRACK);
        put("]",TokenType.RBRACK);
        put("{",TokenType.LBRACE);
        put("}",TokenType.RBRACE);
    }};

    private static List<Token> tokens = new ArrayList<>();

    public static List<Token> getTokens() {
        return tokens;
    }

    public static void analyze(String content) throws Exception{
        int len=content.length();
        int pos=0;
        int lineNumber=1;
        for(pos=0;pos<len;)
        {
                if(content.charAt(pos)=='\n') lineNumber++;
                if(Classifier.isDigit(content.charAt(pos))){
                    String tmp="";
                    while(pos<len&& Classifier.isDigit(content.charAt(pos))){
                        tmp += content.charAt(pos);
                        pos++;
                    }
                    tokens.add(new Token(keywords.getOrDefault(tmp, TokenType.INTCON), tmp,lineNumber,tokens.size()));
                }
                else if(Classifier.isWordHead(content.charAt(pos))){
                    String tmp="";
                    while(pos<len&& Classifier.isWordBody(content.charAt(pos))){
                        tmp += content.charAt(pos);
                        pos++;
                    }
                    tokens.add(new Token(keywords.getOrDefault(tmp, TokenType.IDENFR), tmp,lineNumber,tokens.size()));
                }
                else if(Classifier.isStringBorder(content.charAt(pos))){
                    String tmp="\"";
                    pos++;
                    while(pos<len&& !Classifier.isStringBorder(content.charAt(pos))){
                        tmp+=content.charAt(pos);
                        pos++;
                    }
                    tmp+="\"";
                    ErrorDetect.checkStr(lineNumber,tokens.size(),tmp);
                    tokens.add(new Token(keywords.getOrDefault(tmp, TokenType.STRCON), tmp,lineNumber,tokens.size()));
                    pos++;
                }
//                else if(Classifier.isStringBorder(content.charAt(pos))){
//                    String tmp="\"";
//                    pos++;
//                    while(pos<len&& !Classifier.isStringBorder(content.charAt(pos))){
//                        ErrorDetect.checkLegalChar(lineNumber,tokens.size(),content.charAt(pos));
//                        if(content.charAt(pos)==92&&content.charAt(pos+1)!='n')
//                        {
//                            ErrorRecord.addError(lineNumber,tokens.size(), ErrorType.a);
//                        }
//                        if(content.charAt(pos)==37&&content.charAt(pos+1)!='d')
//                        {
//                            ErrorRecord.addError(lineNumber,tokens.size(), ErrorType.a);
//                        }
//                        tmp+=content.charAt(pos);
//                        pos++;
//                    }
//                    tmp+="\"";
//                    tokens.add(new Token(keywords.getOrDefault(tmp, TokenType.STRCON), tmp,lineNumber,tokens.size()));
//                    pos++;
//                }
                else if(Classifier.isWaitEqual(content.charAt(pos))){
                    if(pos+1<len&&content.charAt(pos+1)=='=')
                    {
                        String tmp="";
                        tmp+=content.charAt(pos);
                        tmp+='=';
                        tokens.add(new Token(keywords.getOrDefault(tmp, TokenType.IDENFR), tmp,lineNumber,tokens.size()));
                        pos+=2;
                    }
                    else {
                        String tmp="";
                        tmp+=content.charAt(pos);
                        tokens.add(new Token(keywords.getOrDefault(tmp, TokenType.IDENFR), tmp,lineNumber,tokens.size()));
                        pos++;
                    }
                }
                else if(Classifier.isWaitAnd(content.charAt(pos))){
                    if(pos+1<len&&content.charAt(pos+1)=='&')
                    {
                        String tmp="";
                        tmp+=content.charAt(pos);
                        tmp+='&';
                        tokens.add(new Token(keywords.getOrDefault(tmp, TokenType.IDENFR), tmp,lineNumber,tokens.size()));
                        pos+=2;
                    }
                    else {
                        throw new Exception();
                    }
                }
                else if(Classifier.isWaitOr(content.charAt(pos))){
                    if(pos+1<len&&content.charAt(pos+1)=='|')
                    {
                        String tmp="";
                        tmp+=content.charAt(pos);
                        tmp+='|';
                        tokens.add(new Token(keywords.getOrDefault(tmp, TokenType.IDENFR), tmp,lineNumber,tokens.size()));
                        pos+=2;
                    }
                    else {
                        throw new Exception();
                    }
                }
                else if(Classifier.isDivOrNote(content.charAt(pos))){
                    if(pos+1<len&&content.charAt(pos+1)=='/')
                    {
                        while(pos<len&&content.charAt(pos)!='\n'){
                            pos++;
                        }
                    }
                    else if(pos+1<len&&content.charAt(pos+1)=='*')
                    {
                        while(pos<len&&(!(content.charAt(pos)=='*'&&content.charAt(pos+1)=='/')))
                        {
                            pos++;
                            if(content.charAt(pos)=='\n') lineNumber++;
                        }
                        pos+=2;
                    }
                    else {
                        String tmp="/";
                        tokens.add(new Token(keywords.getOrDefault(tmp, TokenType.IDENFR), tmp,lineNumber,tokens.size()));
                        pos++;
                    }
                }
                else if(Classifier.isSingleDivider(content.charAt(pos))){
                    String tmp="";
                    tmp+=content.charAt(pos);
                    tokens.add(new Token(keywords.getOrDefault(tmp, TokenType.IDENFR), tmp,lineNumber,tokens.size()));
                    pos++;
                }
                else pos++;
        }
    }


    public static void printLexAns() throws IOException
    {
        FileWriter fileWriter = new FileWriter(Config.fileOutPath);
        for(Token token:tokens)
            fileWriter.write(token.toString());
        fileWriter.close();
    }

}
