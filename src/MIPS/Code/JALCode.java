package MIPS.Code;

import MIPS.Label;
import utils.IOUtils;

import java.io.IOException;

public class JALCode extends MIPSCode{

    Label label;
    public JALCode(Label label){
        this.label = label;
    }
    @Override
    public void print() throws IOException {
        IOUtils.write("\tjal "+label.name+"\n");
    }
}
