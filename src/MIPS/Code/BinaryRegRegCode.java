package MIPS.Code;

import MIPS.Register;
import utils.IOUtils;

import java.io.IOException;

import static MIPS.Code.BinaryRegRegCode.Op.SGT;

public class BinaryRegRegCode extends MIPSCode{

    private final Register rd;
    private final Register rs;
    private final Register rt;
    private final Op op;

    public enum Op{
        ADDU,//加
        SUBU,//减
        MUL,//乘
        DIV, //除
        AND,//与
        OR,//或
        SLLV,//逻辑左移
        SRAV,//算术右移
        XOR,//异或
        SEQ,//等于置1
        SNE,//不等于置1
        SLE,//小于等于置1
        SLT,//小于置1
        SGT,//大于置1
        SGE,//大于等于置1
        ;
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    public BinaryRegRegCode(Register rd, Register rs, Register rt, Op op){
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
        this.op = op;
    }

    @Override
    public void print() throws IOException {
            IOUtils.write("\t"+op.toString()+" "+rd.name+", "+rs.name+", "+rt.name+"\n");
    }
}
