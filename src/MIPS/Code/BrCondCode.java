package MIPS.Code;

import MIPS.Label;
import MIPS.Register;
import MIPS.RegisterManager;
import utils.IOUtils;

import java.io.IOException;

public class BrCondCode extends MIPSCode{

    Label true_label,false_label;
    Register res;
    Register zero = RegisterManager.getZero();

    public BrCondCode(Register res, Label true_label, Label false_label){
        this.res = res;
        this.true_label = true_label;
        this.false_label = false_label;
    }
    @Override
    public void print() throws IOException {
        if(false_label!=null) IOUtils.write("\tbeq "+res.name+", "+zero.name+", "+false_label.name+"\n");
        if(true_label!=null) IOUtils.write("\tbne "+res.name+", "+zero.name+", "+true_label.name+"\n");
    }
}
