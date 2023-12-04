package MIPS.Code;

import MIPS.Register;
import utils.IOUtils;

import java.io.IOException;

public class JRCode extends MIPSCode{

    Register register;

    public JRCode(Register register) {
        this.register = register;
    }

    @Override
    public void print() throws IOException {
        IOUtils.write("\tjr "+register.name+"\n");
    }
}
