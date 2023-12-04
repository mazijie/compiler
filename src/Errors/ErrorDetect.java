package Errors;

import Lexer.TokenType;
import Parser.NonTerminators.*;
import SymbolTable.*;
import SymbolTable.FuncType;

public class ErrorDetect {
    //a-Lexer
    public static void checkStr(int line,int index,String str){
        for(int i=1;i<str.length()-1;i++){
            char c=str.charAt(i);
            if(c==92){
                if(i==str.length()-2||str.charAt(i+1)!='n'){
                    ErrorRecord.addError(line, index,ErrorType.a);
                    return;
                }
                continue;
            }
            if(c==32||c==33||(40<=c&&c<=126)) continue;
            if(c==37&&i<str.length()-2&&str.charAt(i+1)=='d') continue;
            ErrorRecord.addError(line, index,ErrorType.a);
            return;
        }
    }
    //废弃的词法错误检测函数
    public static void checkLegalChar(int line,int index,char c){
        if(c==32||c==33||c==37||(40<=c&&c<=126)) return;
        ErrorRecord.addError(line,index,ErrorType.a);
    }

    //b、c、d、e、f、g、h、l、m
    public static int loopCount=0;
    public static void checkCompUnit(CompUnit compUnit){
        for(Decl decl:compUnit.decls){
            checkDecl(decl);
        }
        for(FuncDef funcDef:compUnit.funcDefs){
            checkFuncDef(funcDef);
        }
        checkMainFuncDef(compUnit.mainFuncDef);
    }
    public static void checkDecl(Decl decl){
        if(decl.varDecl==null){
            checkConstDecl(decl.constDecl);
        }else{
            checkVarDecl(decl.varDecl);
        }
    }
    public static void checkConstDecl(ConstDecl constDecl){
        for(ConstDef constDef:constDecl.constDefs){
            checkConstDef(constDef);
        }
    }
    public static void checkConstDef(ConstDef constDef){
        //存符号表、本级检查
        if(constDef.left!=null)
            TableManager.addArraySymbol(constDef.indent,true,constDef.left.size());
        else
            TableManager.addArraySymbol(constDef.indent,true,0);

        //下级检查
        if(constDef.constExps!=null){
            for(ConstExp constExp:constDef.constExps)
                checkConstExp(constExp);
        }
        checkConstInitVal(constDef.constInitVal);
    }
    public static void checkConstInitVal(ConstInitVal constInitVal){
        if(constInitVal.constExp!=null){
            checkConstExp(constInitVal.constExp);
        }else{
            if(constInitVal.constInitVals!=null){
                for(ConstInitVal item:constInitVal.constInitVals){
                    checkConstInitVal(item);
                }
            }
        }
    }
    public static void checkVarDecl(VarDecl varDecl){
        for(VarDef varDef:varDecl.varDefs)
            checkVarDef(varDef);
    }
    public static void checkVarDef(VarDef varDef){
        //本级检查、存符号表
        if(varDef.left!=null)
            TableManager.addArraySymbol(varDef.ident,false,varDef.left.size());
        else
            TableManager.addArraySymbol(varDef.ident,false,0);

        //下级检查
        if(varDef.constExps!=null){
            for(ConstExp constExp:varDef.constExps){
                checkConstExp(constExp);
            }
        }
        if(varDef.initVal!=null){
            checkInitVal(varDef.initVal);
        }
    }
    public static void checkInitVal(InitVal initVal){
        if(initVal.exp!=null){
            checkExp(initVal.exp);
        }else{
            if(initVal.initVals!=null){
                for(InitVal item:initVal.initVals){
                    checkInitVal(item);
                }
            }
        }
    }
    public static void checkFuncDef(FuncDef funcDef){
        FuncType type;
        if(funcDef.funcType.tk.getType()== TokenType.VOIDTK)
            type=FuncType._void;
        else
            type=FuncType._int;
        //本级检查-存符号表
        TableManager.addFuncSymbol(funcDef.ident, type,funcDef.funcFParams);

        //新建符号表
        TableManager.goToNext();
        //下级检查，传返回类型参数便于判断是否需要return
        if(funcDef.funcFParams!=null)
            checkFuncFParams(funcDef.funcFParams);
        checkBlock(funcDef.block,type);
        //退出符号表
        TableManager.backToPre();
    }
    public static void checkMainFuncDef(MainFuncDef mainFuncDef){
        //存符号表
        TableManager.addFuncSymbol(mainFuncDef.maintk, FuncType._int,null);
        //新建符号表
        TableManager.goToNext();
        //下级检查，传返回类型参数便于判断是否需要return
        checkBlock(mainFuncDef.block,FuncType._int);
        //退出符号表
        TableManager.backToPre();
    }
    public static void checkFuncFParams(FuncFParams funcFParams){
        for(FuncFParam funcFParam:funcFParams.funcFParams){
            checkFuncFParam(funcFParam);
        }
    }
    public static void checkFuncFParam(FuncFParam funcFParam){
        //本级检查-存符号表
        if(funcFParam.left==null)
            TableManager.addArraySymbol(funcFParam.ident,false,0);
        else
            TableManager.addArraySymbol(funcFParam.ident,false,funcFParam.left.size());

        //下级检查
        if(funcFParam.constExps!=null)
            for(ConstExp constExp:funcFParam.constExps)
                checkConstExp(constExp);
    }
    //普通block
    public static void checkBlock(Block block){
        if(block.blockItems==null) return;
        TableManager.goToNext();
        for(BlockItem blockItem:block.blockItems){
            checkBlockItem(blockItem);
        }
        TableManager.backToPre();
    }
    //函数block
    public static void checkBlock(Block block,FuncType type) {
        //空壳函数
        if (block.blockItems == null) {
            if (type == FuncType._int) {
                ErrorRecord.addError(block.right.getLine(), block.right.getIndex(), ErrorType.g);
            }
            return;
        }
        //下级检查
        for (BlockItem blockItem : block.blockItems) {
            checkBlockItem(blockItem);
        }
        //下级专项检查-int是否有返回值
        if(type==FuncType._int)
        {
            BlockItem lastBlockItem = block.blockItems.get(block.blockItems.size() - 1);
            checkBlockItem(lastBlockItem,type,block);
        }
        //下级专项检查-void是否无返回值
        else
        {
            for(BlockItem blockItem: block.blockItems){
                checkBlockItem(blockItem,type,block);
            }
        }
    }
    public static void checkBlockItem(BlockItem blockItem){
        if(blockItem.decl!=null)
            checkDecl(blockItem.decl);
        else
            checkStmt(blockItem.stmt);
    }
    //返回值检查使用
    public static void checkBlockItem(BlockItem blockItem,FuncType type,Block block){
        if(blockItem.stmt==null){
            if(type==FuncType._int)
                ErrorRecord.addError(block.right.getLine(), block.right.getIndex(), ErrorType.g);
            return;
        }
        if(blockItem.stmt.type==7&&blockItem.stmt.exp!=null)//有返回值
        {
            if(type==FuncType._void)
                ErrorRecord.addError(blockItem.stmt.returntk.getLine(), blockItem.stmt.returntk.getIndex(),ErrorType.f);
            return;
        }
        //无返回值
        if(type==FuncType._int)
            ErrorRecord.addError(block.right.getLine(), block.right.getIndex(), ErrorType.g);
    }
    public static void checkStmt(Stmt stmt){
        switch(stmt.type){
            //type1:LVal '=' Exp ';'
            case 1:
            //type8:LVal '=' 'getint''('')'';'
            case 8:
                if(TableManager.isConst(stmt.lVal.ident))
                    ErrorRecord.addError(stmt.lVal.ident.getLine(),stmt.lVal.ident.getIndex(),ErrorType.h);
                checkLVal(stmt.lVal);
                if(stmt.exp!=null) checkExp(stmt.exp);
                break;
            //type2:[Exp] ';'
            case 2:
            //type7:'return' [Exp] ';'
            case 7:
                if(stmt.exp!=null)
                    checkExp(stmt.exp);
                break;
            //type3:Block
            case 3:
                checkBlock(stmt.block);
                break;
            //type4:'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            case 4:
                checkCond(stmt.cond);
                checkStmt(stmt.stmts.get(0));
                if(stmt.stmts.size()==2)
                    checkStmt(stmt.stmts.get(1));
                break;
            //type5:'for' '(' [ForStmt] ';' [Cond] ';' [forStmt] ')' Stmt
            case 5:
                if(stmt.forStmts_1!=null) checkForStmt(stmt.forStmts_1);
                if(stmt.cond!=null) checkCond(stmt.cond);
                if(stmt.forStmts_2!=null) checkForStmt(stmt.forStmts_2);
                loopCount++;
                checkStmt(stmt.stmt);
                loopCount--;
                break;
            //type6_1:'break' ';'
            //type6_2:'continue' ';'
            case 6:
                if(loopCount==0)
                    ErrorRecord.addError(stmt.breaktkORcontinuetk.getLine(), stmt.breaktkORcontinuetk.getIndex(),ErrorType.m);
                break;
            //type9:'printf''('FormatString{','Exp}')'';'
            case 9:
                int tmp=0;
                for(int i=0;i<stmt.formatstring.getContent().length()-1;i++)
                {
                    if(stmt.formatstring.getContent().charAt(i)=='%'&&
                            stmt.formatstring.getContent().charAt(i+1)=='d') tmp++;
                }
                if(stmt.exps==null){
                    if(tmp!=0){
                        ErrorRecord.addError(stmt.printftk.getLine(),stmt.printftk.getIndex(),ErrorType.l);
                    }
                    break;
                }
                if(stmt.exps.size()!=tmp){
                    ErrorRecord.addError(stmt.printftk.getLine(),stmt.printftk.getIndex(),ErrorType.l);
                }
                for(Exp exp:stmt.exps){
                    checkExp(exp);
                }
                break;
        }

    }
    public static void checkForStmt(ForStmt forStmt){
        if(TableManager.isConst(forStmt.lVal.ident))
            ErrorRecord.addError(forStmt.lVal.ident.getLine(),forStmt.lVal.ident.getIndex(),ErrorType.h);
        checkLVal(forStmt.lVal);
        checkExp(forStmt.exp);
    }
    public static void checkExp(Exp exp){
        checkAddExp(exp.addExp);
    }
    public static void checkCond(Cond cond){
        checkLOrExp(cond.lOrExp);
    }
    public static void checkLVal(LVal lVal){
        //本级检查-c
        if(!TableManager.isExist(lVal.ident))
        {
            ErrorRecord.addError(lVal.ident.getLine(),lVal.ident.getIndex(),ErrorType.c);
        }
        //下级检查
        if(lVal.exps!=null){
            for(Exp exp:lVal.exps){
                checkExp(exp);
            }
        }
    }
    public static void checkPrimaryExp(PrimaryExp primaryExp){
        if(primaryExp.exp!=null) checkExp(primaryExp.exp);
        else if(primaryExp.lVal!=null) checkLVal(primaryExp.lVal);
    }
    public static void checkUnaryExp(UnaryExp unaryExp){
        if(unaryExp.primaryExp!=null) checkPrimaryExp(unaryExp.primaryExp);
        else if(unaryExp.unaryOp!=null) checkUnaryExp(unaryExp.unaryExp);
        else{
            if(!TableManager.isExist(unaryExp.ident))
            {
                ErrorRecord.addError(unaryExp.ident.getLine(),unaryExp.ident.getIndex(),ErrorType.c);
                return;
            }
            if(unaryExp.funcRParams!=null) checkFuncRParams(unaryExp.funcRParams);
            TableManager.funcParamsCompare(unaryExp.ident,unaryExp.funcRParams);
        }
    }
    public static void checkFuncRParams(FuncRParams funcRParams){
        for(Exp exp:funcRParams.exps){
            checkExp(exp);
        }
    }
    public static void checkMulExp(MulExp mulExp){
        checkUnaryExp(mulExp.unaryExp);
        if(mulExp.mulExp!=null) checkMulExp(mulExp.mulExp);
    }
    public static void checkAddExp(AddExp addExp){
        checkMulExp(addExp.mulExp);
        if(addExp.addExp!=null) checkAddExp(addExp.addExp);
    }
    public static void checkRelExp(RelExp relExp){
        checkAddExp(relExp.addExp);
        if(relExp.relExp!=null) checkRelExp(relExp.relExp);
    }
    public static void checkEqExp(EqExp eqExp){
        checkRelExp(eqExp.relExp);
        if(eqExp.eqExp!=null) checkEqExp(eqExp.eqExp);
    }
    public static void checkLAndExp(LAndExp lAndExp){
        checkEqExp(lAndExp.eqExp);
        if(lAndExp.landExp!=null) checkLAndExp(lAndExp.landExp);
    }
    public static void checkLOrExp(LOrExp lOrExp){
        checkLAndExp(lOrExp.lAndExp);
        if(lOrExp.lOrExp!=null) checkLOrExp(lOrExp.lOrExp);
    }
    public static void checkConstExp(ConstExp constExp){
        checkAddExp(constExp.addExp);
    }
}
