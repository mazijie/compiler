package MIPS.Code;

import IR.Type.ValueType;
import IR.Value.Value;
import MIPS.Register;
import MIPS.RegisterManager;
import config.Config;
import utils.Boom;
import utils.IOUtils;

import java.io.IOException;

import static MIPS.Code.BinaryRegImmCode.Op.*;

public class BinaryRegImmCode extends MIPSCode{

    //用于寄存器和立即数的运算符
    public enum Op{
        ADDIU,//加立即数
        SUBIU,//减立即数
        MUL,//乘立即数
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
        SRL,//逻辑右移指令，用于除法优化
        ;
        public String toString() {
            return this.name().toLowerCase();
        }
    }
    Op op;
    Register rt;
    Register rs;
    Integer imm;
    Register r_tmp=null;
    public BinaryRegImmCode(Register rt, Register rs, Integer imm, Op op) throws IOException {
        this.rt = rt;
        this.rs = rs;
        this.op = op;
        if(imm<=-32768||imm>32767){
//            r_tmp = RegisterManager.getTempReg(new Value("temp", ValueType._i32));
            r_tmp = Register.$a3;
            LoadImmCode loadImmCode = new LoadImmCode(r_tmp,imm);
            loadImmCode.print();
            BinaryRegRegCode.Op rrop = switch (op) {
                case ADDIU -> BinaryRegRegCode.Op.ADDU;
                case SUBIU -> BinaryRegRegCode.Op.SUBU;
                case MUL -> BinaryRegRegCode.Op.MUL;
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
                case SRL -> BinaryRegRegCode.Op.SRLV;
            };
            BinaryRegRegCode code = new BinaryRegRegCode(rt,rs,r_tmp,rrop);
            code.print();
//            RegisterManager.freeReg(r_tmp);
        }else{
            this.imm=imm;
        }
    }

    @Override
    public void print() throws IOException {
        if(r_tmp!=null) return;
        if(Config.mips_optimize&&op==MUL){
            //乘法小优化:当立即数是2的幂或者与之相差为2
            if(imm==0){
                MoveCode moveCode = new MoveCode(RegisterManager.getZero(),rt);
                moveCode.print();
                return;
            }else if(imm==1){
                MoveCode moveCode = new MoveCode(rs,rt);
                moveCode.print();
                return;
            }else if(imm==-1){
                BinaryRegRegCode minuscode = new BinaryRegRegCode(rt,RegisterManager.getZero(),rs, BinaryRegRegCode.Op.SUBU);
                minuscode.print();
                return;
            }
            else if(isNiceImmForMul(imm)>0){
                int rank = isNiceImmForMul(imm);
                BinaryRegImmCode sllcode = new BinaryRegImmCode(rt,rs,rank,Op.SLL);
                sllcode.print();
                int powres =1;
                for(int t=1;t<=rank;t++){
                    powres*=2;
                }
                int gap = powres-imm;
                if(gap==0)
                    return;
                else if(gap<0){
                    BinaryRegRegCode addCode = new BinaryRegRegCode(rt,rt,rs, BinaryRegRegCode.Op.ADDU);
                    while(gap<0){
                        addCode.print();
                        gap++;
                    }
                }else{
                    BinaryRegRegCode subCode = new BinaryRegRegCode(rt,rt,rs, BinaryRegRegCode.Op.SUBU);
                    while(gap>0){
                        subCode.print();
                        gap--;
                    }
                }
            }else{
                IOUtils.write("\t"+op.toString()+" "+rt.name+", "+rs.name+", "+imm+"\n");
            }
        }
        else if(Config.mips_optimize&&op==DIV&&false){
            //除法小优化,由于负数无法使用，作废
            if(imm==1){
                MoveCode moveCode=new MoveCode(rs,rt);
                moveCode.print();
                return;
            }else if(imm==-1){
                BinaryRegRegCode minuscode = new BinaryRegRegCode(rt,RegisterManager.getZero(),rs, BinaryRegRegCode.Op.SUBU);
                minuscode.print();
                return;
            }
            int base=1;
            int i;
            for(i=1;i<30;i++){
                base*=2;
                if(imm==base||imm+base==0){
                    break;
                }
            }
            if(i==30)
                IOUtils.write("\t"+op.toString()+" "+rt.name+", "+rs.name+", "+imm+"\n");
            else{
                BinaryRegImmCode sracode = new BinaryRegImmCode(rt,rs,i,SRA);
                sracode.print();
                Register temp = Register.$a3;
                BinaryRegImmCode srlcode = new BinaryRegImmCode(temp,rt,31,SRL);
                srlcode.print();
                BinaryRegRegCode addcode = new BinaryRegRegCode(rt,rt,temp, BinaryRegRegCode.Op.ADDU);
                addcode.print();
                if(imm<0) {
                    BinaryRegImmCode minuscode = new BinaryRegImmCode(rt,rt,-1,MUL);
                    minuscode.print();
                }
            }
        }
        else if(Config.mips_optimize&&op==SUBIU){
            BinaryRegImmCode addcode = new BinaryRegImmCode(rt,rs,-1*imm,ADDIU);
            addcode.print();
        }
        else{

            IOUtils.write("\t"+op.toString()+" "+rt.name+", "+rs.name+", "+imm+"\n");
        }
    }

    private int isNiceImmForMul(Integer imm) {
        int base=1;
        if(imm<0) return -1;
        for(int i=1;i<18;i++){
            base*=2;
            if(imm<base-2) return -1;
            else if(imm<=base+2) return i;
        }
        return -1;
    }
}
