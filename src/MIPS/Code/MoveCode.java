package MIPS.Code;

import MIPS.Register;
import utils.IOUtils;

import java.io.IOException;

public class MoveCode extends MIPSCode{
    public Register rs;
    public Register rt;

    public MoveCode(Register rs, Register rt) {
        this.rs = rs;
        this.rt = rt;
    }

    @Override
    public void print() throws IOException {
        IOUtils.write("\tmove "+rt.name+", "+rs.name+"\n");
    }
}
