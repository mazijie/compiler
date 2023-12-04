package utils;

import IR.Visitor;

public class Boom {
    public static void boom(){
        Visitor.curFunction=null;
        Visitor.curFunction.giveName();
    }
}
