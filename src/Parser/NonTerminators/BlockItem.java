package Parser.NonTerminators;

import Parser.Parser;
import Parser.NTTypes;
import utils.IOUtils;

import java.io.IOException;

public class BlockItem {
    public Decl decl;
    public Stmt stmt;
    public BlockItem(Decl decl, Stmt stmt) {
        this.decl=decl;
        this.stmt=stmt;
    }
    public void print() throws IOException {
        if(decl!=null) decl.print();
        else stmt.print();
        //IOUtils.write(Parser.NTTypesToString.get(NTTypes.BlockItem));
    }
}
