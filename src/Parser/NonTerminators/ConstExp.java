package Parser.NonTerminators;

import utils.IOUtils;
import Parser.*;

import java.io.IOException;

public class ConstExp {
    private final AddExp addExp;
    public ConstExp(AddExp addExp){
        this.addExp = addExp;
    }
    public void print() throws IOException {
        addExp.print();
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.ConstExp));
    }
}
