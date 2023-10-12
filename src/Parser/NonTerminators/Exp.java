package Parser.NonTerminators;

import utils.IOUtils;

import Parser.*;

import java.io.IOException;

public class Exp {
    public AddExp addExp;
    public Exp(AddExp addExp){
        this.addExp=addExp;
    }
    public void print() throws IOException {
        addExp.print();
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.Exp));
    }
}
