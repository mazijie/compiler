package MIPS.Code;

import MIPS.Register;
import utils.IOUtils;

import java.io.IOException;

public class DIVCode extends MIPSCode{

    public Register rs,rt;

    public DIVCode(Register rs, Register rt) {
        this.rs = rs;
        this.rt = rt;
    }

    @Override
    public void print() throws IOException {
        IOUtils.write("\tdiv "+rs.name+", "+rt.name+"\n");
    }
}
