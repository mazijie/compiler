package MIPS.Code;

import MIPS.Address;
import MIPS.Register;
import utils.Boom;
import utils.IOUtils;

import java.io.IOException;

public class StoreWordCode extends MIPSCode{

    public Register register;
    public Address address;

    public StoreWordCode(Register register,Address address) {
        this.register = register;
        this.address = address;
    }
    @Override
    public void print() throws IOException {
        if(address!=null)
            IOUtils.write("\tsw "+register.name+", "+address.toString()+"\n");
        else
            Boom.boom();
    }
}
