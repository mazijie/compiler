package MIPS.Code;

import MIPS.Address;
import MIPS.Register;
import utils.IOUtils;

import java.io.IOException;

public class LoadAddressCode extends MIPSCode{
    public Register register;
    public Address address;

    public LoadAddressCode(Register register, Address address) {
        this.register = register;
        this.address = address;
    }

    @Override
    public void print() throws IOException {
        IOUtils.write("\tla "+register.name+", "+address.toString()+"\n");
    }
}
