package MIPS.Code;

import utils.IOUtils;

import java.io.IOException;

public class SyscallCode extends MIPSCode{
    @Override
    public void print() throws IOException {
        IOUtils.write("\tsyscall\n");
    }
}
