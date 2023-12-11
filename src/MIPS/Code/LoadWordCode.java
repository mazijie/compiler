package MIPS.Code;

import MIPS.Address;
import MIPS.Register;
import utils.Boom;
import utils.IOUtils;

import java.io.IOException;

public class LoadWordCode extends MIPSCode{

    public Register rt;
    public Address address;

    public LoadWordCode(Register rt, Address address) {
        this.rt = rt;
        this.address = address;
    }

    @Override
    public void print() throws IOException {
        if(address!=null)
            IOUtils.write("\tlw "+rt.name+", "+address.toString()+"\n");
        else
//            IOUtils.write("\tlw "+rt.name+", "+"null"+"\n");
            Boom.boom();
    }
}
