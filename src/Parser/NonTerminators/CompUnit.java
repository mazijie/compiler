package Parser.NonTerminators;

import Parser.Parser;
import Parser.NTTypes;
import utils.IOUtils;

import java.io.IOException;
import java.util.List;

public class CompUnit {
    //CompUnit → {Decl} {FuncDef} MainFuncDef
    private List<Decl> decls;
    private List<FuncDef> funcDefs;
    private MainFuncDef mainFuncDef;
    public CompUnit(List<Decl> decls,List<FuncDef> funcDefs,MainFuncDef mainFuncDef){
        this.decls=decls;
        this.funcDefs=funcDefs;
        this.mainFuncDef=mainFuncDef;
    }
    public void print() throws IOException {
        for(Decl decl : this.decls)
        {
            decl.print();
        }
        for(FuncDef funcDef : this.funcDefs)
        {
            funcDef.print();
        }
        this.mainFuncDef.print();
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.CompUnit));
    }
}
