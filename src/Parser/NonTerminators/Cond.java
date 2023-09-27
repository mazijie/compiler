package Parser.NonTerminators;

import utils.IOUtils;
import Parser.Parser;
import Parser.NTTypes;

import java.io.IOException;

public class Cond {
    private LOrExp lOrExp;
    public Cond(LOrExp lOrExp)
    {
        this.lOrExp=lOrExp;
    }
    public void print() throws IOException {
        this.lOrExp.print();
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.Cond));
    }
}
