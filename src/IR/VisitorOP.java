package IR;

import IR.Array.Array;
import IR.Array.ArrayType;
import IR.NewSymbol.NewSymbolManager;
import IR.Type.PointerType;
import IR.Type.ValueType;
import IR.Value.*;
import IR.Value.Instructions.BR;
import Lexer.Token;
import Lexer.TokenType;
import Parser.NonTerminators.Number;
import Parser.NonTerminators.*;
import utils.BRManager;
import utils.Boom;
import utils.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static utils.Calculator.calc;

public class VisitorOP {

    public static BasicBlock curBasicBlock=null;//维护一个当前的基本块
    public static Function curFunction = null;//维护一个当前的函数

    //维护一个module
    public static IRModule module =IRModule.module;
    public static IRModule getModule(){
        return module;
    }
    public static void visitCompUnit(CompUnit compUnit) throws IOException {

        NewSymbolManager.addFunction("getint",ValueType._i32);
        NewSymbolManager.addFunction("putint",ValueType._void);
        NewSymbolManager.addFunction("putch",ValueType._void);
        NewSymbolManager.addFunction("putstr",ValueType._void);
        for(Decl decl:compUnit.decls){
            visitDecl(decl);
        }
        for(FuncDef funcDef:compUnit.funcDefs){
            visitFuncDef(funcDef);
        }
        visitMainFuncDef(compUnit.mainFuncDef);
    }

    private static void visitFuncDef(FuncDef funcDef) {
        Function function;
        if(funcDef.funcType.tk.getType()==TokenType.INTTK)
            function =IRBuildFactory.buildFunction(module,funcDef.ident.getContent(), ValueType._i32);
        else
            function =IRBuildFactory.buildFunction(module,funcDef.ident.getContent(),ValueType._void);
        curFunction=function;
        NewSymbolManager.addFunction(funcDef.ident.getContent(),function);
        NewSymbolManager.goToNext();
        List<Argument> arguments=new ArrayList<>();

        if(funcDef.funcFParams!=null){
            for(FuncFParam param:funcDef.funcFParams.funcFParams){
                //FIXME:这里只考虑int32的情况，没考虑数组
                Argument argument;
                if(param.left==null||param.left.size()==0){
                    argument=IRBuildFactory.buildArgument(curFunction.giveName(), ValueType._i32);
                }else if(param.left.size()==1){//一维数组
//                    argument=IRBuildFactory.buildArgument(curFunction.giveName(),new ArrayType(-100,ValueType._i32));
                    argument=IRBuildFactory.buildArgument(curFunction.giveName(),new PointerType(ValueType._i32));
                }else{//二维数组
//                    argument=IRBuildFactory.buildArgument(curFunction.giveName(),new ArrayType(-100,new ArrayType(calc(param.constExps.get(0)),ValueType._i32)));
                    argument=IRBuildFactory.buildArgument(curFunction.giveName(),new PointerType(new ArrayType(calc(param.constExps.get(0)),ValueType._i32)));
                }
                arguments.add(argument);
                curFunction.addArgument(argument);
            }
        }
        curBasicBlock=IRBuildFactory.buildBasicBlock(function.giveName(),function);
        for(int i=0;i<arguments.size();i++){
            Argument argument=arguments.get(i);
            if(!(argument.type instanceof PointerType)){//不是数组指针
                argument.ValueInFunc=argument;
                VarPointer varPointer=IRBuildFactory.buildVarPointer(curFunction.giveName(), PointerType.i32P);
                NewSymbolManager.addVarPointer(funcDef.funcFParams.funcFParams.get(i).ident.getContent(),varPointer);
                IRBuildFactory.buildALLOCA(curBasicBlock,varPointer);
                IRBuildFactory.buildSTORE(curBasicBlock,argument,varPointer);
            }else{
                //是数组指针
                VarPointer augPointer=IRBuildFactory.buildVarPointer(argument.name,(PointerType)argument.type);
                argument.ValueInFunc=augPointer;
                VarPointer varPointer=IRBuildFactory.buildVarPointer(curFunction.giveName(),new PointerType(argument.type));
                NewSymbolManager.addVarPointer(funcDef.funcFParams.funcFParams.get(i).ident.getContent(),varPointer);
                IRBuildFactory.buildALLOCA(curBasicBlock,varPointer);
                IRBuildFactory.buildSTORE(curBasicBlock,augPointer,varPointer);
            }
        }


        visitBlockofFunc(funcDef.block);
        curBasicBlock=null;
        NewSymbolManager.backToPre();
    }


    //    private static void visitDecl(Decl decl) {
//    }
    public static void visitMainFuncDef(MainFuncDef mainFuncDef) throws IOException {

        Function function =IRBuildFactory.buildFunction(module,"main",ValueType._i32);
        curFunction = function;
        NewSymbolManager.addFunction("main",function);
        NewSymbolManager.goToNext();
        curBasicBlock=IRBuildFactory.buildBasicBlock(function.giveName(),function);
        visitBlockofFunc(mainFuncDef.block);
        curBasicBlock=null;
        NewSymbolManager.backToPre();
    }

    public static void visitBlockofFunc(Block block){
        if(block.blockItems==null){
            IRBuildFactory.buildRET(curBasicBlock);
            return;
        }
        for(int i=0;i<block.blockItems.size()-1;i++){
            visitBlockItem(block.blockItems.get(i));
        }
        visitFuncLastBlockItem(block.blockItems.get(block.blockItems.size()-1));
    }

    private static void visitFuncLastBlockItem(BlockItem blockItem) {
        if(blockItem.stmt!=null){
            if(blockItem.stmt.type==7)//return
            {
                if(blockItem.stmt.exp==null)
                    IRBuildFactory.buildRET(curBasicBlock);
                else
                    IRBuildFactory.buildRET(curBasicBlock,visitExp(blockItem.stmt.exp));
            }
            else{
                visitStmt(blockItem.stmt);
                IRBuildFactory.buildRET(curBasicBlock);
            }
        }
        else{
            IRBuildFactory.buildRET(curBasicBlock);
        }
    }

    public static void visitBlock(Block block){
        if(block.blockItems==null) return;
        NewSymbolManager.goToNext();
        for(int i=0;i<block.blockItems.size();i++){
            visitBlockItem(block.blockItems.get(i));
        }
        NewSymbolManager.backToPre();
    }

    private static void visitBlockItem(BlockItem blockitem) {
        if(blockitem.decl!=null)
            visitDecl(blockitem.decl);
        else {
            visitStmt(blockitem.stmt);
        }
    }

    private static void visitForStmt(ForStmt forStmt){
        Value value=visitExp(forStmt.exp);
        VarPointer target = visitLVal(forStmt.lVal);
        IRBuildFactory.buildSTORE(curBasicBlock,value,target);
//        if(forStmt.lVal.left==null||forStmt.lVal.left.size()==0){
//            VarPointer target= NewSymbolManager.searchByName(forStmt.lVal.ident.getContent());
//            IRBuildFactory.buildSTORE(curBasicBlock,value,target);
//        }
//        else{
//            if(forStmt.lVal.left.size()==1){
//                Array array = NewSymbolManager.searchArray(forStmt.lVal.ident.getContent());
//                if(array!=null){
//                    VarPointer res = IRBuildFactory.buildVarPointer(curFunction.giveName(), PointerType.i32P);
//                    Value offset = IRBuildFactory.buildValue(curFunction.giveName(), ValueType._i32);
//                    IRBuildFactory.buildGETELEMENTPTR(curBasicBlock,res,array,offset);
//                    IRBuildFactory.buildSTORE(curBasicBlock,value,res);
//                }
//                else{
//                    GlobalArray globalArray = NewSymbolManager.searchGlobalArray(forStmt.lVal.ident.getContent());
//                    VarPointer res = IRBuildFactory.buildVarPointer(curFunction.giveName(), PointerType.i32P);
//                    Value offset = IRBuildFactory.buildValue(curFunction.giveName(), ValueType._i32);
//                    IRBuildFactory.buildGETELEMENTPTR(curBasicBlock,res,globalArray,offset);
//                    IRBuildFactory.buildSTORE(curBasicBlock,value,res);
//                }
//            }else{
//
//            }
//        }
    }

    private static void visitStmt(Stmt stmt){
        switch(stmt.type){
            case 1:
                //LVal=Exp;
                VarPointer target = visitLVal(stmt.lVal);
                Value value=visitExp(stmt.exp);
                IRBuildFactory.buildSTORE(curBasicBlock,value,target);
//                {
//                    Value value=visitExp(stmt.exp);
//                    if(stmt.lVal.left==null||stmt.lVal.left.size()==0){
//                        VarPointer target= NewSymbolManager.searchByName(stmt.lVal.ident.getContent());
//                        IRBuildFactory.buildSTORE(curBasicBlock,value,target);
//                    }
//                    else{
//                        if(stmt.lVal.left.size()==1){
//                            Array array = NewSymbolManager.searchArray(stmt.lVal.ident.getContent());
//                            if(array!=null){
//                                VarPointer res = IRBuildFactory.buildVarPointer(curFunction.giveName(), PointerType.i32P);
//                                Value offset = IRBuildFactory.buildValue(curFunction.giveName(), ValueType._i32);
//                                IRBuildFactory.buildGETELEMENTPTR(curBasicBlock,res,array,offset);
//                                IRBuildFactory.buildSTORE(curBasicBlock,value,res);
//                            }
//                            else{
//                                GlobalArray globalArray = NewSymbolManager.searchGlobalArray(stmt.lVal.ident.getContent());
//                                VarPointer res = IRBuildFactory.buildVarPointer(curFunction.giveName(), PointerType.i32P);
//                                Value offset = IRBuildFactory.buildValue(curFunction.giveName(), ValueType._i32);
//                                IRBuildFactory.buildGETELEMENTPTR(curBasicBlock,res,globalArray,offset);
//                                IRBuildFactory.buildSTORE(curBasicBlock,value,res);
//                            }
//                        }else{
//
//                        }
//                    }
//                }
//                Value value=visitExp(stmt.exp);
//                VarPointer target= NewSymbolManager.searchByName(stmt.lVal.ident.getContent());
//                IRBuildFactory.buildSTORE(curBasicBlock,value,target);
                break;
            case 2:
                //[Exp];处理函数
                if(stmt.exp!=null){
                    UnaryExp unaryExp=stmt.exp.addExp.mulExp.unaryExp;
                    if(unaryExp.ident!=null){
                        ValueType type=NewSymbolManager.searchFuncForType(unaryExp.ident.getContent());
                        {
                            //优化部分
                            Function f = NewSymbolManager.searchFunc(unaryExp.ident.getContent());
                            if(f!=null){
                                f.addWhoCallMe(curFunction);
                                curFunction.addWhomICall(f);
                            }
                        }
                        List<Value> args=new ArrayList<>();
                        if(unaryExp.funcRParams!=null)
                            for(Exp exp:unaryExp.funcRParams.exps){
                                Value v=visitExp(exp);
                                args.add(v);
                            }
                        if(type!=ValueType._void)
                        {
                            Value loadto=IRBuildFactory.buildValue(curFunction.giveName(), type);
                            IRBuildFactory.buildCall(curBasicBlock,unaryExp.ident.getContent(),type,args,loadto);
                        }
                        else
                            IRBuildFactory.buildCall(curBasicBlock,unaryExp.ident.getContent(),type,args);
                    }
                }
                break;
            case 3:
                //block
                visitBlock(stmt.block);
                break;
            case 4:
                //if-else，这意味着一个基本块的终结
                {
                    BRManager.addTable();
                    BRManager.refresh_br_true_wait_if();
                    BR IFTO;
                    String b1_name = visitCond(stmt.cond);
                    for(BR item:BRManager.br_true_wait_if()){
                        item.label1=b1_name;
                    }
                    visitStmt(stmt.stmts.get(0));
                    IFTO = IRBuildFactory.buildBR(curBasicBlock,null);

                    if(stmt.elsetk!=null){
                        String b2_name=curFunction.giveName();
                        for(BR item:BRManager.br_false_wait_else()) item.label2=b2_name;
                        curBasicBlock=IRBuildFactory.buildBasicBlock(b2_name,curFunction);
                        visitStmt(stmt.stmts.get(1));
                        String b3_name= curFunction.giveName();
                        IRBuildFactory.buildBR(curBasicBlock,b3_name);
                        IFTO.label_dst=b3_name;
                        curBasicBlock=IRBuildFactory.buildBasicBlock(b3_name,curFunction);
                    }
                    else{
                        String b3_name= curFunction.giveName();
                        for(BR item:BRManager.br_false_wait_else()) item.label2=b3_name;
                        IFTO.label_dst=b3_name;
                        curBasicBlock=IRBuildFactory.buildBasicBlock(b3_name,curFunction);
                    }
                    BRManager.removeTable();
                }

                break;
            case 5:
                //for
                {
                   //处理ForStmt1
                    if(stmt.forStmts_1!=null)
                    {
                        visitForStmt(stmt.forStmts_1);
                    }
                    BRManager.addForTable();
                    //做判断
                    if(stmt.cond!=null)
                    {
                        BRManager.refresh_br_true_wait_if();
                        String b1_name = visitCondOfFor(stmt.cond);
                        for(BR item:BRManager.br_true_wait_if()){
                            item.label1=b1_name;
                        }
                        visitStmt(stmt.stmt);
                        String next=curFunction.giveName();
                        IRBuildFactory.buildBR(curBasicBlock,next);
                        for(BR item:BRManager.br_continue_wait_next()){item.label_dst=next;}
                        curBasicBlock=IRBuildFactory.buildBasicBlock(next, curFunction);
                        if(stmt.forStmts_2!=null) visitForStmt(stmt.forStmts_2);
                        IRBuildFactory.buildBR(curBasicBlock,BRManager.get_label_for_begin());//回到条件判断的开头
                        String b3_name= curFunction.giveName();
                        for(BR item:BRManager.br_false_wait_else()) item.label2=b3_name;
                        for(BR item:BRManager.br_break_wait_end()) item.label_dst=b3_name;
                        curBasicBlock=IRBuildFactory.buildBasicBlock(b3_name,curFunction);
                    }
                    else{
                        String label=curFunction.giveName();
                        IRBuildFactory.buildBR(curBasicBlock,label);
                        curBasicBlock=IRBuildFactory.buildBasicBlock(label,curFunction);
                        BRManager.set_label_for_begin(label);//添加新的label
                        visitStmt(stmt.stmt);
                        String next=curFunction.giveName();
                        IRBuildFactory.buildBR(curBasicBlock,next);
                        for(BR item:BRManager.br_continue_wait_next()){item.label_dst=next;}
                        curBasicBlock=IRBuildFactory.buildBasicBlock(next, curFunction);
                        if(stmt.forStmts_2!=null) visitForStmt(stmt.forStmts_2);
                        IRBuildFactory.buildBR(curBasicBlock,BRManager.get_label_for_begin());//回到条件判断的开头
                        String b3_name= curFunction.giveName();
                        for(BR item:BRManager.br_break_wait_end()) item.label_dst=b3_name;
                        curBasicBlock=IRBuildFactory.buildBasicBlock(b3_name,curFunction);
                    }
                    BRManager.removeForTable();
                }
                break;
            case 6:
                //break或者continue
                if(stmt.breaktkORcontinuetk.getType()==TokenType.BREAKTK){
                    BRManager.add_br_break_wait_end(IRBuildFactory.buildBR(curBasicBlock,null));
                }else{
                    BRManager.add_br_continue_wait_next(IRBuildFactory.buildBR(curBasicBlock,null));
                }
                curBasicBlock=IRBuildFactory.buildBasicBlock(curFunction.giveName(), curFunction);
                break;
            case 7:
                //return
                if(stmt.exp==null)
                    IRBuildFactory.buildRET(curBasicBlock);
                else
                    IRBuildFactory.buildRET(curBasicBlock,visitExp(stmt.exp));
                curBasicBlock=IRBuildFactory.buildBasicBlock(curFunction.giveName(), curFunction);
                break;
            case 8:
                //getint
                Value loadto=IRBuildFactory.buildValue(curFunction.giveName(), ValueType._i32);
                IRBuildFactory.buildCall(curBasicBlock,"getint",ValueType._i32,new ArrayList<>(),loadto);
                VarPointer tar=visitLVal(stmt.lVal);
//                VarPointer tar=NewSymbolManager.searchByName(stmt.lVal.ident.getContent());

                IRBuildFactory.buildSTORE(curBasicBlock,loadto,tar);
                break;
            case 9:
                //printf
                {
                    int count=0;
                    String str=stmt.formatstring.getContent();
                    str=str.substring(1,str.length()-1);
                    for(int i=0;i<str.length();i++){
                        if(str.charAt(i)=='%'&&str.charAt(i+1)=='d'){
                            List<Value> args=new ArrayList<>();
                            Value v = visitExp(stmt.exps.get(count));
                            args.add(v);
                            IRBuildFactory.buildCall(curBasicBlock,"putint",ValueType._void,args);
                            i++;
                            count++;
                        }
                        else if(str.charAt(i)=='\\'&&str.charAt(i+1)=='n'){
                            List<Value> args=new ArrayList<>();
                            args.add(IRBuildFactory.buildConstInt('\n'-0));
                            IRBuildFactory.buildCall(curBasicBlock,"putch",ValueType._void,args);
                            i++;
                        }
                        else{
                            List<Value> args=new ArrayList<>();
                            args.add(IRBuildFactory.buildConstInt(str.charAt(i)-'a'+97));
                            IRBuildFactory.buildCall(curBasicBlock,"putch",ValueType._void,args);
                        }
                    }
                }
                break;
            default:
                Boom.boom();
        }
    }

    //返回值是for基本块的名字
    private static String visitCondOfFor(Cond cond) {
        //清理旧账
        BRManager.refresh_br_true_wait_if();
        BRManager.refresh_br_false_wait_else();
        String label = curFunction.giveName();
        BRManager.set_label_for_begin(label);
        IRBuildFactory.buildBR(curBasicBlock,label);
        curBasicBlock = IRBuildFactory.buildBasicBlock(label, curFunction);//第一个EqExp的基本块
        return visitLOrExp(cond.lOrExp);
    }

    //返回值是if基本块的名字
    private static String visitCond(Cond cond) {
        //清理旧账
        BRManager.refresh_br_true_wait_if();
        BRManager.refresh_br_false_wait_else();
        String label = curFunction.giveName();
        IRBuildFactory.buildBR(curBasicBlock,label);
        curBasicBlock = IRBuildFactory.buildBasicBlock(label, curFunction);//第一个EqExp的基本块
        return visitLOrExp(cond.lOrExp);
    }



    private static String visitLOrExp(LOrExp lOrExp) {

        List<LAndExp> lAndExps = new ArrayList<>();
        addLAndExp(lOrExp,lAndExps);
        for(int i=0;i<lAndExps.size()-1;i++)
        {
            BRManager.refresh_br_false_wait_LAndExp();
            String label = visitLAndExp(lAndExps.get(i),false);
            for(BR item:BRManager.br_false_wait_LAndExp()){
                item.label2=label;
            }
        }
        return visitLAndExp(lAndExps.get(lAndExps.size()-1),true);
    }

    //专门服务于visitLOrExp函数
    private static void addLAndExp(LOrExp lOrExp,List<LAndExp> lAndExps){
        lAndExps.add(lOrExp.lAndExp);
        if(lOrExp.lOrExp!=null) addLAndExp(lOrExp.lOrExp,lAndExps);
    }


    private static String visitLAndExp(LAndExp lAndExp, Boolean isLastLAndExp){
        List<EqExp> eqExps = new ArrayList<>();
        List<BR> brs = new ArrayList<>();
        String nextBlockName="";
        addEqExp(lAndExp,eqExps);
        for(int i=0;i<eqExps.size()-1;i++){
            BRManager.refresh_br_true_wait_EqExp();
            EqExp eqExp = eqExps.get(i);
            Value v = visitEqExp(eqExp);
            Value cond = IRBuildFactory.buildValue(curFunction.giveName(), ValueType._i1);
            IRBuildFactory.buildICMP(curBasicBlock,IRBuildFactory.buildConstInt("0"),v,cond,new Token(TokenType.NEQ,null,0,0));

            //建立br
            BR br=IRBuildFactory.buildBR(curBasicBlock,cond,null,null);
            if(isLastLAndExp) {
                //成功填下一个，失败填else
                BRManager.add_br_true_wait_EqExp(br);
                BRManager.add_br_false_wait_else(br);
            }else{
                //成功填下一个，失败填下一个LAndExp
                BRManager.add_br_true_wait_EqExp(br);
                BRManager.add_br_false_wait_LAndExp(br);
            }
            nextBlockName = curFunction.giveName();
            for(BR item:BRManager.br_true_wait_EqExp()){
                item.label1=nextBlockName;
            }
            curBasicBlock = IRBuildFactory.buildBasicBlock(nextBlockName,curFunction);
        }
        BRManager.refresh_br_true_wait_EqExp();
        EqExp eqExp=eqExps.get(eqExps.size()-1);
        Value v = visitEqExp(eqExp);
        Value cond = IRBuildFactory.buildValue(curFunction.giveName(), ValueType._i1);
        IRBuildFactory.buildICMP(curBasicBlock,IRBuildFactory.buildConstInt("0"),v,cond,new Token(TokenType.NEQ,null,0,0));
        BR br=IRBuildFactory.buildBR(curBasicBlock,cond,null,null);
        if(isLastLAndExp) {
            //成功填if，失败填else
            BRManager.add_br_true_wait_if(br);
            BRManager.add_br_false_wait_else(br);
        }else{
            //成功填if，失败填下一个LAndExp
            BRManager.add_br_true_wait_if(br);
            BRManager.add_br_false_wait_LAndExp(br);
        }
        nextBlockName = curFunction.giveName();
        curBasicBlock = IRBuildFactory.buildBasicBlock(nextBlockName,curFunction);
        return nextBlockName;
    }

    //专门服务于visitLAndExp函数
    private static void addEqExp(LAndExp lAndExp,List<EqExp> eqExps){
        eqExps.add(lAndExp.eqExp);
        if(lAndExp.landExp!=null) addEqExp(lAndExp.landExp,eqExps);
    }

    private static Value visitEqExp(EqExp eqExp){
        //对于EqExp,应返回i32的value用于递归比较
        if(eqExp.eqExp==null)
        {
            return visitRelExp(eqExp.relExp);//直接将计算结果上呈
        }
        //存在比较
        EqExp curEqExp = eqExp;
        Value lastResult = visitRelExp(eqExp.relExp);
        Value curResult;
        while(curEqExp.eqExp!=null){
            Value v = visitRelExp(curEqExp.eqExp.relExp);
            curResult = IRBuildFactory.buildValue(curFunction.giveName(), ValueType._i1);//表示比较的结果
            IRBuildFactory.buildICMP(curBasicBlock,v,lastResult,curResult,curEqExp.operand);
            lastResult = IRBuildFactory.buildValue(curFunction.giveName(), ValueType._i32);
            IRBuildFactory.buildZEXTTO(curBasicBlock,curResult,lastResult);
            curEqExp=curEqExp.eqExp;
        }
        return lastResult;
    }

    private static Value visitRelExp(RelExp relExp){
        //对于RelExp,应返回i32的value用于递归比较
        if(relExp.relExp==null)
        {
            return visitAddExp(relExp.addExp);//直接将计算结果上呈
        }
//        //存在比较
//        Value v1=visitAddExp(relExp.addExp);
//        Value v2=visitRelExp(relExp.relExp);
//        Value result=IRBuildFactory.buildValue(curFunction.giveName(), ValueType._i1);//表示比较的结果
//        IRBuildFactory.buildICMP(curBasicBlock,v1,v2,result,relExp.token);
//        Value response=IRBuildFactory.buildValue(curFunction.giveName(), ValueType._i32);//表示我们的回应
//        IRBuildFactory.buildZEXTTO(curBasicBlock,result,response);
//        return response;
        //存在比较
        RelExp curRelExp = relExp;
        Value lastResult = visitAddExp(relExp.addExp);
        Value curResult;
        while(curRelExp.relExp!=null){
            Value v = visitAddExp(curRelExp.relExp.addExp);
            curResult = IRBuildFactory.buildValue(curFunction.giveName(), ValueType._i1);//表示比较的结果
            IRBuildFactory.buildICMP(curBasicBlock,lastResult,v,curResult,curRelExp.token);
            lastResult = IRBuildFactory.buildValue(curFunction.giveName(), ValueType._i32);
            IRBuildFactory.buildZEXTTO(curBasicBlock,curResult,lastResult);
            curRelExp=curRelExp.relExp;
        }
        return lastResult;
    }


    private static void visitDecl(Decl decl) {
        if(decl.varDecl!=null){
            visitVarDecl(decl.varDecl);
        }
        else{
            visitConstDecl(decl.constDecl);
        }
    }

    private static void visitConstDecl(ConstDecl constDecl) {
        for(ConstDef constDef:constDecl.constDefs){
            visitConstDef(constDef);
        }
    }

    private static void visitConstDef(ConstDef constDef) {

        //构建变量type
        PointerType type;
        if(constDef.left==null||constDef.left.size()==0){//不是数组
            type=PointerType.i32P;
        } else if (constDef.left.size()==1){//一维数组
            int length=calc(constDef.constExps.get(0).addExp);
            type=new PointerType(new ArrayType(length,ValueType._i32));
        }else{//二维数组
            int sub_length=calc(constDef.constExps.get(1).addExp);
            int length=calc(constDef.constExps.get(0).addExp);
            ArrayType subtype=new ArrayType(sub_length,ValueType._i32);
            type = new PointerType(new ArrayType(length,subtype));
        }

        //全局变量
        if(curFunction==null){
            if(constDef.constExps==null||constDef.constExps.size()==0){//全局变量
                GlobalVar globalVar;
                if(constDef.constInitVal==null)
                    globalVar=IRBuildFactory.buildGlobalVar(constDef.indent.getContent(),true,type);
                else if(constDef.constInitVal.constExp!=null){
                    int val=calc(constDef.constInitVal.constExp);
                    globalVar=IRBuildFactory.buildGlobalVar(constDef.indent.getContent(),true,type,val);
                }
                else{
                    Boom.boom();
                    globalVar=null;
                }
                NewSymbolManager.addGlobalVar(constDef.indent.getContent(),globalVar);
                module.addGlobalVar(globalVar);
            }
            else{//全局数组
                GlobalVar globalVar;
                if(constDef.constExps.size()==1){
                    //一维数组
                    List<ConstInteger> vals=new ArrayList<>();
                    for(ConstInitVal constInitVal:constDef.constInitVal.constInitVals){
                        vals.add(IRBuildFactory.buildConstInt(calc(constInitVal.constExp)));
                    }
                    Array array = IRBuildFactory.buildArray("",(ArrayType) type.pointto,vals);
//                    GlobalArray res = IRBuildFactory.buildGlobalArray(constDef.indent.getContent(),
//                            true,type,ValueType._i32,calc(constDef.constExps.get(0)),
//                            vals,12);
                    globalVar = IRBuildFactory.buildGlobalVar(constDef.indent.getContent(),true,type,array);
                    NewSymbolManager.addGlobalVar(constDef.indent.getContent(),globalVar);
                    module.addGlobalVar(globalVar);
                }else{
                    //二维数组
                    List<Array> arrays=new ArrayList<>();
                    for(ConstInitVal constInitVal:constDef.constInitVal.constInitVals){
                        List<ConstInteger> vals=new ArrayList<>();
                        for(ConstInitVal item:constInitVal.constInitVals){
                            vals.add(IRBuildFactory.buildConstInt(calc(item.constExp)));
                        }
                        Array subres = IRBuildFactory.buildArray("", (ArrayType) ((ArrayType) type.pointto).elementType,vals);
                        arrays.add(subres);
                    }
                    Array res = IRBuildFactory.buildArray("",(ArrayType) type.pointto,arrays,2);
                    globalVar = IRBuildFactory.buildGlobalVar(constDef.indent.getContent(), true,type,res);
                    NewSymbolManager.addGlobalVar(constDef.indent.getContent(),globalVar);
                    module.addGlobalVar(globalVar);
                }
            }
        }
        //局部变量
        else{
            //初值
            if(constDef.constInitVal!=null){
                if(constDef.constInitVal.constExp!=null){//不是数组

                    //注册一个局部变量，名字由函数按寄存器号赋予
                    VarPointer varPointer=IRBuildFactory.buildVarPointer(curFunction.giveName(), type);
                    //将变量名字与varpointer关联起来存符号表，之后遇到名字可以直接查询

                    //TODO:优化点-判断当前这个常量能不能算出来，目前均视为不能算
                    IRBuildFactory.buildALLOCA(curBasicBlock,varPointer);

                    Value v1=visitAddExp(constDef.constInitVal.constExp.addExp);
                    IRBuildFactory.buildSTORE(curBasicBlock,v1,varPointer);
                    if(v1 instanceof ConstInteger)
                        NewSymbolManager.addConstVarPointer(constDef.indent.getContent(),((ConstInteger) v1).val);
                    else
                        NewSymbolManager.addVarPointer(constDef.indent.getContent(),varPointer);
                }
                else{
                    //是数组
                    //注册一个局部数组的指针，名字由函数按寄存器号赋予
//                    Array array=IRBuildFactory.buildArray("",(ArrayType) type.pointto);
                    VarPointer varPointer = IRBuildFactory.buildVarPointer(curFunction.giveName(), type);
                    IRBuildFactory.buildALLOCA(curBasicBlock,varPointer);

                    //处理初值问题
                    if(constDef.constInitVal.constInitVals.size()==0||
                            constDef.constInitVal.constInitVals.get(0).constExp!=null){
                        //一维数组
                        for(int i=0;i<calc(constDef.constExps.get(0));i++){
                            VarPointer res = IRBuildFactory.buildVarPointer(curFunction.giveName(), new PointerType(((ArrayType)(type.pointto)).elementType));
                            IRBuildFactory.buildGETELEMENTPTR(curBasicBlock,res,varPointer,IRBuildFactory.buildConstInt(i),0);
                            if(i<constDef.constInitVal.constInitVals.size())
                                IRBuildFactory.buildSTORE(curBasicBlock,visitAddExp(constDef.constInitVal.constInitVals.get(i).constExp.addExp),res);
                            else
                                IRBuildFactory.buildSTORE(curBasicBlock,IRBuildFactory.buildConstInt("0"),res);
                        }
                        NewSymbolManager.addVarPointer(constDef.indent.getContent(),varPointer);
                    }
                    else{
                        //二维数组
                        //对于每个子数组
                        for(int i=0;i<calc(constDef.constExps.get(0));i++){
                            //此部分致力于获得子数组的指针，用于进一步进入子数组赋值
                            PointerType subPointerType = new PointerType(((ArrayType)(type.pointto)).elementType);
                            VarPointer subVarPointer = IRBuildFactory.buildVarPointer(curFunction.giveName(),subPointerType);
                            IRBuildFactory.buildGETELEMENTPTR(curBasicBlock,subVarPointer,varPointer,IRBuildFactory.buildConstInt(i),0);

                            //对于子数组的每个元素，应先获得对应指针，再store赋值
                            for(int j=0;j<calc(constDef.constExps.get(1));j++){
                                PointerType subsubPointerType = new PointerType(((ArrayType)(subPointerType.pointto)).elementType);
                                VarPointer subsubVarPointer = IRBuildFactory.buildVarPointer(curFunction.giveName(),subsubPointerType);
                                IRBuildFactory.buildGETELEMENTPTR(curBasicBlock,subsubVarPointer,subVarPointer,IRBuildFactory.buildConstInt(j),0);

                                //为得到的元素的指针store赋值
                                if(i<constDef.constInitVal.constInitVals.size()&&
                                    j<constDef.constInitVal.constInitVals.get(i).constInitVals.size())
                                    IRBuildFactory.buildSTORE(curBasicBlock,
                                            visitAddExp(constDef.constInitVal.constInitVals.get(i).constInitVals.get(j).constExp.addExp),subsubVarPointer);
                                else
                                    IRBuildFactory.buildSTORE(curBasicBlock,IRBuildFactory.buildConstInt("0"),subsubVarPointer);
                            }
                        }
                        NewSymbolManager.addVarPointer(constDef.indent.getContent(),varPointer);
                    }
                }
            }

        }
    }

    private static void visitVarDecl(VarDecl varDecl) {
            for(VarDef varDef:varDecl.varDefs){
                visitVarDef(varDef);
            }
    }

    private static void visitVarDef(VarDef varDef) {
        PointerType type;
        if(varDef.left==null||varDef.left.size()==0){
            type=PointerType.i32P;
        } else if (varDef.left.size()==1){
            int length=calc(varDef.constExps.get(0).addExp);
            type=new PointerType(new ArrayType(length,ValueType._i32));
        }else{
            int sub_length=calc(varDef.constExps.get(1).addExp);
            int length=calc(varDef.constExps.get(0).addExp);
            ArrayType subtype=new ArrayType(sub_length,ValueType._i32);
            type = new PointerType(new ArrayType(length,subtype));
        }
        //全局变量
        if(curFunction==null){
            if(varDef.left==null){//不是数组
                GlobalVar globalVar;
                if(varDef.initVal==null)
                    globalVar=IRBuildFactory.buildGlobalVar(varDef.ident.getContent(),false,type);
                else{
                    int val=calc(varDef.initVal.exp);
                    globalVar=IRBuildFactory.buildGlobalVar(varDef.ident.getContent(),false,type,val);
                }
                NewSymbolManager.addGlobalVar(varDef.ident.getContent(),globalVar);
                module.addGlobalVar(globalVar);
            }
            else{//数组
                GlobalVar globalVar;
                if(varDef.constExps.size()==1){
                    //一维数组
                    List<ConstInteger> vals=new ArrayList<>();
                    if(varDef.initVal!=null)
                        for(InitVal initVal:varDef.initVal.initVals){
                            vals.add(IRBuildFactory.buildConstInt(calc(initVal.exp)));
                        }
                    Array array = IRBuildFactory.buildArray("",(ArrayType) type.pointto,vals);
//                    GlobalArray res = IRBuildFactory.buildGlobalArray(constDef.indent.getContent(),
//                            true,type,ValueType._i32,calc(constDef.constExps.get(0)),
//                            vals,12);
                    globalVar = IRBuildFactory.buildGlobalVar(varDef.ident.getContent(),false,type,array);
                    NewSymbolManager.addGlobalVar(varDef.ident.getContent(),globalVar);
                    module.addGlobalVar(globalVar);
                }else{
                    //二维数组
                    List<Array> arrays=new ArrayList<>();
                    if(varDef.initVal!=null)
                        for(InitVal initVal:varDef.initVal.initVals){
                            List<ConstInteger> vals=new ArrayList<>();
                            for(InitVal item:initVal.initVals){
                                vals.add(IRBuildFactory.buildConstInt(calc(item.exp)));
                            }
                            Array subres = IRBuildFactory.buildArray("", (ArrayType) ((ArrayType) type.pointto).elementType,vals);
                            arrays.add(subres);
                        }
                    Array res = IRBuildFactory.buildArray("",(ArrayType) type.pointto,arrays,2);
                    globalVar = IRBuildFactory.buildGlobalVar(varDef.ident.getContent(), false,type,res);
                    NewSymbolManager.addGlobalVar(varDef.ident.getContent(),globalVar);
                    module.addGlobalVar(globalVar);
                }
            }
        }
        else{
            //局部变量
            if(varDef.left==null)//不是数组
            {
                //注册一个局部变量，名字由函数按寄存器号赋予
                VarPointer varPointer=IRBuildFactory.buildVarPointer(curFunction.giveName(), type);
                //TODO:优化点-判断当前这个常量能不能算出来，目前均视为不能算
                IRBuildFactory.buildALLOCA(curBasicBlock,varPointer);

                if(varDef.initVal!=null){
                    Value v1=visitAddExp(varDef.initVal.exp.addExp);
                    IRBuildFactory.buildSTORE(curBasicBlock,v1,varPointer);
                }
                //将变量名字与varpointer关联起来存符号表，之后遇到名字可以直接查询
                NewSymbolManager.addVarPointer(varDef.ident.getContent(),varPointer);
            }
            else{//是数组
                //初值
                VarPointer varPointer = IRBuildFactory.buildVarPointer(curFunction.giveName(), type);
                IRBuildFactory.buildALLOCA(curBasicBlock,varPointer);
                if(varDef.initVal!=null){
                    //处理初值问题
                    if(varDef.initVal.initVals.size()==0||
                            varDef.initVal.initVals.get(0).exp!=null){
                        //一维数组
                        for(int i=0;i<varDef.initVal.initVals.size();i++){
                            VarPointer res = IRBuildFactory.buildVarPointer(curFunction.giveName(), new PointerType(((ArrayType)(type.pointto)).elementType));
                            IRBuildFactory.buildGETELEMENTPTR(curBasicBlock,res,varPointer,IRBuildFactory.buildConstInt(i),0);
                            IRBuildFactory.buildSTORE(curBasicBlock,visitAddExp(varDef.initVal.initVals.get(i).exp.addExp),res);
                        }
                        NewSymbolManager.addVarPointer(varDef.ident.getContent(),varPointer);
                    }
                    else{
                        //二维数组
                        //对于每个子数组
                        for(int i=0;i<varDef.initVal.initVals.size();i++){
                            //此部分致力于获得子数组的指针，用于进一步进入子数组赋值
                            PointerType subPointerType = new PointerType(((ArrayType)(type.pointto)).elementType);
                            VarPointer subVarPointer = IRBuildFactory.buildVarPointer(curFunction.giveName(),subPointerType);
                            IRBuildFactory.buildGETELEMENTPTR(curBasicBlock,subVarPointer,varPointer,IRBuildFactory.buildConstInt(i),0);

                            //对于子数组的每个元素，应先获得对应指针，再store赋值
                            for(int j=0;j<varDef.initVal.initVals.get(i).initVals.size();j++){
                                PointerType subsubPointerType = new PointerType(((ArrayType)(subPointerType.pointto)).elementType);
                                VarPointer subsubVarPointer = IRBuildFactory.buildVarPointer(curFunction.giveName(),subsubPointerType);
                                IRBuildFactory.buildGETELEMENTPTR(curBasicBlock,subsubVarPointer,subVarPointer,IRBuildFactory.buildConstInt(j),0);
                                //为得到的元素的指针store赋值
                                IRBuildFactory.buildSTORE(curBasicBlock, visitAddExp(varDef.initVal.initVals.get(i).initVals.get(j).exp.addExp),subsubVarPointer);
                            }
                        }
                    }
                }
                NewSymbolManager.addVarPointer(varDef.ident.getContent(),varPointer);
            }
        }
    }

    public static Value visitExp(Exp exp){
        Value value= visitAddExp(exp.addExp);
        return value;
    }
    public static Value visitAddExp(AddExp addExp){
        if(addExp.operand==null){
            return visitMulExp(addExp.mulExp);
        }
        List<MulExp> mulExps=new ArrayList<>();
        List<Token> op=new ArrayList<>();
        addMulExp(addExp,mulExps,op);
//        int immPart = 0;
//        Value firstVar=null;
//        int index=0;
//        for(index=0;index<mulExps.size();index++){
//            Value v1 = visitMulExp(mulExps.get(index));
//            if(v1 instanceof ConstInteger){
//                if(index==0){
//                    immPart+=((ConstInteger) v1).val;
//                }else{
//                    switch (op.get(index-1).getType()) {
//                        case PLUS -> immPart+=((ConstInteger) v1).val;
//                        case MINU -> immPart-=((ConstInteger) v1).val;
//                    }
//                }
//            }else{
//                firstVar = v1;
//                break;
//            }
//        }
//        if(firstVar==null){
//            return IRBuildFactory.buildConstInt(immPart);
//        }else{
//            if(index>0&&op.get(index-1).getType()==TokenType.MINU){
//                Value realFirst=IRBuildFactory.buildValue(curFunction.giveName(),ValueType._i32);
//                IRBuildFactory.buildSUBInstruction(curBasicBlock,IRBuildFactory.buildConstInt(0),firstVar,realFirst);
//                firstVar=realFirst;
//            }
//            for(index=index+1;index<mulExps.size();index++){
//                Value v1 = visitMulExp(mulExps.get(index));
//                if(v1 instanceof ConstInteger){
//                    switch (op.get(index-1).getType()) {
//                        case PLUS -> immPart+=((ConstInteger) v1).val;
//                        case MINU -> immPart-=((ConstInteger) v1).val;
//                    }
//                }else{
//                    Value new_var=IRBuildFactory.buildValue(curFunction.giveName(),ValueType._i32);
//                    switch (op.get(index-1).getType()) {
//                        case PLUS -> IRBuildFactory.buildADDInstruction(curBasicBlock, firstVar, v1, new_var);
//                        case MINU -> IRBuildFactory.buildSUBInstruction(curBasicBlock, firstVar, v1, new_var);
//                    }
//                    firstVar=new_var;
//                }
//            }
//        }
//        if(immPart!=0){
//            Value res=IRBuildFactory.buildValue(curFunction.giveName(),ValueType._i32);
//            IRBuildFactory.buildADDInstruction(curBasicBlock,firstVar,IRBuildFactory.buildConstInt(immPart),res);
//            return res;
//        }else return firstVar;
        Value last_var=visitMulExp(mulExps.get(0));
        for(int i=1;i<mulExps.size();i++){
            Value v1=visitMulExp(mulExps.get(i));
            if(v1 instanceof ConstInteger&&last_var instanceof ConstInteger)
            {
                switch (op.get(i-1).getType()) {
                    case PLUS -> last_var=IRBuildFactory.buildConstInt(((ConstInteger) v1).val+((ConstInteger) last_var).val);
                    case MINU -> last_var=IRBuildFactory.buildConstInt(((ConstInteger) last_var).val-((ConstInteger) v1).val);
                }
            }else{
                Value new_var=IRBuildFactory.buildValue(curFunction.giveName(),ValueType._i32);
                switch (op.get(i-1).getType()) {
                    case PLUS -> IRBuildFactory.buildADDInstruction(curBasicBlock, last_var, v1, new_var);
                    case MINU -> IRBuildFactory.buildSUBInstruction(curBasicBlock, last_var, v1, new_var);
                }
                last_var=new_var;
            }
        }

        return last_var;

    }

    public static void addMulExp(AddExp addExp,List<MulExp> mulExps,List<Token> op){
        mulExps.add(addExp.mulExp);
        if(addExp.addExp!=null) {
            op.add(addExp.operand);
            addMulExp(addExp.addExp,mulExps,op);
        }
    }

    private static Value visitMulExp(MulExp mulExp) {
        if(mulExp.op==null){
            return visitUnaryExp(mulExp.unaryExp);
        }
        List<UnaryExp> unaryExps=new ArrayList<>();
        List<Token> op=new ArrayList<>();
        addUnaryExp(mulExp,unaryExps,op);
        Value last_var=visitUnaryExp(unaryExps.get(0));
        for(int i=1;i<unaryExps.size();i++){
            Value v1=visitUnaryExp(unaryExps.get(i));
            if(v1 instanceof ConstInteger&&last_var instanceof ConstInteger)
            {
                switch (op.get(i-1).getType()) {
                    case MULT -> last_var=IRBuildFactory.buildConstInt(((ConstInteger) v1).val*((ConstInteger) last_var).val);
                    case DIV -> last_var=IRBuildFactory.buildConstInt(((ConstInteger) last_var).val/((ConstInteger) v1).val);
                    case MOD -> last_var=IRBuildFactory.buildConstInt(((ConstInteger) last_var).val%((ConstInteger) v1).val);
                }
            }else{
                Value new_var=IRBuildFactory.buildValue(curFunction.giveName(),ValueType._i32);
                switch (op.get(i-1).getType()) {
                    case MULT -> IRBuildFactory.buildMULInstruction(curBasicBlock, last_var, v1, new_var);
                    case DIV -> IRBuildFactory.buildSDIVInstruction(curBasicBlock, last_var, v1, new_var);
                    case MOD -> IRBuildFactory.buildSREMInstruction(curBasicBlock, last_var, v1, new_var);
                }
                last_var=new_var;
            }
        }

        return last_var;
    }

    public static void addUnaryExp(MulExp mulExp,List<UnaryExp> unaryExps,List<Token> op){
        unaryExps.add(mulExp.unaryExp);
        if(mulExp.mulExp!=null) {
            op.add(mulExp.op);
            addUnaryExp(mulExp.mulExp,unaryExps,op);
        }
    }

    private static Value visitUnaryExp(UnaryExp unaryExp){
        if(unaryExp.primaryExp!=null){
            return visitPrimaryExp(unaryExp.primaryExp);
        }
        else if(unaryExp.unaryOp!=null){
            if(unaryExp.unaryOp.token.getType()==TokenType.PLUS)
                //+
                return visitUnaryExp(unaryExp.unaryExp);
            Value v2=visitUnaryExp(unaryExp.unaryExp);
            if(unaryExp.unaryOp.token.getType()==TokenType.MINU) {
                //-
                Value res = IRBuildFactory.buildValue(curFunction.giveName(), ValueType._i32);
                //如果是常数直接返回其相反数
                if(v2 instanceof ConstInteger) return new ConstInteger(-1*((ConstInteger) v2).val);
                IRBuildFactory.buildSUBInstruction(curBasicBlock, IRBuildFactory.buildConstInt("0"), v2, res);
                return res;
            }else{
                //!
                Value middleres = IRBuildFactory.buildValue(curFunction.giveName(), ValueType._i1);
                Value res = IRBuildFactory.buildValue(curFunction.giveName(), ValueType._i32);
                IRBuildFactory.buildICMP(curBasicBlock,v2,IRBuildFactory.buildConstInt("0"),middleres,new Token(TokenType.EQL,null,0,0));
                IRBuildFactory.buildZEXTTO(curBasicBlock,middleres,res);
                return res;
            }

        }
        else{
            //TODO:调用函数,形成Call指令

            ValueType returnType=NewSymbolManager.searchFuncForType(unaryExp.ident.getContent());
            {
                //优化部分
                Function f = NewSymbolManager.searchFunc(unaryExp.ident.getContent());
                if(f!=null){
                    f.addWhoCallMe(curFunction);
                    curFunction.addWhomICall(f);
                }
            }
            List<Value> args = new ArrayList<Value>();
            if(unaryExp.funcRParams!=null)
                for(Exp exp:unaryExp.funcRParams.exps){
                    args.add(visitExp(exp));
                }
            Value res=IRBuildFactory.buildValue(curFunction.giveName(),returnType);
            IRBuildFactory.buildCall(curBasicBlock,unaryExp.ident.getContent(),returnType,args,res);
            return res;
        }
    }

    private static Value visitPrimaryExp(PrimaryExp primaryExp){
        if(primaryExp.exp!=null){
            return visitExp(primaryExp.exp);
        }else if(primaryExp.lVal!=null){
            {
                //var为指向返回值的变量//FIXME
                //FIXME
                if(NewSymbolManager.isConstVarPointer(primaryExp.lVal.ident.getContent())){
                    return IRBuildFactory.buildConstInt(NewSymbolManager.getConstVarPointer(primaryExp.lVal.ident.getContent()));
                }
                VarPointer resPointer = visitLVal(primaryExp.lVal);
                assert resPointer != null;
                Value res;
                //有数组的拆数组，没数组的拆指针
                if(resPointer.type.pointto instanceof ArrayType){
                    res=IRBuildFactory.buildVarPointer(curFunction.giveName(),new PointerType(((ArrayType) resPointer.type.pointto).elementType));
                    IRBuildFactory.buildGETELEMENTPTR(curBasicBlock,(VarPointer) res,resPointer,IRBuildFactory.buildConstInt(0),0);
                }else{
                    res = IRBuildFactory.buildValue(curFunction.giveName(),resPointer.type.pointto);
                    IRBuildFactory.buildLOAD(curBasicBlock,res,resPointer);
                }
                return res;
            }
        }else{
            return visitNumber(primaryExp.number);
        }
    }

    private static Value visitNumber(Number number) {
        return IRBuildFactory.buildConstInt(number.intconst.getContent());
    }

    public static void printResult() throws IOException {
        IOUtils.write("declare i32 @getint()\n");
        IOUtils.write("declare void @putint(i32)\n");
        IOUtils.write("declare void @putch(i32)\n");
        IOUtils.write("declare void @putstr(i8*)\n\n");
        module.print();
    }

    public static VarPointer visitLVal(LVal lVal){

        VarPointer varPointer = NewSymbolManager.searchByName(lVal.ident.getContent());
        if(varPointer==null) System.out.println(lVal.ident.getContent());
        if(lVal.left==null||lVal.left.size()==0){
            return varPointer;
        }
        else if(lVal.left.size()==1){
            //是数组或者指针，如果是数组，type为0,如果是指针，type为1。
            if(varPointer.type.pointto instanceof ArrayType arrayType){
                //是数组
                Value offset = visitExp(lVal.exps.get(0));
                VarPointer res = IRBuildFactory.buildVarPointer(curFunction.giveName(),new PointerType(arrayType.elementType));
                IRBuildFactory.buildGETELEMENTPTR(curBasicBlock,res,varPointer,offset,0);
                return res;
            }
            else if(varPointer.type.pointto instanceof PointerType pointerType){
                //是指针
                VarPointer pointer = IRBuildFactory.buildVarPointer(curFunction.giveName(),(PointerType) (varPointer.type.pointto));
                IRBuildFactory.buildLOAD(curBasicBlock,pointer,varPointer);
                Value offset = visitExp(lVal.exps.get(0));
                VarPointer res = IRBuildFactory.buildVarPointer(curFunction.giveName(),pointerType);
                IRBuildFactory.buildGETELEMENTPTR(curBasicBlock,res,pointer,offset,1);
                return res;
            }
        }
        else{
            //是数组的指针或者数组的数组
            VarPointer subpointer;
            if(varPointer.type.pointto instanceof ArrayType arrayType){
                //是数组
                 Value offset_1 = visitExp(lVal.exps.get(0));
                Array array = IRBuildFactory.buildArray("curFunction.giveName()",arrayType);
                subpointer = IRBuildFactory.buildVarPointer(curFunction.giveName(),new PointerType(arrayType.elementType));
                IRBuildFactory.buildGETELEMENTPTR(curBasicBlock,subpointer,varPointer,offset_1,0);
            }
            else if(varPointer.type.pointto instanceof PointerType pointerType){
                //是指针
                Value offset_1 = visitExp(lVal.exps.get(0));
                VarPointer pointer = IRBuildFactory.buildVarPointer(curFunction.giveName(),(PointerType) (varPointer.type.pointto));
                IRBuildFactory.buildLOAD(curBasicBlock,pointer,varPointer);
                subpointer = IRBuildFactory.buildVarPointer(curFunction.giveName(),pointerType);
                IRBuildFactory.buildGETELEMENTPTR(curBasicBlock,subpointer,pointer,offset_1,1);
            }
            else return null;
            Value offset_2 = visitExp(lVal.exps.get(1));
            VarPointer res = IRBuildFactory.buildVarPointer(curFunction.giveName(),new PointerType(((ArrayType)subpointer.type.pointto).elementType));
            IRBuildFactory.buildGETELEMENTPTR(curBasicBlock,res,subpointer,offset_2,0);
            return res;
        }
        return null;
    }
}
