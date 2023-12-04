package MIPS.Code;

import IR.Type.ValueType;
import IR.Value.Value;
import MIPS.Register;
import MIPS.RegisterManager;
import utils.IOUtils;

import java.io.IOException;

public class BinaryRegImmCode extends MIPSCode{

    //用于寄存器和立即数的运算符
    public enum Op{
        ADDI,//加立即数
        SUBI,//减立即数
        MULO,//乘立即数
        DIV, //除立即数
        ANDI,//与立即数
        ORI,//或立即数
        SLL,//逻辑左移
        SRA,//算术右移
        XORI,//异或立即数
        SEQ,//等于立即数置1
        SNE,//不等于立即数置1
        SLE,//小于等于立即数置1
        SLTI,//小于立即数置1
        SGT,//大于立即数置1
        SGE,//大于等于立即数置1
        ;
        public String toString() {
            return this.name().toLowerCase();
        }
    }
    Op op;
    Register rt;
    Register rs;
    Integer imm;
    Register r_tmp;
    public BinaryRegImmCode(Register rt, Register rs, Integer imm, Op op) throws IOException {
        this.rt = rt;
        this.rs = rs;
        this.op = op;
        if(imm<=-32768||imm>32767){
            r_tmp = RegisterManager.getTempReg(new Value("temp", ValueType._i32));
            LoadImmCode loadImmCode = new LoadImmCode(r_tmp,imm);
            loadImmCode.print();
            BinaryRegRegCode.Op rrop = switch (op) {
                case ADDI -> BinaryRegRegCode.Op.ADD;
                case SUBI -> BinaryRegRegCode.Op.SUB;
                case MULO -> BinaryRegRegCode.Op.MUL;
                case DIV -> BinaryRegRegCode.Op.DIV;
                case ANDI -> BinaryRegRegCode.Op.AND;
                case ORI -> BinaryRegRegCode.Op.OR;
                case SLL -> BinaryRegRegCode.Op.SLLV;
                case SRA -> BinaryRegRegCode.Op.SRAV;
                case XORI -> BinaryRegRegCode.Op.XOR;
                case SEQ -> BinaryRegRegCode.Op.SEQ;
                case SNE -> BinaryRegRegCode.Op.SNE;
                case SLE -> BinaryRegRegCode.Op.SLE;
                case SLTI -> BinaryRegRegCode.Op.SLT;
                case SGT -> BinaryRegRegCode.Op.SGT;
                case SGE -> BinaryRegRegCode.Op.SGE;
            };
            BinaryRegRegCode code = new BinaryRegRegCode(rt,rs,r_tmp,rrop);
            code.print();
            RegisterManager.freeReg(r_tmp);
        }else{
            this.imm=imm;
        }
    }

    @Override
    public void print() throws IOException {
        if(r_tmp==null)
            IOUtils.write("\t"+op.toString()+" "+rt.name+", "+rs.name+", "+imm+"\n");
    }
}
