package MIPS.Code;

import MIPS.Label;
import utils.IOUtils;

import java.io.IOException;

public class JumpCode extends MIPSCode{

    Label label;
    public JumpCode(Label label){
        this.label = label;
    }
    @Override
    public void print() throws IOException {
        IOUtils.write("\tj "+label.name+"\n");
    }
}
