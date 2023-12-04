package Parser;
import Errors.ErrorRecord;
import Errors.ErrorType;
import Exceptions.CompileException;
import Lexer.Token;
import Lexer.TokenType;
import Parser.NonTerminators.*;
import Parser.NonTerminators.Number;
import utils.Boom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private static List<Token> tokens;
    public static CompUnit compUnit;
    private static int pos=0;

    public static Map<NTTypes,String> NTTypesToString=new HashMap<>(){{
        put(NTTypes.AddExp,"<AddExp>\n");
        put(NTTypes.Block,"<Block>\n");
        put(NTTypes.BlockItem,"<BlockItem>\n");
        put(NTTypes.BType,"<BType>\n");
        put(NTTypes.CompUnit,"<CompUnit>\n");
        put(NTTypes.Cond,"<Cond>\n");
        put(NTTypes.ConstDecl,"<ConstDecl>\n");
        put(NTTypes.ConstDef,"<ConstDef>\n");
        put(NTTypes.ConstInitVal,"<ConstInitVal>\n");
        put(NTTypes.ConstExp,"<ConstExp>\n");
        put(NTTypes.Decl,"<Decl>\n");
        put(NTTypes.EqExp,"<EqExp>\n");
        put(NTTypes.Exp,"<Exp>\n");
        put(NTTypes.ForStmt,"<ForStmt>\n");
        put(NTTypes.FuncDef,"<FuncDef>\n");
        put(NTTypes.FuncFParam,"<FuncFParam>\n");
        put(NTTypes.FuncFParams,"<FuncFParams>\n");
        put(NTTypes.FuncRParams,"<FuncRParams>\n");
        put(NTTypes.FuncType,"<FuncType>\n");
        put(NTTypes.InitVal,"<InitVal>\n");
        put(NTTypes.LAndExp,"<LAndExp>\n");
        put(NTTypes.LOrExp,"<LOrExp>\n");
        put(NTTypes.LVal,"<LVal>\n");
        put(NTTypes.MainFuncDef,"<MainFuncDef>\n");
        put(NTTypes.MulExp,"<MulExp>\n");
        put(NTTypes.Number,"<Number>\n");
        put(NTTypes.PrimaryExp,"<PrimaryExp>\n");
        put(NTTypes.RelExp,"<RelExp>\n");
        put(NTTypes.Stmt,"<Stmt>\n");
        put(NTTypes.UnaryExp,"<UnaryExp>\n");
        put(NTTypes.UnaryOp,"<UnaryOp>\n");
        put(NTTypes.VarDecl,"<VarDecl>\n");
        put(NTTypes.VarDef,"<VarDef>\n");
    }};
    public static void buildTree(List<Token> _tokens) throws CompileException {
        tokens=_tokens;
        compUnit= buildCompUnit();
    }

    public static void printResult() throws IOException {
        compUnit.print();
    }

    //根据文法，以下为公用的拆解函数
    private static CompUnit buildCompUnit() throws CompileException {
        // CompUnit -> {Decl} {FuncDef} MainFuncDef
        //Decl与FUNCDEF的区别：有无参数括号
        //FUNC与Main的区别：是否为maintk
        List<Decl> decls = new ArrayList<>();
        List<FuncDef> funcDefs = new ArrayList<>();
        MainFuncDef mainFuncDef;
        //处理decl
        while (tokens.get(pos+1).getType() != TokenType.MAINTK && tokens.get(pos+2).getType() != TokenType.LPARENT) {
            Decl decl = buildDecl();
            decls.add(decl);
        }
        //处理funcdef
        while (tokens.get(pos+1).getType() != TokenType.MAINTK) {
            FuncDef funcDef = buildFuncDef();
            funcDefs.add(funcDef);
        }
        //处理main函数
        mainFuncDef = buildMainFuncDef();
        return new CompUnit(decls, funcDefs, mainFuncDef);
    }

    private static Decl buildDecl() throws CompileException {
        if(tokens.get(pos).getType()==TokenType.CONSTTK){
            ConstDecl constDecl=buildConstDecl();
            return new Decl(constDecl,null);
        }else{
            VarDecl varDecl = buildVarDecl();
            return new Decl(null,varDecl);
        }
    }

    private static ConstDecl buildConstDecl() throws CompileException {
        Token consttk=tokens.get(pos);
        compare(TokenType.CONSTTK);
        BType bType = buildBType();
        List<ConstDef> constDefs = new ArrayList<>();
        List<Token> commas = new ArrayList<>();
        constDefs.add(buildConstDef());
        while(tokens.get(pos).getType()==TokenType.COMMA){
            commas.add(tokens.get(pos));
            compare(TokenType.COMMA);
            constDefs.add(buildConstDef());
        }
        Token semicn=tokens.get(pos);
        compare(TokenType.SEMICN);
//        if(commas.size()==0) return new ConstDecl(consttk,bType,constDefs,null,semicn);
        return new ConstDecl(consttk,bType,constDefs,commas,semicn);
    }

    private static BType buildBType() throws CompileException {
        Token inttk=tokens.get(pos);
        compare(TokenType.INTTK);
        return new BType(inttk);
    }

    private static ConstDef buildConstDef() throws CompileException{
        Token ident=tokens.get(pos);
        compare(TokenType.IDENFR);
        List<Token> lefts=new ArrayList<>();
        List<Token> rights=new ArrayList<>();
        List<ConstExp> constExps=new ArrayList<>();
        while(tokens.get(pos).getType()==TokenType.LBRACK){
            lefts.add(tokens.get(pos));
            compare(TokenType.LBRACK);
            constExps.add(buildConstExp());
            rights.add(tokens.get(pos));
            compare(TokenType.RBRACK);
        }
        Token assign=tokens.get(pos);
        compare(TokenType.ASSIGN);
        ConstInitVal constInitVal = buildConstInitVal();
        if(lefts.size()==0)
        {
            lefts=null;
            rights=null;
            constExps=null;
        }
        return new ConstDef(ident,lefts,constExps,rights,assign,constInitVal);
    }

    private static ConstInitVal buildConstInitVal() throws CompileException{
        if(tokens.get(pos).getType()!=TokenType.LBRACE){
            return new ConstInitVal(buildConstExp(),null,null,null,null);
        }else{
            Token left=tokens.get(pos);
            compare(TokenType.LBRACE);
            List<ConstInitVal> constInitVals=new ArrayList<>();
            List<Token> commas=new ArrayList<>();
            if(tokens.get(pos).getType()!=TokenType.RBRACE)
            {
                constInitVals.add(buildConstInitVal());
                while(tokens.get(pos).getType()==TokenType.COMMA){
                    commas.add(tokens.get(pos));
                    compare(TokenType.COMMA);
                    constInitVals.add(buildConstInitVal());
                }
                Token right=tokens.get(pos);
                compare(TokenType.RBRACE);
                return new ConstInitVal(null,left,right,constInitVals,commas);
            }
            else{
                Token right=tokens.get(pos);
                compare(TokenType.RBRACE);
                return new ConstInitVal(null,left,right,null,null);
            }
        }
    }

    private static VarDecl buildVarDecl() throws CompileException{
        BType bType = buildBType();
        List<VarDef>varDefs=new ArrayList<>();
        List<Token> commas = new ArrayList<>();
        varDefs.add(buildVarDef());
        while(tokens.get(pos).getType()==TokenType.COMMA){
            commas.add(tokens.get(pos));
            compare(TokenType.COMMA);
            varDefs.add(buildVarDef());
        }
        Token semicn=tokens.get(pos);
        compare(TokenType.SEMICN);
        return new VarDecl(bType,varDefs,commas,semicn);
    }

    private static VarDef buildVarDef() throws CompileException{
        Token ident=tokens.get(pos);
        compare(TokenType.IDENFR);
        List<Token> lefts=new ArrayList<>();
        List<Token> rights=new ArrayList<>();
        List<ConstExp> constExps=new ArrayList<>();
        while(tokens.get(pos).getType()==TokenType.LBRACK){
            lefts.add(tokens.get(pos));
            compare(TokenType.LBRACK);
            constExps.add(buildConstExp());
            rights.add(tokens.get(pos));
            compare(TokenType.RBRACK);
        }
        if(constExps.size()==0)
        {
            lefts=null;
            rights=null;
            constExps=null;
        }
        if(tokens.get(pos).getType()==TokenType.ASSIGN){
            Token equal=tokens.get(pos);
            compare(TokenType.ASSIGN);
            InitVal initVal = buildInitVal();
            return new VarDef(ident,lefts,constExps,rights,equal,initVal);
        }
        return new VarDef(ident,lefts,constExps,rights,null,null);
    }

    private static InitVal buildInitVal() throws CompileException {
        if(tokens.get(pos).getType()!=TokenType.LBRACE){
            Exp exp=buildExp();
            return new InitVal(exp);
        }else{
            Token left=tokens.get(pos);
            compare(TokenType.LBRACE);
            List<InitVal> initVals=new ArrayList<>();
            List<Token> commas=new ArrayList<>();
            initVals.add(buildInitVal());
            while(tokens.get(pos).getType()==TokenType.COMMA){
                commas.add(tokens.get(pos));
                compare(TokenType.COMMA);
                initVals.add(buildInitVal());
            }
            Token right=tokens.get(pos);
            compare(TokenType.RBRACE);
            if(commas.size()==0) commas=null;
            return new InitVal(left,initVals,right,commas);
        }
    }

    private static FuncDef buildFuncDef() throws CompileException {
        FuncType funcType=buildFuncType();
        Token ident=tokens.get(pos);
        compare(TokenType.IDENFR);
        Token left=tokens.get(pos);
        compare(TokenType.LPARENT);
        FuncFParams funcFParams=null;
        if(tokens.get(pos).getType()==TokenType.INTTK){
            funcFParams=buildFuncFParams();
        }
        Token right=tokens.get(pos);
        compare(TokenType.RPARENT);
        Block block=buildBlock();
        return new FuncDef(funcType,ident,left,funcFParams,right,block);
    }

    private static MainFuncDef buildMainFuncDef() throws CompileException{
        Token inttk = tokens.get(pos);
        compare(TokenType.INTTK);
        Token maintk = tokens.get(pos);
        compare(TokenType.MAINTK);
        Token left =tokens.get(pos);
        compare(TokenType.LPARENT);
        Token right=tokens.get(pos);
        compare(TokenType.RPARENT);
        Block block=buildBlock();
        return new MainFuncDef(inttk,maintk,left,right,block);
    }

    private static FuncType buildFuncType() throws CompileException {
        Token token;
        if(tokens.get(pos).getType()==TokenType.INTTK){
            token=tokens.get(pos);
            compare(TokenType.INTTK);
        }else{
            token=tokens.get(pos);
            compare(TokenType.VOIDTK);
        }
        return new FuncType(token);
    }

    private static FuncFParams buildFuncFParams() throws CompileException{
        List<FuncFParam> funcFParams=new ArrayList<>();
        List<Token> commas=new ArrayList<>();
        funcFParams.add(buildFuncFParam());
        while(tokens.get(pos).getType()==TokenType.COMMA){
            commas.add(tokens.get(pos));
            compare(TokenType.COMMA);
            funcFParams.add(buildFuncFParam());
        }
        return new FuncFParams(funcFParams,commas);
    }

    private static FuncFParam buildFuncFParam() throws CompileException {
        BType bType=buildBType();
        Token ident=tokens.get(pos);
        compare(TokenType.IDENFR);
        List<Token> lefts=null;
        List<Token> rights=null;
        List<ConstExp> constExps=null;
        if(tokens.get(pos).getType()==TokenType.LBRACK)
        {
            lefts=new ArrayList<>();
            rights=new ArrayList<>();
            lefts.add(tokens.get(pos));
            compare(TokenType.LBRACK);
            rights.add(tokens.get(pos));
            compare(TokenType.RBRACK);
            if(tokens.get(pos).getType()==TokenType.LBRACK){
                constExps=new ArrayList<>();
                while(tokens.get(pos).getType()==TokenType.LBRACK){
                    lefts.add(tokens.get(pos));
                    compare(TokenType.LBRACK);
                    constExps.add(buildConstExp());
                    rights.add(tokens.get(pos));
                    compare(TokenType.RBRACK);
                }
            }
        }
        return new FuncFParam(bType,ident,lefts,rights,constExps);
    }

    private static Block buildBlock() throws CompileException {
        Token left=tokens.get(pos);
        compare(TokenType.LBRACE);
        List<BlockItem> blockItems=null;
        while(tokens.get(pos).getType()!=TokenType.RBRACE)
        {
            if(blockItems==null) blockItems=new ArrayList<>();
            blockItems.add(buildBlockItem());
        }
        Token right=tokens.get(pos);
        compare(TokenType.RBRACE);
        return new Block(left,blockItems,right);
    }

    private static BlockItem buildBlockItem() throws CompileException{
        Decl decl=null;
        Stmt stmt=null;
        if(tokens.get(pos).getType()==TokenType.INTTK||
                tokens.get(pos).getType()==TokenType.CONSTTK){
            decl=buildDecl();
        }else{
            stmt=buildStmt();
        }
        return new BlockItem(decl,stmt);
    }

    private static Stmt buildStmt() throws CompileException {
        //block
        if(tokens.get(pos).getType()==TokenType.LBRACE){
            Block block=buildBlock();
            return new Stmt(block);
        }
        //if
        else if(tokens.get(pos).getType()==TokenType.IFTK){
            Token iftk=tokens.get(pos);
            compare(TokenType.IFTK);
            Token left=tokens.get(pos);
            compare(TokenType.LPARENT);
            Cond cond=buildCond();
            Token right=tokens.get(pos);
            compare(TokenType.RPARENT);
            List<Stmt> stmts=new ArrayList<>();
            stmts.add(buildStmt());
            Token elsetk=null;
            if(tokens.get(pos).getType()==TokenType.ELSETK){
                elsetk=tokens.get(pos);
                compare(TokenType.ELSETK);
                stmts.add(buildStmt());
            }
            return new Stmt(iftk,left,right,cond,stmts,elsetk);
        }
        //break
        else if(tokens.get(pos).getType()==TokenType.BREAKTK){
            Token breaktk=tokens.get(pos);
            compare(TokenType.BREAKTK);
            Token semion =tokens.get(pos);
            compare(TokenType.SEMICN);
            return new Stmt(breaktk,semion);
        }
        //continue
        else if(tokens.get(pos).getType()==TokenType.CONTINUETK){
            Token continuetk=tokens.get(pos);
            compare(TokenType.CONTINUETK);
            Token semion =tokens.get(pos);
            compare(TokenType.SEMICN);
            return new Stmt(continuetk,semion);
        }
        //return
        else if(tokens.get(pos).getType()==TokenType.RETURNTK){
            Token returntk=tokens.get(pos);
            Exp exp=null;
            compare(TokenType.RETURNTK);
            if(tokens.get(pos).getType()!=TokenType.SEMICN){
                exp=buildExp();
            }
            Token semion =tokens.get(pos);
            compare(TokenType.SEMICN);
            return new Stmt(returntk,exp,semion);
        }
        //printf
        else if(tokens.get(pos).getType()==TokenType.PRINTFTK){
            Token printftk=tokens.get(pos);
            compare(TokenType.PRINTFTK);
            Token left=tokens.get(pos);
            compare(TokenType.LPARENT);
            Token formatString =tokens.get(pos);
            compare(TokenType.STRCON);
            List<Token> commas=null;
            List<Exp> exps=null;
            while(tokens.get(pos).getType()==TokenType.COMMA){
                if(commas==null) commas=new ArrayList<>();
                if(exps==null) exps=new ArrayList<>();
                commas.add(tokens.get(pos));
                compare(TokenType.COMMA);
                exps.add(buildExp());
            }
            Token right=tokens.get(pos);
            compare(TokenType.RPARENT);
            Token semicn=tokens.get(pos);
            compare(TokenType.SEMICN);
            return new Stmt(printftk,left,right,formatString,commas,exps,semicn);
        }
        //for
        else if(tokens.get(pos).getType()==TokenType.FORTK){
            Token fortk=tokens.get(pos);
            compare(TokenType.FORTK);
            Token left=tokens.get(pos);
            compare(TokenType.LPARENT);
            ForStmt forStmt_1 = null,forStmt_2=null;
            Cond cond=null;
            if(tokens.get(pos).getType()!=TokenType.SEMICN){
                forStmt_1=buildForStmt();
            }
            List<Token> semicns=new ArrayList<>();
            semicns.add(tokens.get(pos));
            compare(TokenType.SEMICN);
            if(tokens.get(pos).getType()!=TokenType.SEMICN){
                cond=buildCond();
            }
            semicns.add(tokens.get(pos));
            compare(TokenType.SEMICN);
            if(tokens.get(pos).getType()!=TokenType.RPARENT){
                forStmt_2=buildForStmt();
            }
            Token right=tokens.get(pos);
            compare(TokenType.RPARENT);
            Stmt stmt=buildStmt();
            return new Stmt(fortk,left,right,forStmt_1,cond,forStmt_2,semicns,stmt);
        }
        //LVal或者exp;
        else{
//            LVal lVal=buildLVal();
//            Token assign=tokens.get(pos);
            boolean flag=false;
//            compare(TokenType.ASSIGN);
            for(int i=pos;
                tokens.get(i).getType()!=TokenType.SEMICN&&
                tokens.get(i).getLine()==tokens.get(pos).getLine();i++){
                if (tokens.get(i).getType() == TokenType.ASSIGN) {
                    flag = true;
                    break;
                }
            }
            if(flag){
                LVal lVal=buildLVal();
                Token assign=tokens.get(pos);
                compare(TokenType.ASSIGN);
                if(tokens.get(pos).getType()==TokenType.GETINTTK){
                    Token getinttk=tokens.get(pos);
                    compare(TokenType.GETINTTK);
                    Token left=tokens.get(pos);
                    compare(TokenType.LPARENT);
                    Token right=tokens.get(pos);
                    compare(TokenType.RPARENT);
                    Token semicn=tokens.get(pos);
                    compare(TokenType.SEMICN);
                    return new Stmt(lVal,assign,getinttk,left,right,semicn);
                }
                else{
                    Exp exp=buildExp();
                    Token semicn=tokens.get(pos);
                    compare(TokenType.SEMICN);
                    return new Stmt(lVal,assign,exp,semicn);
                }
            }else{
                Exp exp=null;
                if(tokens.get(pos).getType()!=TokenType.SEMICN){
                    exp=buildExp();
                }
                Token semicn=tokens.get(pos);
                compare(TokenType.SEMICN);
                return new Stmt(exp,semicn);
            }
        }
    }

    private static ForStmt buildForStmt() throws CompileException{
        LVal lVal = buildLVal();
        Token assign = tokens.get(pos);
        compare(TokenType.ASSIGN);
        Exp exp = buildExp();
        return new ForStmt(lVal,assign,exp);
    }

    private static Exp buildExp() throws CompileException{
        AddExp addExp=buildAddExp();
        return new Exp(addExp);
    }

    private static Cond buildCond() throws CompileException{
        LOrExp lOrExp=buildLOrExp();
        return new Cond(lOrExp);
    }

    private static LVal buildLVal() throws CompileException {
        Token ident =tokens.get(pos);
        compare(TokenType.IDENFR);
        List<Token> lefts=null;
        List<Token> rights=null;
        List<Exp> exps=null;
        while(tokens.get(pos).getType()==TokenType.LBRACK)
        {
            if(lefts==null){
                lefts=new ArrayList<>();
                rights=new ArrayList<>();
                exps=new ArrayList<>();
            }
            lefts.add(tokens.get(pos));
            compare(TokenType.LBRACK);
            exps.add(buildExp());
            rights.add(tokens.get(pos));
            compare(TokenType.RBRACK);
        }
        return new LVal(ident,lefts,rights,exps);
    }

    private static PrimaryExp buildPrimaryExp() throws CompileException{
        Token left=null;
        Token right=null;
        Exp exp=null;
        LVal lVal=null;
        Number number=null;
        if(tokens.get(pos).getType()==TokenType.LPARENT){
            left=tokens.get(pos);
            compare(TokenType.LPARENT);
            exp=buildExp();
            right=tokens.get(pos);
            compare(TokenType.RPARENT);
        }
        else if(tokens.get(pos).getType()==TokenType.INTCON){
            number=buildNumber();
        }
        else{
            lVal=buildLVal();
        }
        return new PrimaryExp(left,right,exp,lVal,number);
    }

    private static Number buildNumber() throws CompileException{
        Token intconst=tokens.get(pos);
        compare(TokenType.INTCON);
        return new Number(intconst);
    }

    private static UnaryExp buildUnaryExp() throws CompileException{
        PrimaryExp primaryExp=null;
        Token ident=null;
        Token left=null;
        Token right=null;
        FuncRParams funcRParams=null;
        UnaryOp unaryOp =null;
        UnaryExp unaryExp = null;
        if(tokens.get(pos).getType()==TokenType.IDENFR&&tokens.get(pos+1).getType()==TokenType.LPARENT){
            ident=tokens.get(pos);
            compare(TokenType.IDENFR);
            left=tokens.get(pos);
            compare(TokenType.LPARENT);
            if(tokens.get(pos).getType()!=TokenType.RPARENT){
                funcRParams=buildFuncRParams();
            }
            right=tokens.get(pos);
            compare(TokenType.RPARENT);
        }else if(tokens.get(pos).getType()==TokenType.PLUS||
                tokens.get(pos).getType()==TokenType.MINU||
            tokens.get(pos).getType()==TokenType.NOT){
            unaryOp=buildUnaryOp();
            unaryExp=buildUnaryExp();
        }
        else{
            primaryExp=buildPrimaryExp();
        }
        return new UnaryExp(primaryExp,ident,left,right,funcRParams,unaryOp,unaryExp);
    }

    private static UnaryOp buildUnaryOp() throws CompileException {
        Token token;
        if(tokens.get(pos).getType()==TokenType.PLUS){
            token=tokens.get(pos);
            compare(TokenType.PLUS);
        }
        else if(tokens.get(pos).getType()==TokenType.MINU){
            token=tokens.get(pos);
            compare(TokenType.MINU);
        }
        else{
            token=tokens.get(pos);
            compare(TokenType.NOT);
        }
        return new UnaryOp(token);
    }

    private static FuncRParams buildFuncRParams() throws CompileException{
        List<Exp> exps=new ArrayList<>();
        List<Token> commas=null;
        exps.add(buildExp());
        while(tokens.get(pos).getType()==TokenType.COMMA){
            if(commas==null){commas=new ArrayList<>();}
            commas.add(tokens.get(pos));
            compare(TokenType.COMMA);
            exps.add(buildExp());
        }
        return new FuncRParams(exps,commas);
    }

    private static MulExp buildMulExp() throws CompileException {
        //MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        //MulExp → UnaryExp | UnaryExp ('*' | '/' | '%') MulExp
        UnaryExp unaryExp;
        Token token=null;
        MulExp mulExp=null;
        unaryExp=buildUnaryExp();
        if(tokens.get(pos).getType()==TokenType.MULT||
                tokens.get(pos).getType()==TokenType.DIV||
                tokens.get(pos).getType()==TokenType.MOD){
            token = tokens.get(pos);
            compare(token.getType());
            mulExp=buildMulExp();
        }
        return new MulExp(unaryExp,token,mulExp);
    }

    private static AddExp buildAddExp() throws CompileException {
        //AddExp → MulExp | AddExp ('+' | '−') MulExp
        //AddExp → MulExp | MulExp ('+' | '−') AddExp
        MulExp mulExp=buildMulExp();
        Token token=null;
        AddExp addExp=null;
        if(tokens.get(pos).getType()==TokenType.PLUS||
                tokens.get(pos).getType()==TokenType.MINU){
            token=tokens.get(pos);
            compare(token.getType());
            addExp=buildAddExp();
        }
        return new AddExp(mulExp,token,addExp);
    }

    private static RelExp buildRelExp() throws CompileException {
        //RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        //RelExp → AddExp | AddExp ('<' | '>' | '<=' | '>=') RelExp
        AddExp addExp=buildAddExp();
        Token token=null;
        RelExp relExp=null;
        if(tokens.get(pos).getType()==TokenType.LSS||
                tokens.get(pos).getType()==TokenType.GRE||
                tokens.get(pos).getType()==TokenType.LEQ||
                tokens.get(pos).getType()==TokenType.GEQ
        ){
            token=tokens.get(pos);
            compare(token.getType());
            relExp=buildRelExp();
        }
        return new RelExp(addExp,token,relExp);
    }

    private static EqExp buildEqExp() throws CompileException {
        //EqExp → RelExp | EqExp ('==' | '!=') RelExp
        //EqExp → RelExp | RelExp ('==' | '!=') EqExp
        RelExp relExp=buildRelExp();
        Token token=null;
        EqExp eqExp=null;
        if(tokens.get(pos).getType()==TokenType.EQL||
        tokens.get(pos).getType()==TokenType.NEQ){
            token=tokens.get(pos);
            compare(token.getType());
            eqExp=buildEqExp();
        }
        return new EqExp(relExp,token,eqExp);
    }

    private static LAndExp buildLAndExp() throws CompileException {
        //LAndExp → EqExp | LAndExp '&&' EqExp
        //LAndExp → EqExp | EqExp '&&' LAndExp
        EqExp eqExp=buildEqExp();
        Token token=null;
        LAndExp lAndExp=null;
        if(tokens.get(pos).getType()==TokenType.AND){
            token=tokens.get(pos);
            compare(token.getType());
            lAndExp=buildLAndExp();
        }
        return new LAndExp(eqExp,token,lAndExp);
    }

    private static LOrExp buildLOrExp() throws CompileException {
        //LOrExp → LAndExp | LOrExp '||' LAndExp
        //LOrExp → LAndExp | LAndExp '||' LOrExp
        LAndExp lAndExp=buildLAndExp();
        Token token=null;
        LOrExp lOrExp=null;
        if(tokens.get(pos).getType()==TokenType.OR){
            token=tokens.get(pos);
            compare(token.getType());
            lOrExp=buildLOrExp();
        }
        return new LOrExp(lAndExp,token,lOrExp);
    }

    private static ConstExp buildConstExp() throws CompileException {
        AddExp addExp;
        addExp=buildAddExp();
        return new ConstExp(addExp);
    }

    //判断当前位置的token是不是对应类型的
    private static void compare(TokenType type) throws CompileException {
        if(tokens.get(pos).getType()==type)
        {
            if(pos<tokens.size()-1) pos++;
        }
        else if(type==TokenType.SEMICN){
            //缺少分号
            ErrorRecord.addError(tokens.get(pos-1).getLine(),tokens.get(pos-1).getIndex(), ErrorType.i);
        }
        else if(type==TokenType.RPARENT){
            //缺少右小括号
            ErrorRecord.addError(tokens.get(pos-1).getLine(),tokens.get(pos-1).getIndex(), ErrorType.j);
        }
        else if(type==TokenType.RBRACK){
            //缺少右中括号
            ErrorRecord.addError(tokens.get(pos-1).getLine(),tokens.get(pos-1).getIndex(), ErrorType.k);
        }
        else {
            System.out.println("The Token Error Happens at: "+pos+"/"+tokens.size());
            System.out.println("The Token is:"+tokens.get(pos));
            if(pos>=1) System.out.println("Last Token is:"+tokens.get(pos-1));
            throw new CompileException(tokens.get(pos).getLine(),type);
        }
    }
}
