package MIPS.Code;

import MIPS.Register;
import utils.IOUtils;

import java.io.IOException;

public class LoadImmCode extends MIPSCode{

    public Register register;
    public Integer imm;

    public LoadImmCode(Register register, Integer imm) {
        this.register = register;
        this.imm = imm;
    }

    @Override
    public void print() throws IOException {
        IOUtils.write("\tli "+register.name+", "+imm+"\n");
    }
}
