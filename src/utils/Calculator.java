package utils;

import IR.NewSymbol.NewSymbolManager;
import IR.Value.GlobalVar;
import Lexer.Token;
import Parser.NonTerminators.*;
import Parser.NonTerminators.Number;

import java.util.ArrayList;
import java.util.List;

import static Lexer.TokenType.PLUS;
import static java.lang.Integer.parseInt;

public class Calculator {

    //根据规定，全局变量必须是常量表达式

    public static int calc(Exp exp){
        return calc(exp.addExp);
    }

    public static int calc(ConstExp constExp){
        return calc(constExp.addExp);
    }

    public static int calc(AddExp addexp){
        if(addexp.operand==null) return calc(addexp.mulExp);
        List<MulExp> mulExps=new ArrayList<>();
        List<Token> op=new ArrayList<>();
        addMulExp(addexp,mulExps,op);
        int result=calc(mulExps.get(0));
        for(int i=1;i<mulExps.size();i++){
            switch (op.get(i-1).getType()) {
                case PLUS -> result = result + calc(mulExps.get(i));
                case MINU -> result = result - calc(mulExps.get(i));
            };
        }
        return result;
    }

    public static void addMulExp(AddExp addExp, List<MulExp> mulExps, List<Token> op){
        mulExps.add(addExp.mulExp);
        if(addExp.addExp!=null) {
            op.add(addExp.operand);
            addMulExp(addExp.addExp,mulExps,op);
        }
    }


    public static int calc(MulExp mulexp){
        if(mulexp.op==null) return calc(mulexp.unaryExp);
        List<UnaryExp> unaryExps=new ArrayList<>();
        List<Token> op=new ArrayList<>();
        addUnaryExp(mulexp,unaryExps,op);
        int result=calc(unaryExps.get(0));
        for(int i=1;i<unaryExps.size();i++){
            switch (op.get(i-1).getType()) {
                case MULT -> result = result * calc(unaryExps.get(i));
                case DIV -> result = result / calc(unaryExps.get(i));
                case MOD -> result = result % calc(unaryExps.get(i));
            };
        }
        return result;
    }

    public static void addUnaryExp(MulExp mulExp,List<UnaryExp> unaryExps,List<Token> op){
        unaryExps.add(mulExp.unaryExp);
        if(mulExp.mulExp!=null) {
            op.add(mulExp.op);
            addUnaryExp(mulExp.mulExp,unaryExps,op);
        }
    }

    public static int calc(UnaryExp unaryExp){
        if(unaryExp.primaryExp!=null) return calc(unaryExp.primaryExp);
        else{
            if(unaryExp.unaryOp.token.getType()==PLUS) return calc(unaryExp.unaryExp);
            else return -calc(unaryExp.unaryExp);
        }
    }

    private static int calc(PrimaryExp primaryExp) {
        if(primaryExp.number!=null) return calc(primaryExp.number);
        else if(primaryExp.lVal!=null){
            return ((GlobalVar)NewSymbolManager.searchByName(primaryExp.lVal.ident.getContent())).val;
        }
        else return calc(primaryExp.exp);
    }

    private static int calc(Number number) {
        return parseInt(number.intconst.getContent());
    }

    public static int indexOf2(int num){
        int base = 1;
        for(int i=0;;i++){
            if(base == num) return i;
            if(base > num) return -1;
            base *= 2;
        }
    }
}
