package MIPS.Code;

import MIPS.Register;
import utils.IOUtils;

import java.io.IOException;

public class MFHICode {
    public Register rt;
    public MFHICode(Register rt){
        this.rt = rt;
    }
    public void print() throws IOException {
        IOUtils.write("\tmfhi "+rt.name+"\n");
    }
}
