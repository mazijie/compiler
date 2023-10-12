package SymbolTable;

import Errors.ErrorRecord;
import Errors.ErrorType;
import Lexer.Token;
import Parser.NonTerminators.*;

import java.util.ArrayList;
import java.util.List;

public class FuncSymbol extends Symbol{
    public FuncType type;
    public FuncFParams params;
    public List<Integer> dimensions=new ArrayList<Integer>();
    public FuncSymbol(Token token, FuncType type, FuncFParams params){
        super(token,SymbolType.Func);
        this.type=type;
        this.params=params;
        if(params==null) return;
        for(int i=0;i<params.funcFParams.size();i++){
            FuncFParam item=params.funcFParams.get(i);
            if(item.left==null) dimensions.add(0);
            else dimensions.add(item.left.size());
        }
    }
    public void compare(Token token,FuncRParams a){
        //比对参数数量
        if(a==null&&params==null) return;
        if(a==null||params==null){
            ErrorRecord.addError(token.getLine(), token.getIndex(), ErrorType.d);
            return;
        }
        if(a.exps.size()!=params.funcFParams.size())
        {
            ErrorRecord.addError(token.getLine(),token.getIndex(), ErrorType.d);
            return;
        }
        //依次比对参数维数、类型
        for(int i=0;i<a.exps.size();i++){
            int dimension_expected,dimension_actual;

//            Token ident_expected=params.funcFParams.get(i).ident;
//            Symbol symbol_expected=TableManager.getSymbol(ident_expected);
//            dimension_expected=((ArraySymbol)(symbol_expected)).dimension;
            dimension_expected=dimensions.get(i);

            UnaryExp unaryExp_actual=a.exps.get(i).addExp.mulExp.unaryExp;
            //是函数的情况
            if(unaryExp_actual.ident!=null||unaryExp_actual.unaryOp!=null){
                //检查函数返回类型
                FuncSymbol funcSymbol = (FuncSymbol) (TableManager.getSymbol(unaryExp_actual.ident));
                if(funcSymbol.type==FuncType._void){
                    ErrorRecord.addError(token.getLine(), token.getIndex(), ErrorType.e);
                    return;
                }

                //有返回值的函数维度为0
                dimension_actual=0;
                if(dimension_actual!=dimension_expected){
                    ErrorRecord.addError(token.getLine(), token.getIndex(), ErrorType.e);
                    return;
                }
                continue;
            }
            PrimaryExp primaryExp_actual=unaryExp_actual.primaryExp;
            //是常数的情况
            if(primaryExp_actual.number!=null||primaryExp_actual.exp!=null) {
                dimension_actual=0;
                if(dimension_actual!=dimension_expected){
                    ErrorRecord.addError(token.getLine(), token.getIndex(), ErrorType.e);
                    return;
                }
                continue;
            }
            //是变量的情况
            LVal lVal_actual=primaryExp_actual.lVal;
            Token ident_actual=lVal_actual.ident;
            int reduct_demension=0;
            if(lVal_actual.exps!=null){
                reduct_demension=lVal_actual.exps.size();
            }
            Symbol symbol_actual=TableManager.getSymbol(ident_actual);
            if(symbol_actual==null){
//                ErrorRecord.addError(token.getLine(), token.getIndex(), ErrorType.c);
                continue;
            }
            dimension_actual=((ArraySymbol)(symbol_actual)).dimension;
            if(dimension_actual-reduct_demension!=dimension_expected){
                ErrorRecord.addError(token.getLine(), token.getIndex(), ErrorType.e);
                continue;
            }
        }
    }
//    public boolean compareType(FuncFParams a)
//    {
//        for(int i=0;i<a.funcFParams.size();i++){
//            if(params.funcFParams.get(i).bType!=a.funcFParams.get(i).bType)
//                return false;
//        }
//        return true;
//    }
}
