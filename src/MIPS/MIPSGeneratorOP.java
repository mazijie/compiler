package MIPS;

import IR.Array.Array;
import IR.Array.ArrayType;
import IR.IRModule;
import IR.Type.ValueType;
import IR.Value.*;
import IR.Value.Instructions.*;
import MIPS.Code.*;
import config.Config;
import utils.Boom;
import utils.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MIPSGeneratorOP {

//    public static List<MIPSCode> codes = new ArrayList<>();
    static Function curFunction;
//    private static Register rt;

    public static void genMips(IRModule module) throws IOException {
        // .data
        if(!module.globalVars.isEmpty()){IOUtils.write(".data\n");}
        for (GlobalVar globalVar : module.globalVars) {
            IOUtils.write("\t");
            if (globalVar.isArray()){
                //数组
                Array array = globalVar.array;
                if(array.all_zero){
                    int length = array.arrayType.length;
                    if(array.arrays!=null){
                        length *= ((ArrayType)(array.arrayType).elementType).length;
                    }
                    IOUtils.write(globalVar.name.substring(1)+": .space "+length*4);
                }
                else{
                    IOUtils.write(globalVar.name.substring(1)+": .word ");
                    if(array.arrays!=null){
                        for(int i = 0; i<array.arrayType.length;i++){
                            if(array.arrays.size()<=i){
                                for(int j = 0;j<((ArrayType)(array.arrayType).elementType).length;j++){
                                    IOUtils.write(("0,"));
                                }
                            }else{
                                Array subArray = array.arrays.get(i);
                                for(int j = 0;j<((ArrayType)subArray.type).length;j++){
                                    if(j>=subArray.arrayType.length) IOUtils.write("0,");
                                    else IOUtils.write(subArray.vals.get(j).val+",");
                                }
                            }
                        }

                    }else{
                        for(int i = 0;i<(array.arrayType).length;i++){
                            if(i>=array.vals.size()) IOUtils.write("0,");
                            else IOUtils.write(array.vals.get(i).val+",");
                        }
                    }
                }
                IOUtils.write("\n");
            }else if(globalVar.isString()) {
                //常量字符串
                IOUtils.write(globalVar.name.substring(1)+": .asciiz \""+globalVar.string+"\"\n");
            } else {
                //普通全局变量
                IOUtils.write(globalVar.name.substring(1)+": .word "+globalVar.val+"\n");
            }
        }
        IOUtils.write(".text\n");
        //--------------------------call main-------------------------------------
        AddressManager.addSP();
        StoreWordCode storeFP = new StoreWordCode(RegisterManager.getFp(),AddressManager.getSPAddress());//将FP存起来
        storeFP.print();
        JALCode jalCode = new JALCode(new Label("main"));
        jalCode.print();
        //--------------------------call main-------------------------------------

        IOUtils.write("\tli $v0, 10\n\tsyscall\n");
        for(Function function:module.functions){
            IOUtils.write(function.name+":\n");
            //TODO:函数开始时的一些工作
            {
                curFunction=function;
//                RegisterManager.freeAllRegs();//释放全部的全局寄存器和临时寄存器
                AddressManager.setOffsetZero();//将SP与FP差值置0
                AddressManager.clearArray();//清理所有的局部数组
//                RegisterManager.clearGlobalValues();
                AddressManager.clearAddressOfValue();
                for(int i=0;i<4&&i<function.arguments.size();i++){
                    Register target =RegisterManager.getRegOP(function.arguments.get(i).ValueInFunc,0,false);
//                    Register target = RegisterManager.getTempReg(function.arguments.get(i));
                    MoveCode moveCode = new MoveCode(RegisterManager.getParamReg(i),target);
                    moveCode.print();
                    RegisterManager.freeTempRegOP(target,true);
                }
                for(int i=4;i<function.arguments.size();i++){
                    Register target =RegisterManager.getRegOP(function.arguments.get(i).ValueInFunc,0,false);
//                    Register target = RegisterManager.getTempReg(function.arguments.get(i));
                    LoadWordCode loadWordCode = new LoadWordCode(target,new Address(RegisterManager.getFp(),4*(i-3)));
                    loadWordCode.print();
                    RegisterManager.freeTempRegOP(target,true);
//                    AddressManager.subSP();
                }
            }
            int i=0;
            for(BasicBlock basicBlock:function.basicBlocks){
                if(i!=0) IOUtils.write(function.name+"_"+basicBlock.name.substring(1)+":\n");

                //到新的基本块就清除所有的临时寄存器（教材说的）
//                if(i!=0)RegisterManager.freeTempRegs();
                i++;
                for(Instruction instruction:basicBlock.instructions){
                    RegisterManager.freeGlobalRegsOP(instruction.whoIn);//把不在in集合里的寄存器都free了
                    if(!RegisterManager.tempUsedOP.isEmpty()) {
                        for(Register register:RegisterManager.tempUsedOP.keySet()){
                            System.out.println(register.name);
                        }
                        Boom.boom();//检查有没有没清干净的临时寄存器
                    }
                    translate(instruction);
                }
            }
        }
    }

    private static void translate(Instruction instruction) throws IOException {
        //优化部分：对不在指令in集合里的值，全部释放(废弃，改为图着色算法)
//        if(Config.mips_optimize)
//        {
//            HashSet<Value> inVars = new HashSet<>(instruction.whoIn);
//            RegisterManager.freeReg(inVars);
//        }
        if (instruction.type == InstructionType.ADD) transADD(instruction);
        else if (instruction.type == InstructionType.ALLOCA) transALLOCA(instruction);
        else if (instruction.type == InstructionType.BR) transBR(instruction);
        else if (instruction.type == InstructionType.CALL) transCALL(instruction);
        else if (instruction.type == InstructionType.GETELEMENTPTR) transGETELEMENTPTR(instruction);
        else if (instruction.type == InstructionType.ICMP) transICMP(instruction);
        else if (instruction.type == InstructionType.LOAD) transLOAD(instruction);
        else if (instruction.type == InstructionType.MUL) transMUL(instruction);
        else if (instruction.type == InstructionType.RET) transRET(instruction);
        else if (instruction.type == InstructionType.SDIV) transSDIV(instruction);
        else if (instruction.type == InstructionType.SREM) transSREM(instruction);
        else if (instruction.type == InstructionType.STORE) transSTORE(instruction);
        else if (instruction.type == InstructionType.SUB) transSUB(instruction);
        else if (instruction.type == InstructionType.ZEXTTO) transZEXTTO(instruction);
        else Boom.boom();
    }

    private static void transZEXTTO(Instruction instruction) throws IOException {
        ZEXTTO ir = (ZEXTTO) instruction;
//        Register rs = RegisterManager.getTempReg(ir.zextfrom);
//        Register rt = RegisterManager.getTempReg(ir.zextto);
        Register rs =  RegisterManager.getRegOP(ir.zextfrom,0,true);
        Register rt =  RegisterManager.getRegOP(ir.zextto,1,false);
        MoveCode code = new MoveCode(rs,rt);
        code.print();
        RegisterManager.freeTempRegOP(rs,false);
        RegisterManager.freeTempRegOP(rt,true);
    }

    private static void transSUB(Instruction instruction) throws IOException {
        SUB ir = (SUB) instruction;
//        Register result = RegisterManager.getTempReg(ir.res);
        Register result = RegisterManager.getRegOP(ir.res,0,false);
        Register rs=null,rt=null;
        if(ir.v1 instanceof ConstInteger && ir.v2 instanceof ConstInteger){
            ConstInteger c1 = (ConstInteger) ir.v1;
            ConstInteger c2 = (ConstInteger) ir.v2;
            int val = c1.val - c2.val;
            LoadImmCode code = new LoadImmCode(result, val);
            code.print();
        }else if(ir.v2 instanceof ConstInteger){
            rs = RegisterManager.getRegOP(ir.v1,1,true);
//            Register rs = RegisterManager.getTempReg(ir.v1);
            int imm = ((ConstInteger)(ir.v2)).val;
            BinaryRegImmCode code = new BinaryRegImmCode(result, rs , imm, BinaryRegImmCode.Op.SUBIU);

            code.print();
        }else if(ir.v1 instanceof ConstInteger){
            rs = RegisterManager.getTempApplicationRegOP(1);
//            Register rs = RegisterManager.getTempReg(ir.v1);
            LoadImmCode code_1 = new LoadImmCode(rs, ((ConstInteger) ir.v1).val);
            code_1.print();
            rt = RegisterManager.getRegOP(ir.v2,2,true);
//            Register rt = RegisterManager.getTempReg(ir.v2);
            BinaryRegRegCode code_2 = new BinaryRegRegCode(result, rs , rt, BinaryRegRegCode.Op.SUBU);
            code_2.print();
        }else{
            rs = RegisterManager.getRegOP(ir.v1,1,true);
            rt = RegisterManager.getRegOP(ir.v2,2,true);
//            Register rs = RegisterManager.getTempReg(ir.v1);
//            Register rt = RegisterManager.getTempReg(ir.v2);
            BinaryRegRegCode code = new BinaryRegRegCode(result, rs , rt, BinaryRegRegCode.Op.SUBU);
            code.print();
        }
        RegisterManager.freeTempRegOP(result,true);
        if(rs!=null) RegisterManager.freeTempRegOP(rs,false);
        if(rt!=null) RegisterManager.freeTempRegOP(rt,false);
    }

    private static void transSTORE(Instruction instruction) throws IOException {
        STORE ir = (STORE)instruction;
        //存东西，要不就是把值存到地址，要不就是把值存到变量
        if(ir.v2 instanceof GlobalVar){
            //全局变量必须直接存，只可能是把值存到变量
            Register rs = RegisterManager.getRegOP(ir.v1,0,true);
//            Register rs = RegisterManager.getTempReg(ir.v1);
            if(ir.v1 instanceof ConstInteger){
                if(((ConstInteger) ir.v1).val!=0) {
                    LoadImmCode loadImmCode = new LoadImmCode(rs, ((ConstInteger) ir.v1).val);
                    loadImmCode.print();
                } else{
                    MoveCode moveCode = new MoveCode(RegisterManager.getZero(),rs);
                    moveCode.print();
                }
            }
            StoreWordCode code = new StoreWordCode(rs, new Address(new Label(ir.v2.name.substring(1))));
            code.print();
            RegisterManager.freeTempRegOP(rs,false);
        }else{
            //局部变量

            if(!isAddressModeOP(ir.v2)){//变量模式:由寄存器代内存管理变量,实际是move
                Register rt = RegisterManager.getRegOP(ir.v2,0,false);
//                Register rt = RegisterManager.getGlobalReg(ir.v2);
                if(ir.v1 instanceof ConstInteger){
                    if(((ConstInteger) ir.v1).val!=0) {
                        LoadImmCode loadImmCode = new LoadImmCode(rt, ((ConstInteger) ir.v1).val);
                        loadImmCode.print();
                    } else{
                        MoveCode moveCode = new MoveCode(RegisterManager.getZero(),rt);
                        moveCode.print();
                    }
                }else{
//                    Register rs = RegisterManager.getTempReg(ir.v1);
                    Register rs = RegisterManager.getRegOP(ir.v1,1,true);
                    MoveCode moveCode = new MoveCode(rs,rt);
                    moveCode.print();
                    RegisterManager.freeTempRegOP(rs,false);
                }
                RegisterManager.freeTempRegOP(rt,true);//变量模式下，rt被修改
            }else{//寄存器为地址模式，必须将rs的值存到rt存的对应地址位置
                Register rt = RegisterManager.getRegOP(ir.v2,0,true);
//                Register rt = RegisterManager.getTempReg(ir.v2);
                if(ir.v1 instanceof ConstInteger){
                    if(((ConstInteger) ir.v1).val!=0) {
                        //将一个常数存入地址
//                        Register rs = RegisterManager.getTempReg(ir.v1);
                        Register rs = RegisterManager.getRegOP(ir.v1,1,true);
                        LoadImmCode loadImmCode = new LoadImmCode(rs,((ConstInteger) ir.v1).val);
                        loadImmCode.print();
                        StoreWordCode storeWordCode = new StoreWordCode(rs,new Address(rt));
                        storeWordCode.print();
                        RegisterManager.freeTempRegOP(rs,false);
//                        RegisterManager.freeReg(rs);
                    } else{
                        //将0存入地址
                        Register rs = RegisterManager.getZero();
                        StoreWordCode storeWordCode = new StoreWordCode(rs,new Address(rt));
                        storeWordCode.print();
                    }
                }else{
                    //将寄存器值存入地址
//                    Register rs = RegisterManager.getTempReg(ir.v1);
                    Register rs = RegisterManager.getRegOP(ir.v1,1,true);
                    StoreWordCode storeWordCode = new StoreWordCode(rs,new Address(rt));
                    storeWordCode.print();
                    RegisterManager.freeTempRegOP(rs,false);
                }
                RegisterManager.freeTempRegOP(rt,false);//地址模式下，寄存器的值不变
            }
        }
    }

    private static void transSREM(Instruction instruction) throws IOException {
        SREM ir = (SREM) instruction;
        Register result = RegisterManager.getRegOP(ir.res,0,false);
//        Register result = RegisterManager.getTempReg(ir.res);
        if(ir.v1 instanceof ConstInteger && ir.v2 instanceof ConstInteger){
            ConstInteger c1 = (ConstInteger) ir.v1;
            ConstInteger c2 = (ConstInteger) ir.v2;
            int val = c1.val % c2.val;
            LoadImmCode code = new LoadImmCode(result, val);
            code.print();
        }else if(ir.v2 instanceof ConstInteger){
            Register rs = RegisterManager.getRegOP(ir.v1,1,true);
            Register rt = RegisterManager.getTempApplicationRegOP(2);
//            Register rs = RegisterManager.getTempReg(ir.v1);
//            Register rt = RegisterManager.getTempReg(ir.v2);
            int imm = ((ConstInteger)(ir.v2)).val;
            LoadImmCode loadImmCode = new LoadImmCode(rt,imm);
            loadImmCode.print();
            DIVCode divCode = new DIVCode(rs,rt);
            divCode.print();
            MFHICode mfhiCode = new MFHICode(result);
            mfhiCode.print();
            RegisterManager.freeTempRegOP(rs,false);
            RegisterManager.freeTempRegOP(rt,false);
        }else if(ir.v1 instanceof ConstInteger){
//            Register rs = RegisterManager.getTempReg(ir.v1);
//            Register rt = RegisterManager.getTempReg(ir.v2);
            Register rs = RegisterManager.getTempApplicationRegOP(1);
            Register rt = RegisterManager.getRegOP(ir.v2,2,true);
            int imm = ((ConstInteger)(ir.v1)).val;
            LoadImmCode loadImmCode = new LoadImmCode(rs,imm);
            loadImmCode.print();
            DIVCode divCode = new DIVCode(rs,rt);
            divCode.print();
            MFHICode mfhiCode = new MFHICode(result);
            mfhiCode.print();
            RegisterManager.freeTempRegOP(rs,false);
            RegisterManager.freeTempRegOP(rt,false);
        }else{
            Register rs = RegisterManager.getRegOP(ir.v1,1,true);
            Register rt = RegisterManager.getRegOP(ir.v2,2,true);
//            Register rs = RegisterManager.getTempReg(ir.v1);
//            Register rt = RegisterManager.getTempReg(ir.v2);
            DIVCode divCode = new DIVCode(rs,rt);
            divCode.print();
            MFHICode mfhiCode = new MFHICode(result);
            mfhiCode.print();
            RegisterManager.freeTempRegOP(rs,false);
            RegisterManager.freeTempRegOP(rt,false);
        }
        RegisterManager.freeTempRegOP(result,true);
    }

    private static void transSDIV(Instruction instruction) throws IOException {
        SDIV ir = (SDIV) instruction;
        Register result = RegisterManager.getRegOP(ir.res,0,false);
//        Register result = RegisterManager.getTempReg(ir.res);
        if(ir.v1 instanceof ConstInteger && ir.v2 instanceof ConstInteger){
            ConstInteger c1 = (ConstInteger) ir.v1;
            ConstInteger c2 = (ConstInteger) ir.v2;
            int val = c1.val / c2.val;
            LoadImmCode code = new LoadImmCode(result, val);
            code.print();
        }else if(ir.v2 instanceof ConstInteger){
            Register rs = RegisterManager.getRegOP(ir.v1,1,true);
//            Register rs = RegisterManager.getTempReg(ir.v1);
            int imm = ((ConstInteger)(ir.v2)).val;
            BinaryRegImmCode code = new BinaryRegImmCode(result, rs , imm, BinaryRegImmCode.Op.DIV);
            code.print();
            RegisterManager.freeTempRegOP(rs,false);
        }else if(ir.v1 instanceof ConstInteger){
            Register rs = RegisterManager.getRegOP(ir.v1,1,true);
//            Register rs = RegisterManager.getTempReg(ir.v1);
            LoadImmCode code_1 = new LoadImmCode(rs, ((ConstInteger) ir.v1).val);
            code_1.print();
            Register rt = RegisterManager.getRegOP(ir.v2,2,true);
//            Register rt = RegisterManager.getTempReg(ir.v2);
            BinaryRegRegCode code_2 = new BinaryRegRegCode(result, rs , rt, BinaryRegRegCode.Op.DIV);
            code_2.print();
            RegisterManager.freeTempRegOP(rs,false);
            RegisterManager.freeTempRegOP(rt,false);
        }else{
            Register rs = RegisterManager.getRegOP(ir.v1,1,true);
            Register rt = RegisterManager.getRegOP(ir.v2,2,true);
//            Register rs = RegisterManager.getTempReg(ir.v1);
//            Register rt = RegisterManager.getTempReg(ir.v2);
            BinaryRegRegCode code = new BinaryRegRegCode(result, rs , rt, BinaryRegRegCode.Op.DIV);
            code.print();
            RegisterManager.freeTempRegOP(rs,false);
            RegisterManager.freeTempRegOP(rt,false);
        }
        RegisterManager.freeTempRegOP(result,true);
    }

    private static void transRET(Instruction instruction) throws IOException {
        //这意味着函数的结束，应当在此完成函数的收尾工作：将返回值放在v0寄存器，将sp回归到fp，令fp为sp处存的值，最后跳转到ra寄存器，
        RET ir = (RET)instruction;
        if(ir.value!=null) {
            MIPSCode code;
            if(ir.value instanceof ConstInteger&&((ConstInteger)ir.value).val==0){
                code = new MoveCode(RegisterManager.getZero(),RegisterManager.getReturnReg());
            }else if(ir.value instanceof ConstInteger){
                code = new LoadImmCode(RegisterManager.getReturnReg(),((ConstInteger)ir.value).val);
            }else{
                Register from = RegisterManager.getRegOP(ir.value,0,true);
//                code = new MoveCode(RegisterManager.getTempReg(ir.value),RegisterManager.getReturnReg());
                code = new MoveCode(from,RegisterManager.getReturnReg());
                RegisterManager.freeTempRegOP(from,false);
            }
            code.print();
        }
        JRCode jrCode = new JRCode(RegisterManager.getRa());
        jrCode.print();//jr $ra
    }

    private static void transMUL(Instruction instruction) throws IOException {
        MUL ir = (MUL) instruction;
        Value v1 = ir.v1;
        Value v2 = ir.v2;
        Value res = ir.res;
        Register resReg = RegisterManager.getRegOP(res,0,false);
//        Register resReg = RegisterManager.getTempReg(res);
        if(v1 instanceof ConstInteger && v2 instanceof ConstInteger){
            LoadImmCode code = new LoadImmCode(resReg,((ConstInteger) v1).val*((ConstInteger) v2).val);
            code.print();
        }else if(v1 instanceof ConstInteger){
            Register reg2 = RegisterManager.getRegOP(v2,1,true);
//            Register reg2 = RegisterManager.getTempReg(v2);
            BinaryRegImmCode code = new BinaryRegImmCode(resReg,reg2, ((ConstInteger) v1).val, BinaryRegImmCode.Op.MUL);
            code.print();
            RegisterManager.freeTempRegOP(reg2,false);
        }else if(v2 instanceof ConstInteger){
            Register reg1 = RegisterManager.getRegOP(v1,1,true);
//            Register reg1 = RegisterManager.getTempReg(v1);
            BinaryRegImmCode code = new BinaryRegImmCode(resReg,reg1, ((ConstInteger) v2).val, BinaryRegImmCode.Op.MUL);
            code.print();
            RegisterManager.freeTempRegOP(reg1,false);
        }else{
            Register reg1 = RegisterManager.getRegOP(v1,1,true);
            Register reg2 = RegisterManager.getRegOP(v2,2,true);
//            Register reg1 = RegisterManager.getTempReg(v1);
//            Register reg2 = RegisterManager.getTempReg(v2);
            BinaryRegRegCode code = new BinaryRegRegCode(resReg, reg1, reg2, BinaryRegRegCode.Op.MUL);
            code.print();
            RegisterManager.freeTempRegOP(reg1,false);
            RegisterManager.freeTempRegOP(reg2,false);
        }
        RegisterManager.freeTempRegOP(resReg,true);
    }

    private static void transLOAD(Instruction instruction) throws IOException {
        //加载全局变量、函数参数、局部变量
        LOAD ir = (LOAD)instruction;
        Register res = RegisterManager.getRegOP(ir.loadto,0,false);
//        Register res = RegisterManager.getTempReg(ir.loadto);
        if(ir.loadfrom instanceof GlobalVar){
            //全局变量，使用label,加载基地址
            if(ir.loadfrom.isArray()){
                //数组，加载地址
                LoadAddressCode code = new LoadAddressCode(res, new Address(new Label(ir.loadfrom.name.substring(1))));
                //启动地址模式
                setRegAdressModeOP(ir.loadto);
                code.print();
            }else{
                //变量，加载值
                LoadWordCode code = new LoadWordCode(res,new Address(new Label(ir.loadfrom.name.substring(1))));
                code.print();
            }
        }else{
            //局部变量
            //把全局寄存器的值或者地址移到临时寄存器
            if(AddressManager.existsArray(ir.loadfrom)){
                //局部数组
                Address address = AddressManager.getArrayAddress(ir.loadfrom);
                LoadWordCode loadWordCode = new LoadWordCode(res,address);
                loadWordCode.print();
            }
            else if(!isAddressModeOP(ir.loadfrom)){//变量模式
//                Register rs = RegisterManager.getGlobalReg(ir.loadfrom);
                Register rs = RegisterManager.getRegOP(ir.loadfrom,1,true);
                MoveCode code = new MoveCode(rs,res);
                code.print();
                RegisterManager.freeTempRegOP(rs,false);
            }else{//地址模式
//                Register rs = RegisterManager.getTempReg(ir.loadfrom);
                Register rs = RegisterManager.getRegOP(ir.loadfrom,1,true);
                LoadWordCode loadWordCode = new LoadWordCode(res,new Address(rs));
                loadWordCode.print();
                RegisterManager.freeTempRegOP(rs,false);
            }
        }
        RegisterManager.freeTempRegOP(res,true);
    }

    private static void transICMP(Instruction instruction) throws IOException {
        ICMP ir = (ICMP) instruction;
//        Register res = RegisterManager.getTempReg(ir.res);
        Register res = RegisterManager.getRegOP(ir.res,0,false);
        
        if(ir.v1 instanceof ConstInteger&&ir.v2 instanceof ConstInteger){
            int c1 = ((ConstInteger) ir.v1).val;
            int c2 = ((ConstInteger) ir.v2).val;
            switch(ir.icmpType){
                case eq:
                    if(c1==c2){
                        LoadImmCode code = new LoadImmCode(res,1);
                        code.print();
                    }else{
                        LoadImmCode code = new LoadImmCode(res,0);
                        code.print();
                    }break;
                case ne:
                    if(c1==c2){
                        LoadImmCode code = new LoadImmCode(res,0);
                        code.print();
                    }else{
                        LoadImmCode code = new LoadImmCode(res,1);
                        code.print();
                    }break;
                case sgt:
                    if(c1>c2){
                        LoadImmCode code = new LoadImmCode(res,1);
                        code.print();
                    }else{
                        LoadImmCode code = new LoadImmCode(res,0);
                        code.print();
                    }break;
                case sge:
                    if(c1>=c2){
                        LoadImmCode code = new LoadImmCode(res,1);
                        code.print();
                    }else{
                        LoadImmCode code = new LoadImmCode(res,0);
                        code.print();
                    }break;
                case slt:
                    if(c1<c2){
                        LoadImmCode code = new LoadImmCode(res,1);
                        code.print();
                    }else{
                        LoadImmCode code = new LoadImmCode(res,0);
                        code.print();
                    }break;
                case sle:
                    if(c1<=c2){
                        LoadImmCode code = new LoadImmCode(res,1);
                        code.print();
                    }else{
                        LoadImmCode code = new LoadImmCode(res,0);
                        code.print();
                    }break;
            }
        }
        else{

            if(ir.v2 instanceof ConstInteger){
//                Register rs = RegisterManager.getTempReg(ir.v1);
                Register rs = RegisterManager.getRegOP(ir.v1,1,true);
                int imm  = ((ConstInteger)ir.v2).val;
                switch (ir.icmpType) {
                    case eq -> {
                        BinaryRegImmCode code = new BinaryRegImmCode(res, rs, imm, BinaryRegImmCode.Op.SEQ);
                        code.print();
                        break;
                    }
                    case ne -> {
                        BinaryRegImmCode code = new BinaryRegImmCode(res, rs, imm, BinaryRegImmCode.Op.SNE);
                        code.print();
                    }
                    case sgt -> {
                        BinaryRegImmCode code = new BinaryRegImmCode(res, rs, imm, BinaryRegImmCode.Op.SGT);
                        code.print();
                    }
                    case sge -> {
                        BinaryRegImmCode code = new BinaryRegImmCode(res, rs, imm, BinaryRegImmCode.Op.SGE);
                        code.print();
                    }
                    case slt -> {
                        BinaryRegImmCode code = new BinaryRegImmCode(res, rs, imm, BinaryRegImmCode.Op.SLTI);
                        code.print();
                    }
                    case sle -> {
                        BinaryRegImmCode code = new BinaryRegImmCode(res, rs, imm, BinaryRegImmCode.Op.SLE);
                        code.print();
                    }
                }
                RegisterManager.freeTempRegOP(rs,false);
            }else if(ir.v1 instanceof ConstInteger){
//                Register rs = RegisterManager.getTempReg(ir.v2);
                Register rs = RegisterManager.getRegOP(ir.v2,1,true);
                int imm  = ((ConstInteger)ir.v1).val;
                switch (ir.icmpType) {
                    case eq -> {
                        BinaryRegImmCode code = new BinaryRegImmCode(res, rs, imm, BinaryRegImmCode.Op.SEQ);
                        code.print();
                        break;
                    }
                    case ne -> {
                        BinaryRegImmCode code = new BinaryRegImmCode(res, rs, imm, BinaryRegImmCode.Op.SNE);
                        code.print();
                    }
                    case sgt -> {
                        BinaryRegImmCode code = new BinaryRegImmCode(res, rs, imm, BinaryRegImmCode.Op.SLTI);
                        code.print();
                    }
                    case sge -> {
                        BinaryRegImmCode code = new BinaryRegImmCode(res, rs, imm, BinaryRegImmCode.Op.SLE);
                        code.print();
                    }
                    case slt -> {
                        BinaryRegImmCode code = new BinaryRegImmCode(res, rs, imm, BinaryRegImmCode.Op.SGT);
                        code.print();
                    }
                    case sle -> {
                        BinaryRegImmCode code = new BinaryRegImmCode(res, rs, imm, BinaryRegImmCode.Op.SGE);
                        code.print();
                    }
                }
                RegisterManager.freeTempRegOP(rs,false);
            } else{
//                Register rs = RegisterManager.getTempReg(ir.v1);
//                Register rt = RegisterManager.getTempReg(ir.v2);
                Register rs = RegisterManager.getRegOP(ir.v1,1,true);
                Register rt = RegisterManager.getRegOP(ir.v2,2,true);
                switch (ir.icmpType) {
                    case eq -> {
                        BinaryRegRegCode code = new BinaryRegRegCode(res, rs, rt, BinaryRegRegCode.Op.SEQ);
                        code.print();
                    }
                    case ne -> {
                        BinaryRegRegCode code = new BinaryRegRegCode(res, rs, rt, BinaryRegRegCode.Op.SNE);
                        code.print();
                    }
                    case sgt -> {
                        BinaryRegRegCode code = new BinaryRegRegCode(res, rs, rt, BinaryRegRegCode.Op.SGT);
                        code.print();
                    }
                    case sge -> {
                        BinaryRegRegCode code = new BinaryRegRegCode(res, rs, rt, BinaryRegRegCode.Op.SGE);
                        code.print();
                    }
                    case slt -> {
                        BinaryRegRegCode code = new BinaryRegRegCode(res, rs, rt, BinaryRegRegCode.Op.SLT);
                        code.print();
                    }
                    case sle -> {
                        BinaryRegRegCode code = new BinaryRegRegCode(res, rs, rt, BinaryRegRegCode.Op.SLE);
                        code.print();
                    }
                }
                RegisterManager.freeTempRegOP(rs,false);
                RegisterManager.freeTempRegOP(rt,false);
            }
        }
        RegisterManager.freeTempRegOP(res,true);
    }

    //FIXME:数组处理
    private static void transGETELEMENTPTR(Instruction instruction) throws IOException {
        GETELEMENTPTR ir = (GETELEMENTPTR) instruction;
        setRegAdressModeOP(ir.res);//无论如何，得到的都是一个地址了
        if(ir.type==0)//%2 = getelementptr i32, i32* %1, i32 0, i32 1
        {
            if(!(ir.array instanceof GlobalVar))//局部数组
            {
                ValueType type = ((ArrayType)ir.array.type.pointto).elementType;
                Address address;
                if(AddressManager.existsArray(ir.array)){
                    address = AddressManager.getArrayAddress(ir.array);
                }else{
                    Register reg = RegisterManager.getRegOP(ir.array,0,true);//得到储存地址的寄存器
//                    address = new Address(RegisterManager.getTempReg(ir.array));
                    address = new Address(reg);
                    RegisterManager.freeTempRegOP(reg,false);
                }
                if(!(type instanceof ArrayType)){//元素不是数组
                    if(ir.offset instanceof ConstInteger){
                        Register target =  RegisterManager.getRegOP(ir.res,1,false);
//                        Register target = RegisterManager.getTempReg(ir.res);
                        LoadAddressCode loadAddressCode = new LoadAddressCode(target,address);
                        loadAddressCode.print();
                        BinaryRegImmCode code = new BinaryRegImmCode(target,target,4*((ConstInteger)ir.offset).val, BinaryRegImmCode.Op.ADDIU);
                        code.print();
                        RegisterManager.freeTempRegOP(target,true);
                    }else{
                        Register target =  RegisterManager.getRegOP(ir.res,1,false);
//                        Register target = RegisterManager.getTempReg(ir.res);
                        LoadAddressCode loadAddressCode = new LoadAddressCode(target,address);
                        loadAddressCode.print();
//                        Register offsetReg = RegisterManager.getTempReg(ir.offset);
                        Register offsetReg = RegisterManager.getRegOP(ir.offset,2,true);
                        BinaryRegRegCode code = new BinaryRegRegCode(target,target,offsetReg, BinaryRegRegCode.Op.ADDU);
                        code.print();
                        code.print();
                        code.print();
                        code.print();
                        RegisterManager.freeTempRegOP(target,true);
                        RegisterManager.freeTempRegOP(offsetReg,false);
                    }
                }else{//元素是数组
                    if(ir.offset instanceof ConstInteger){
//                        Register target = RegisterManager.getTempReg(ir.res);
                        Register target =  RegisterManager.getRegOP(ir.res,1,false);
                        LoadAddressCode loadAddressCode = new LoadAddressCode(target,address);
                        loadAddressCode.print();
                        Integer sublength = ((ArrayType) type).length;
                        BinaryRegImmCode code = new BinaryRegImmCode(target,target,4*((ConstInteger)ir.offset).val*sublength, BinaryRegImmCode.Op.ADDIU);
                        code.print();
                        RegisterManager.freeTempRegOP(target,true);
                    }else{
//                        Register target = RegisterManager.getTempReg(ir.res);
                        Register target =  RegisterManager.getRegOP(ir.res,1,false);
                        LoadAddressCode loadAddressCode = new LoadAddressCode(target,address);
                        loadAddressCode.print();
                        Register offsetReg = RegisterManager.getRegOP(ir.offset,2,true);
//                        Register offsetReg = RegisterManager.getTempReg(ir.offset);
                        Integer sublength = ((ArrayType) type).length;
                        Register realOffsetReg = Register.$a0;
                        BinaryRegImmCode code_1 = new BinaryRegImmCode(realOffsetReg,offsetReg,4*sublength, BinaryRegImmCode.Op.MUL);
                        code_1.print();
                        BinaryRegRegCode code_2 = new BinaryRegRegCode(target,target,realOffsetReg, BinaryRegRegCode.Op.ADDU);
                        code_2.print();
//                        RegisterManager.freeReg(realOffsetReg);
                        RegisterManager.freeTempRegOP(target,true);
                        RegisterManager.freeTempRegOP(offsetReg,false);
                    }
                }
            }else{//全局数组
                ValueType type = ((ArrayType)ir.array.type.pointto).elementType;
                if(!(type instanceof ArrayType)){
                    if(ir.offset instanceof ConstInteger){
                        Register target = RegisterManager.getRegOP(ir.res,0,false);
//                        Register target = RegisterManager.getTempReg(ir.res);
                        int offsetAsInt = ((ConstInteger)ir.offset).val*4;
                        Label label = new Label(ir.array.name.substring(1));
                        LoadAddressCode loadAddressCode = new LoadAddressCode(target, new Address(label,offsetAsInt));
                        loadAddressCode.print();
                        RegisterManager.freeTempRegOP(target,true);
                    }else{
                        Register target = RegisterManager.getRegOP(ir.res,0,false);
//                        Register target = RegisterManager.getTempReg(ir.res);
                        Label label = new Label(ir.array.name.substring(1));
                        Register offsetReg = RegisterManager.getRegOP(ir.offset,1,true);
//                        Register offsetReg = RegisterManager.getTempReg(ir.offset);
                        LoadAddressCode loadAddressCode = new LoadAddressCode(target, new Address(offsetReg,label));
                        loadAddressCode.print();
                        BinaryRegRegCode binaryRegRegCode = new BinaryRegRegCode(target,target,offsetReg, BinaryRegRegCode.Op.ADDU);
                        binaryRegRegCode.print();
                        binaryRegRegCode.print();
                        binaryRegRegCode.print();
                        RegisterManager.freeTempRegOP(target,true);
                        RegisterManager.freeTempRegOP(offsetReg,false);
                    }
                }else{
                    if(ir.offset instanceof ConstInteger){
//                        Register target = RegisterManager.getTempReg(ir.res);
                        Register target = RegisterManager.getRegOP(ir.res,0,false);
                        Label label = new Label(ir.array.name.substring(1));
                        int offsetAsInt = ((ConstInteger)ir.offset).val*4;
                        Integer sublength = ((ArrayType) type).length;
                        LoadAddressCode loadAddressCode = new LoadAddressCode(target, new Address(label,offsetAsInt*sublength));
                        loadAddressCode.print();
                        RegisterManager.freeTempRegOP(target,true);
                    }else{
//                        Register target = RegisterManager.getTempReg(ir.res);
                        Register target = RegisterManager.getRegOP(ir.res,0,false);
                        Label label = new Label(ir.array.name.substring(1));
//                        Register offsetReg = RegisterManager.getTempReg(ir.offset);
                        Register offsetReg = RegisterManager.getRegOP(ir.offset,1,true);
                        Integer sublength = ((ArrayType) type).length;
                        Register realOffsetReg = RegisterManager.getTempApplicationRegOP(2);
//                        Register realOffsetReg = RegisterManager.getTempReg(new Value("temp",ValueType._i32));
                        LoadAddressCode loadAddressCode = new LoadAddressCode(target, new Address(label));
                        loadAddressCode.print();
                        BinaryRegImmCode code_1 = new BinaryRegImmCode(realOffsetReg,offsetReg,4*sublength, BinaryRegImmCode.Op.MUL);
                        code_1.print();
                        LoadAddressCode code_2 = new LoadAddressCode(target, new Address(realOffsetReg,label));
                        code_2.print();
//                        RegisterManager.freeReg(realOffsetReg);
                        RegisterManager.freeTempRegOP(target,true);
                        RegisterManager.freeTempRegOP(offsetReg,false);
                        RegisterManager.freeTempRegOP(realOffsetReg,true);
                    }
                }
            }
        }else{
            //%2 = getelementptr i32, *i32 %1, i32 1
            ValueType type = ir.array.type.pointto;
            if(!(type instanceof ArrayType)){
                if(ir.offset instanceof ConstInteger){
                    Register target =RegisterManager.getRegOP(ir.res,0,false);
//                    Register target = RegisterManager.getTempReg(ir.res);
//                    Register rs = RegisterManager.getTempReg(ir.array);
                    Register rs =RegisterManager.getRegOP(ir.array,1,true);
                    BinaryRegImmCode code = new BinaryRegImmCode(target,rs,4*((ConstInteger)ir.offset).val, BinaryRegImmCode.Op.ADDIU);
                    code.print();
                    RegisterManager.freeTempRegOP(target,true);
                    RegisterManager.freeTempRegOP(rs,false);
                }else{
                    Register target =RegisterManager.getRegOP(ir.res,0,false);
                    Register rs =RegisterManager.getRegOP(ir.array,1,true);
                    Register offsetReg =RegisterManager.getRegOP(ir.offset,2,true);
//                    Register target = RegisterManager.getTempReg(ir.res);
//                    Register rs = RegisterManager.getTempReg(ir.array);
//                    Register offsetReg = RegisterManager.getTempReg(ir.offset);
                    BinaryRegRegCode code_1 = new BinaryRegRegCode(target,rs,offsetReg, BinaryRegRegCode.Op.ADDU);
                    code_1.print();
                    BinaryRegRegCode code_2 = new BinaryRegRegCode(target,target,offsetReg, BinaryRegRegCode.Op.ADDU);
                    code_2.print();
                    code_2.print();
                    code_2.print();
                    RegisterManager.freeTempRegOP(target,true);
                    RegisterManager.freeTempRegOP(rs,false);
                    RegisterManager.freeTempRegOP(offsetReg,false);
                }
            }else{
                if(ir.offset instanceof ConstInteger){
                    Register target = RegisterManager.getRegOP(ir.res,0,false);
                    Register rs = RegisterManager.getRegOP(ir.array,1,true);
//                    Register target = RegisterManager.getTempReg(ir.res);
//                    Register rs = RegisterManager.getTempReg(ir.array);
                    Integer sublength = ((ArrayType) type).length;
                    BinaryRegImmCode code = new BinaryRegImmCode(target,rs,4*((ConstInteger)ir.offset).val*sublength, BinaryRegImmCode.Op.ADDIU);
                    code.print();
                    RegisterManager.freeTempRegOP(target,true);
                    RegisterManager.freeTempRegOP(rs,false);
                }else{
                    Register target = RegisterManager.getRegOP(ir.res,0,false);
                    Register rs = RegisterManager.getRegOP(ir.array,1,true);
                    Register offsetReg = RegisterManager.getRegOP(ir.offset,2,true);
//                    Register target = RegisterManager.getTempReg(ir.res);
//                    Register rs = RegisterManager.getTempReg(ir.array);
//                    Register offsetReg = RegisterManager.getTempReg(ir.offset);
                    Integer sublength = ((ArrayType) type).length;
                    Register realOffsetReg = Register.$a0;
                    BinaryRegImmCode code_1 = new BinaryRegImmCode(realOffsetReg,offsetReg,4*sublength, BinaryRegImmCode.Op.MUL);
                    code_1.print();
                    BinaryRegRegCode code_2 = new BinaryRegRegCode(target,rs,realOffsetReg, BinaryRegRegCode.Op.ADDU);
                    code_2.print();
//                    RegisterManager.freeReg(realOffsetReg);
                    RegisterManager.freeTempRegOP(target,true);
                    RegisterManager.freeTempRegOP(rs,false);
                    RegisterManager.freeTempRegOP(offsetReg,false);
                }
            }
        }
    }

    //FIXME:函数调用
    private static void transCALL(Instruction instruction) throws IOException {

        CALL ir = (CALL) instruction;
        if(ir.name.equals("putint")){
            if(ir.params.get(0) instanceof ConstInteger)
            {
                LoadImmCode loadImmCode = new LoadImmCode(RegisterManager.get$a0(),((ConstInteger) ir.params.get(0)).val);
                loadImmCode.print();
            }else{
                Register reg = RegisterManager.getRegOP(ir.params.get(0),0,true);
//                Register reg = RegisterManager.getTempReg(ir.params.get(0));
                MoveCode moveCode = new MoveCode(reg, RegisterManager.get$a0());
                moveCode.print();
                RegisterManager.freeTempRegOP(reg,false);
            }
            LoadImmCode loadImmCode = new LoadImmCode(RegisterManager.getReturnReg(),1);
            loadImmCode.print();
            SyscallCode syscallCode = new SyscallCode();
            syscallCode.print();
            return;
        }else if(ir.name.equals("getint")){
            LoadImmCode loadImmCode = new LoadImmCode(RegisterManager.getReturnReg(),5);
            loadImmCode.print();
            SyscallCode syscallCode = new SyscallCode();
            syscallCode.print();
            Register reg = RegisterManager.getRegOP(ir.loadto,0,false);
//            Register reg = RegisterManager.getTempReg(ir.loadto);
            MoveCode moveCode = new MoveCode(RegisterManager.getReturnReg(),reg);
            moveCode.print();
            RegisterManager.freeTempRegOP(reg,true);
            return;
        }else if(ir.name.equals("putch")){
            LoadImmCode loadImmCode = new LoadImmCode(RegisterManager.get$a0(), ((ConstInteger)ir.params.get(0)).val);
            loadImmCode.print();
            LoadImmCode setOutput = new LoadImmCode(RegisterManager.getReturnReg(),11);
            setOutput.print();
            SyscallCode syscallCode = new SyscallCode();
            syscallCode.print();
            return;
        }

        //1.存储全局寄存器和临时寄存器的值，还有$ra
//        RegisterManager.stageRegs();
        RegisterManager.stageRegsOP();



        // 2.存储参数
        for(int i=ir.params.size()-1;i>3;i--){
            Value value = ir.params.get(i);
            AddressManager.addSP();
            if(value instanceof ConstInteger){
                Register tempParamReg = RegisterManager.get$a0();
                LoadImmCode loadImmCode = new LoadImmCode(tempParamReg, ((ConstInteger) value).val);
                loadImmCode.print();
                StoreWordCode storeWordCode = new StoreWordCode(tempParamReg,AddressManager.getSPAddress());
                storeWordCode.print();
            }else{
                Register temp = RegisterManager.getRegOP(value,1,true);//0号给返回值用
                StoreWordCode storeWordCode = new StoreWordCode(temp,AddressManager.getSPAddress());
                storeWordCode.print();
                RegisterManager.freeTempRegOP(temp,false);
//                if(RegisterManager.containsTempValue(value)){
//                    StoreWordCode storeWordCode = new StoreWordCode(RegisterManager.getTempReg(value),AddressManager.getSPAddress());
//                    storeWordCode.print();
//                }else if(value instanceof VarPointer&&RegisterManager.containsGlobalValue(value)){
//                    StoreWordCode storeWordCode = new StoreWordCode(RegisterManager.getGlobalReg((VarPointer) value),AddressManager.getSPAddress());
//                    storeWordCode.print();
//                }else{
//                    Address address = AddressManager.find(value);
//                    Register tempParamReg = RegisterManager.get$a0();
//                    assert address!=null;
//                    LoadWordCode loadWordCode = new LoadWordCode(tempParamReg,address);
//                    loadWordCode.print();
//                    StoreWordCode storeWordCode = new StoreWordCode(tempParamReg,AddressManager.getSPAddress());
//                    storeWordCode.print();
//                }
            }
        }

        for(int i=0;i<ir.params.size()&&i<4;i++){
            Value value = ir.params.get(i);

            if(value instanceof ConstInteger){
                LoadImmCode loadImmCode = new LoadImmCode(RegisterManager.getParamReg(i),((ConstInteger) value).val);
                loadImmCode.print();
            }else{
                Register temp = RegisterManager.getRegOP(value,1,true);//0号给返回值用
                MoveCode moveCode = new MoveCode(temp,RegisterManager.getParamReg(i));
                moveCode.print();
                RegisterManager.freeTempRegOP(temp,false);
//                if(RegisterManager.containsTempValue(value)){
//                    MoveCode moveCode = new MoveCode(RegisterManager.getTempReg(value),RegisterManager.getParamReg(i));
//                    moveCode.print();
//                }else if(value instanceof VarPointer&&RegisterManager.containsGlobalValue(value)){
//                    MoveCode moveCode = new MoveCode(RegisterManager.getGlobalReg((VarPointer) value),RegisterManager.getParamReg(i));
//                    moveCode.print();
//                }else{
//                    Register target = RegisterManager.getParamReg(i);
//                    Address address = AddressManager.find(value);
//                    assert address!=null;
//                    LoadWordCode loadWordCode = new LoadWordCode(target,address);
//                    loadWordCode.print();
//                }
            }
        }

        //3.存储并切换帧指针和栈指针
        AddressManager.setFPtoSP();

        //4.调用函数
        JALCode jalCode = new JALCode(new Label(ir.name));
        jalCode.print();

        //5.恢复栈指针和帧指针
        AddressManager.setSPtoFP();

        //6.去掉参数占用的位置
        for(int i=4;i<ir.params.size();i++)
            AddressManager.subSP();

        //7.恢复寄存器状态
//        RegisterManager.restoreRegs();
        RegisterManager.restoreRegsOP();

        //8.加载返回值
        if(ir.loadto!=null){
            Register to = RegisterManager.getRegOP(ir.loadto,0,false);
            MoveCode moveReturnVal = new MoveCode(RegisterManager.getReturnReg(),to);
//            MoveCode moveReturnVal = new MoveCode(RegisterManager.getReturnReg(),RegisterManager.getTempReg(ir.loadto));
            moveReturnVal.print();
            RegisterManager.freeTempRegOP(to,true);
        }


    }

    private static void transBR(Instruction instruction) throws IOException {
        BR ir = (BR) instruction;
        if(ir.label_dst!=null){
            //无条件跳转
            if(!Config.mips_optimize){
                JumpCode jumpCode = new JumpCode(new Label(curFunction.name+"_"+ir.label_dst.substring(1)));
                jumpCode.print();
            }else{
                List<BasicBlock> nextblock = ir.getNextBlock();
                if(ir.basicBlock.function.basicBlocks.indexOf(ir.basicBlock)+1==ir.basicBlock.function.basicBlocks.indexOf(nextblock.get(0))){
                    return;
                }else{
                    JumpCode jumpCode = new JumpCode(new Label(curFunction.name+"_"+ir.label_dst.substring(1)));
                    jumpCode.print();
                }
            }
        }else{
            //判断条件后跳转
//            Register reg = RegisterManager.getTempReg(ir.cond);
            Register reg = RegisterManager.getRegOP(ir.cond,0,true);
            if(!Config.mips_optimize){
                BrCondCode brCondCode = new BrCondCode(reg, new Label(curFunction.name+"_"+ir.label1.substring(1)), new Label(curFunction.name+"_"+ir.label2.substring(1)));
                brCondCode.print();
            }else{
                List<BasicBlock> nextBlocks=ir.getNextBlock();
                if(ir.basicBlock.function.basicBlocks.indexOf(ir.basicBlock)+1==ir.basicBlock.function.basicBlocks.indexOf(nextBlocks.get(0)))
                {
                    BrCondCode brCondCode = new BrCondCode(reg, null, new Label(curFunction.name+"_"+ir.label2.substring(1)));
                    brCondCode.print();
                }else if(ir.basicBlock.function.basicBlocks.indexOf(ir.basicBlock)+1==ir.basicBlock.function.basicBlocks.indexOf(nextBlocks.get(1))){
                    BrCondCode brCondCode = new BrCondCode(reg, new Label(curFunction.name+"_"+ir.label1.substring(1)), null);
                    brCondCode.print();
                }else{
                    BrCondCode brCondCode = new BrCondCode(reg, new Label(curFunction.name+"_"+ir.label1.substring(1)), new Label(curFunction.name+"_"+ir.label2.substring(1)));
                    brCondCode.print();
                }
            }
            RegisterManager.freeTempRegOP(reg,false);
        }
    }

    private static void transALLOCA(Instruction instruction) throws IOException {
        ALLOCA ir = (ALLOCA) instruction;
        if(ir.pointer.type.pointto instanceof ArrayType){
            ArrayType arrayType =(ArrayType) ir.pointer.type.pointto;
            for(int i=0;i< arrayType.length;i++){
                if(arrayType.elementType instanceof ArrayType){
                    for(int j=0;j< ((ArrayType) arrayType.elementType).length;j++){
                        AddressManager.addSP();
                    }
                }else {
                    AddressManager.addSP();
                }
            }
            //很关键，为局部数组标定了对应的地址
            AddressManager.storeArray(ir.pointer, AddressManager.getSPAddress());
        }
        //普通变量
//        else
//            RegisterManager.getRegOP(ir.pointer,0,)
//            RegisterManager.getGlobalReg(ir.pointer);
    }

    private static void transADD(Instruction instruction) throws IOException {
        ADD ir = (ADD) instruction;
        Value v1 = ir.v1;
        Value v2 = ir.v2;
        Value res = ir.res;
        Register resReg = RegisterManager.getRegOP(res,0,false);
//        Register resReg = RegisterManager.getTempReg(res);
        if(v1 instanceof ConstInteger && v2 instanceof ConstInteger){
            LoadImmCode code = new LoadImmCode(resReg,((ConstInteger) v1).val+((ConstInteger) v2).val);
            code.print();
        }else if(v1 instanceof ConstInteger){
//            Register reg2 = RegisterManager.getTempReg(v2);
            Register reg2 = RegisterManager.getRegOP(v2,1,true);
            BinaryRegImmCode code = new BinaryRegImmCode(resReg,reg2, ((ConstInteger) v1).val, BinaryRegImmCode.Op.ADDIU);
            code.print();
            RegisterManager.freeTempRegOP(reg2,false);
        }else if(v2 instanceof ConstInteger){
//            Register reg1 = RegisterManager.getTempReg(v1);
            Register reg1 = RegisterManager.getRegOP(v1,1,true);
            BinaryRegImmCode code = new BinaryRegImmCode(resReg,reg1, ((ConstInteger) v2).val, BinaryRegImmCode.Op.ADDIU);
            code.print();
            RegisterManager.freeTempRegOP(reg1,false);
        }else{
            Register reg1 = RegisterManager.getRegOP(v1,1,true);
            Register reg2 = RegisterManager.getRegOP(v2,2,true);
//            Register reg1 = RegisterManager.getTempReg(v1);
//            Register reg2 = RegisterManager.getTempReg(v2);
            BinaryRegRegCode code = new BinaryRegRegCode(resReg, reg1, reg2, BinaryRegRegCode.Op.ADDU);
            code.print();
            RegisterManager.freeTempRegOP(reg1,false);
            RegisterManager.freeTempRegOP(reg2,false);
        }
        RegisterManager.freeTempRegOP(resReg,true);
    }

    static Set<Value> addressMode=new HashSet<>();//负责存储地址的变量
    public static void setRegAdressModeOP(Value value){
        addressMode.add(value);
    }
    public static boolean isAddressModeOP(Value value){
        return addressMode.contains(value);
    }
}
