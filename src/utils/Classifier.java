package utils;

import java.util.HashSet;
import java.util.Set;

public class Classifier{
    private static final Set<Character> singledividers = new HashSet<Character>(){
        {
            add('+');add('-');add('*');add('%');add(';');add(',');
            add('(');add(')');add('[');add(']');add('{');add('}');
        }
    };
    public static boolean isDigit(char c)
    {
        return Character.isDigit(c);
    }
    public static boolean isWordHead(char c)
    {
        return Character.isLetter(c)||c=='_';
    }
    public static boolean isWordBody(char c)
    {
        return Character.isLetter(c)||c=='_'||Character.isDigit(c);
    }
    public static boolean isStringBorder(char c)
    {
        return c=='\"';
    }
    public static boolean isWaitEqual(char c)
    {
        return c=='!'||c=='<'||c=='>'||c=='=';
    }
    public static boolean isWaitAnd(char c)
    {
        return c=='&';
    }
    public static boolean isWaitOr(char c)
    {
        return c=='|';
    }
    public static boolean isDivOrNote(char c)
    {
        return c=='/';
    }
    public static boolean isSingleDivider(char c)
    {
        return singledividers.contains(c);
    }
}
