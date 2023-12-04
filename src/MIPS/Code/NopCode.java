package MIPS.Code;

import utils.IOUtils;

import java.io.IOException;

public class NopCode extends MIPSCode{
    @Override
    public void print() throws IOException {
        IOUtils.write("\tnop\n");
    }
}
