package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import java.io.IOException;
import java.util.List;

import Parser.*;

public class Stmt {
    public int type;
    public LVal lVal;
    public Token equal;
    public Token semicn;
    public Stmt stmt;
    public Exp exp;
    public List<Exp> exps;
    public List<Token> semicns;
    public Block block;
    public Token iftk;
    public Token left;
    public Token right;
    public Cond cond;
    public List<Stmt> stmts;
    public Token elsetk;
    public Token fortk;
    public ForStmt forStmts_1;
    public ForStmt forStmts_2;
    public Token breaktkORcontinuetk;
    public Token returntk;
    public Token getinttk;
    public Token printftk;
    public Token formatstring;
    public List<Token> commas;
    //type1:LVal '=' Exp ';'
    public Stmt(LVal lVal, Token equal,Exp exp,Token semicn){
        type=1;
        this.lVal=lVal;
        this.equal=equal;
        this.exp=exp;
        this.semicn=semicn;
    }

    //type2:[Exp] ';'
    public Stmt(Exp exp,Token semicn){
        type=2;
        this.exp=exp;
        this.semicn=semicn;
    }

    //type3:Block
    public Stmt(Block block){
        type=3;
        this.block=block;
    }

    //type4:'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    public Stmt(Token iftk,Token left,Token right,Cond cond,List<Stmt> stmts,Token elsetk){
        type=4;
        this.iftk=iftk;
        this.left=left;
        this.right=right;
        this.cond=cond;
        this.stmts=stmts;
        this.elsetk=elsetk;
    }

    //type5:'for' '(' [ForStmt] ';' [Cond] ';' [forStmt] ')' Stmt
    public Stmt(Token fortk,Token left,Token right,
                ForStmt forStmts_1,Cond cond,ForStmt forStmts_2,
                List<Token> semicns, Stmt stmt){
        type=5;
        this.fortk=fortk;
        this.left=left;
        this.right=right;
        this.forStmts_1=forStmts_1;
        this.cond=cond;
        this.forStmts_2=forStmts_2;
        this.semicns=semicns;
        this.stmt=stmt;
    }

    //type6_1:'break' ';'
    //type6_2:'continue' ';'
    public Stmt(Token breaktk,Token semicn){
        type=6;
        this.breaktkORcontinuetk=breaktk;
        this.semicn=semicn;
    }

    //type7:'return' [Exp] ';'
    public Stmt(Token returntk,Exp exp,Token semicn){
        type=7;
        this.returntk=returntk;
        this.exp=exp;
        this.semicn=semicn;
    }

    //type8:LVal '=' 'getint''('')'';'
    public Stmt(LVal lVal,Token equal,Token getinttk,Token left,
                Token right,Token selicn){
        type=8;
        this.lVal=lVal;
        this.equal=equal;
        this.getinttk=getinttk;
        this.left=left;
        this.right=right;
        this.semicn=selicn;
    }

    //type9:'printf''('FormatString{','Exp}')'';'
    public Stmt(Token printftk,Token left,Token right,Token formatstring,
                List<Token> commas,List<Exp> exps,Token selicn){
        type=9;
        this.printftk=printftk;
        this.left=left;
        this.right=right;
        this.formatstring=formatstring;
        this.commas=commas;
        this.exps=exps;
        this.semicn=selicn;
    }

    public void print() throws IOException {
        switch (type){
            case 1:
                lVal.print();
                IOUtils.write(equal.toString());
                exp.print();
                IOUtils.write(semicn.toString());
                break;
            case 2:
                if(exp!=null) exp.print();
                IOUtils.write(semicn.toString());
                break;
            case 3:
                block.print();
                break;
            case 4:
                IOUtils.write(iftk.toString());
                IOUtils.write(left.toString());
                cond.print();
                IOUtils.write(right.toString());
                stmts.get(0).print();
                if(elsetk!=null){
                    IOUtils.write(elsetk.toString());
                    stmts.get(1).print();
                }
                break;
            case 5:
                IOUtils.write(fortk.toString());
                IOUtils.write(left.toString());
                if(forStmts_1!=null)
                {
                    forStmts_1.print();
                }
                IOUtils.write(semicns.get(0).toString());
                if(cond!=null)
                {
                    cond.print();
                }
                IOUtils.write(semicns.get(1).toString());
                if(forStmts_2!=null)
                {
                    forStmts_2.print();
                }
                IOUtils.write(right.toString());
                stmt.print();
                break;
            case 6:
                IOUtils.write(breaktkORcontinuetk.toString());
                IOUtils.write(semicn.toString());
                break;
            case 7:
                IOUtils.write(returntk.toString());
                if(exp!=null){
                    exp.print();
                }
                IOUtils.write(semicn.toString());
                break;
            case 8:
                lVal.print();
                IOUtils.write(equal.toString());
                IOUtils.write(getinttk.toString());
                IOUtils.write(left.toString());
                IOUtils.write(right.toString());
                IOUtils.write(semicn.toString());
                break;
            case 9:
                IOUtils.write(printftk.toString());
                IOUtils.write(left.toString());
                IOUtils.write(formatstring.toString());
                if(commas!=null){
                    for(int i=0;i<commas.size();i++){
                        IOUtils.write(commas.get(i).toString());
                        exps.get(i).print();
                    }
                }
                IOUtils.write(right.toString());
                IOUtils.write(semicn.toString());
                break;
        }
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.Stmt));
    }
}
