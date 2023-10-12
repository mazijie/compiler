package Parser.NonTerminators;

import java.io.IOException;

public class Decl {
    public ConstDecl constDecl;
    public VarDecl varDecl;
    public Decl(ConstDecl constDecl, VarDecl varDecl) {
        this.constDecl=constDecl;
        this.varDecl=varDecl;
    }
    public void print() throws IOException {
        if(this.constDecl!=null){
            this.constDecl.print();
        }else{
            this.varDecl.print();
        }
    }
}
