package MIPS;

import IR.Array.Array;
import IR.Array.ArrayType;
import IR.IRModule;
import IR.Type.ValueType;
import IR.Value.*;
import IR.Value.Instructions.*;
import IR.Visitor;
import MIPS.Code.*;
import utils.Boom;
import utils.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MIPSGenerator {

//    public static List<MIPSCode> codes = new ArrayList<>();
    static Function curFunction;
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
                RegisterManager.freeAllRegs();//释放全部的全局寄存器和临时寄存器
                AddressManager.setOffsetZero();//将SP与FP差值置0
                AddressManager.clearArray();//清理所有的局部数组
                RegisterManager.clearGlobalValues();
                AddressManager.clearAddressOfValue();
                for(int i=0;i<4&&i<function.arguments.size();i++){
                    Register target = RegisterManager.getTempReg(function.arguments.get(i));
                    MoveCode moveCode = new MoveCode(RegisterManager.getParamReg(i),target);
                    moveCode.print();
                }
                for(int i=4;i<function.arguments.size();i++){
                    Register target = RegisterManager.getTempReg(function.arguments.get(i));
                    LoadWordCode loadWordCode = new LoadWordCode(target,new Address(RegisterManager.getFp(),4*(i-3)));
                    loadWordCode.print();
//                    AddressManager.subSP();
                }
            }
            int i=0;
            for(BasicBlock basicBlock:function.basicBlocks){
                if(i!=0) IOUtils.write(function.name+"_"+basicBlock.name.substring(1)+":\n");

                //到新的基本块就清除所有的临时寄存器（教材说的）
                if(i!=0)RegisterManager.freeTempRegs();
                i++;
                for(Instruction instruction:basicBlock.instructions){
                    translate(instruction);
                }
            }
        }
    }

    private static void translate(Instruction instruction) throws IOException {
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
    }

    private static void transZEXTTO(Instruction instruction) throws IOException {
        ZEXTTO ir = (ZEXTTO) instruction;
        Register rs = RegisterManager.getTempReg(ir.zextfrom);
        Register rt = RegisterManager.getTempReg(ir.zextto);
        MoveCode code = new MoveCode(rs,rt);
        code.print();
    }

    private static void transSUB(Instruction instruction) throws IOException {
        SUB ir = (SUB) instruction;
        Register result = RegisterManager.getTempReg(ir.res);
        if(ir.v1 instanceof ConstInteger && ir.v2 instanceof ConstInteger){
            ConstInteger c1 = (ConstInteger) ir.v1;
            ConstInteger c2 = (ConstInteger) ir.v2;
            int val = c1.val - c2.val;
            LoadImmCode code = new LoadImmCode(result, val);
            code.print();
        }else if(ir.v2 instanceof ConstInteger){
            Register rs = RegisterManager.getTempReg(ir.v1);
            int imm = ((ConstInteger)(ir.v2)).val;
            BinaryRegImmCode code = new BinaryRegImmCode(result, rs , imm, BinaryRegImmCode.Op.SUBI);

            code.print();
        }else if(ir.v1 instanceof ConstInteger){
            Register rs = RegisterManager.getTempReg(ir.v1);
            LoadImmCode code_1 = new LoadImmCode(rs, ((ConstInteger) ir.v1).val);
            code_1.print();
            Register rt = RegisterManager.getTempReg(ir.v2);
            BinaryRegRegCode code_2 = new BinaryRegRegCode(result, rs , rt, BinaryRegRegCode.Op.SUB);
            code_2.print();
        }else{
            Register rs = RegisterManager.getTempReg(ir.v1);
            Register rt = RegisterManager.getTempReg(ir.v2);
            BinaryRegRegCode code = new BinaryRegRegCode(result, rs , rt, BinaryRegRegCode.Op.SUB);
            code.print();
        }
    }

    private static void transSTORE(Instruction instruction) throws IOException {
        STORE ir = (STORE)instruction;

        if(ir.v2 instanceof GlobalVar){
            //全局变量必须直接存
            Register rs = RegisterManager.getTempReg(ir.v1);
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
        }else{
            if(RegisterManager.isGlobalValue(ir.v2)){
                Register rt = RegisterManager.getGlobalReg(ir.v2);
                if(ir.v1 instanceof ConstInteger){
                    if(((ConstInteger) ir.v1).val!=0) {
                        LoadImmCode loadImmCode = new LoadImmCode(rt, ((ConstInteger) ir.v1).val);
                        loadImmCode.print();
                    } else{
                        MoveCode moveCode = new MoveCode(RegisterManager.getZero(),rt);
                        moveCode.print();
                    }
                }else{
                    Register rs = RegisterManager.getTempReg(ir.v1);
                    MoveCode moveCode = new MoveCode(rs,rt);
                    moveCode.print();
                }
            }else{
                Register rt = RegisterManager.getTempReg(ir.v2);
                if(ir.v1 instanceof ConstInteger){
                    if(((ConstInteger) ir.v1).val!=0) {
                        Register rs = RegisterManager.getTempReg(ir.v1);
                        LoadImmCode loadImmCode = new LoadImmCode(rs,((ConstInteger) ir.v1).val);
                        loadImmCode.print();
                        StoreWordCode storeWordCode = new StoreWordCode(rs,new Address(rt));
                        storeWordCode.print();
                        RegisterManager.freeReg(rs);
                    } else{
                        Register rs = RegisterManager.getZero();
                        StoreWordCode storeWordCode = new StoreWordCode(rs,new Address(rt));
                        storeWordCode.print();
                    }
                }else{
                    Register rs = RegisterManager.getTempReg(ir.v1);
                    StoreWordCode storeWordCode = new StoreWordCode(rs,new Address(rt));
                    storeWordCode.print();
                }
            }
        }
    }

    private static void transSREM(Instruction instruction) throws IOException {
        SREM ir = (SREM) instruction;
        Register result = RegisterManager.getTempReg(ir.res);
        if(ir.v1 instanceof ConstInteger && ir.v2 instanceof ConstInteger){
            ConstInteger c1 = (ConstInteger) ir.v1;
            ConstInteger c2 = (ConstInteger) ir.v2;
            int val = c1.val % c2.val;
            LoadImmCode code = new LoadImmCode(result, val);
            code.print();
        }else if(ir.v2 instanceof ConstInteger){
            Register rs = RegisterManager.getTempReg(ir.v1);
            Register rt = RegisterManager.getTempReg(ir.v2);
            int imm = ((ConstInteger)(ir.v2)).val;
            LoadImmCode loadImmCode = new LoadImmCode(rt,imm);
            loadImmCode.print();
            DIVCode divCode = new DIVCode(rs,rt);
            divCode.print();
            MFHICode mfhiCode = new MFHICode(result);
            mfhiCode.print();
        }else if(ir.v1 instanceof ConstInteger){
            Register rs = RegisterManager.getTempReg(ir.v1);
            Register rt = RegisterManager.getTempReg(ir.v2);
            int imm = ((ConstInteger)(ir.v1)).val;
            LoadImmCode loadImmCode = new LoadImmCode(rs,imm);
            loadImmCode.print();
            DIVCode divCode = new DIVCode(rs,rt);
            divCode.print();
            MFHICode mfhiCode = new MFHICode(result);
            mfhiCode.print();
        }else{
            Register rs = RegisterManager.getTempReg(ir.v1);
            Register rt = RegisterManager.getTempReg(ir.v2);
            DIVCode divCode = new DIVCode(rs,rt);
            divCode.print();
            MFHICode mfhiCode = new MFHICode(result);
            mfhiCode.print();
        }
    }

    private static void transSDIV(Instruction instruction) throws IOException {
        SDIV ir = (SDIV) instruction;
        Register result = RegisterManager.getTempReg(ir.res);
        if(ir.v1 instanceof ConstInteger && ir.v2 instanceof ConstInteger){
            ConstInteger c1 = (ConstInteger) ir.v1;
            ConstInteger c2 = (ConstInteger) ir.v2;
            int val = c1.val / c2.val;
            LoadImmCode code = new LoadImmCode(result, val);
            code.print();
        }else if(ir.v2 instanceof ConstInteger){
            Register rs = RegisterManager.getTempReg(ir.v1);
            int imm = ((ConstInteger)(ir.v2)).val;
            BinaryRegImmCode code = new BinaryRegImmCode(result, rs , imm, BinaryRegImmCode.Op.DIV);

            code.print();
        }else if(ir.v1 instanceof ConstInteger){
            Register rs = RegisterManager.getTempReg(ir.v1);
            LoadImmCode code_1 = new LoadImmCode(rs, ((ConstInteger) ir.v1).val);
            code_1.print();
            Register rt = RegisterManager.getTempReg(ir.v2);
            BinaryRegRegCode code_2 = new BinaryRegRegCode(result, rs , rt, BinaryRegRegCode.Op.DIV);
            code_2.print();
        }else{
            Register rs = RegisterManager.getTempReg(ir.v1);
            Register rt = RegisterManager.getTempReg(ir.v2);
            BinaryRegRegCode code = new BinaryRegRegCode(result, rs , rt, BinaryRegRegCode.Op.DIV);
            code.print();
        }
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
                code = new MoveCode(RegisterManager.getTempReg(ir.value),RegisterManager.getReturnReg());
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
        Register resReg = RegisterManager.getTempReg(res);
        if(v1 instanceof ConstInteger && v2 instanceof ConstInteger){
            LoadImmCode code = new LoadImmCode(resReg,((ConstInteger) v1).val*((ConstInteger) v2).val);
            code.print();
        }else if(v1 instanceof ConstInteger){
            Register reg2 = RegisterManager.getTempReg(v2);
            BinaryRegImmCode code = new BinaryRegImmCode(resReg,reg2, ((ConstInteger) v1).val, BinaryRegImmCode.Op.MULO);
            code.print();
        }else if(v2 instanceof ConstInteger){
            Register reg1 = RegisterManager.getTempReg(v1);
            BinaryRegImmCode code = new BinaryRegImmCode(resReg,reg1, ((ConstInteger) v2).val, BinaryRegImmCode.Op.MULO);
            code.print();
        }else{
            Register reg1 = RegisterManager.getTempReg(v1);
            Register reg2 = RegisterManager.getTempReg(v2);
            BinaryRegRegCode code = new BinaryRegRegCode(resReg, reg1, reg2, BinaryRegRegCode.Op.MUL);
            code.print();
        }
    }

    private static void transLOAD(Instruction instruction) throws IOException {
        //加载全局变量、函数参数、局部变量
        LOAD ir = (LOAD)instruction;
        Register res = RegisterManager.getTempReg(ir.loadto);
        if(ir.loadfrom instanceof GlobalVar){
            //全局变量，使用label,加载中基地址
            if(ir.loadfrom.isArray()){
                //数组，加载地址
                LoadAddressCode code = new LoadAddressCode(res, new Address(new Label(ir.loadfrom.name.substring(1))));
                code.print();
            }else{
                //变量，加载值
                LoadWordCode code = new LoadWordCode(res,new Address(new Label(ir.loadfrom.name.substring(1))));
                code.print();
            }
        }else{
            //局部变量
            //把全局寄存器的值或者地址移到临时寄存器
            if(RegisterManager.isGlobalValue(ir.loadfrom)){
                Register rs = RegisterManager.getGlobalReg(ir.loadfrom);
                MoveCode code = new MoveCode(rs,res);
                code.print();
            }else{
                Register rs = RegisterManager.getTempReg(ir.loadfrom);
                LoadWordCode loadWordCode = new LoadWordCode(res,new Address(rs));
                loadWordCode.print();
            }
        }
    }

    private static void transICMP(Instruction instruction) throws IOException {
        ICMP ir = (ICMP) instruction;
        Register res = RegisterManager.getTempReg(ir.res);
        
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
                Register rs = RegisterManager.getTempReg(ir.v1);
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
            }else if(ir.v1 instanceof ConstInteger){
                Register rs = RegisterManager.getTempReg(ir.v2);
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
            } else{
                Register rs = RegisterManager.getTempReg(ir.v1);
                Register rt = RegisterManager.getTempReg(ir.v2);
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
            }
        }
        
    }

    //FIXME:数组处理
    private static void transGETELEMENTPTR(Instruction instruction) throws IOException {
        GETELEMENTPTR ir = (GETELEMENTPTR) instruction;
        if(ir.type==0)//%2 = getelementptr i32, i32* %1, i32 0, i32 1
        {
            if(!(ir.array instanceof GlobalVar))//局部数组
            {
                ValueType type = ((ArrayType)ir.array.type.pointto).elementType;
                Address address;
                if(AddressManager.existsArray(ir.array)){
                    address = AddressManager.getArrayAddress(ir.array);
                }else{
                    address = new Address(RegisterManager.getTempReg(ir.array));
                }
                if(!(type instanceof ArrayType)){
                    if(ir.offset instanceof ConstInteger){
                        Register target = RegisterManager.getTempReg(ir.res);
                        LoadAddressCode loadAddressCode = new LoadAddressCode(target,address);
                        loadAddressCode.print();
                        BinaryRegImmCode code = new BinaryRegImmCode(target,target,4*((ConstInteger)ir.offset).val, BinaryRegImmCode.Op.ADDI);
                        code.print();
                    }else{
                        Register target = RegisterManager.getTempReg(ir.res);
                        LoadAddressCode loadAddressCode = new LoadAddressCode(target,address);
                        loadAddressCode.print();
                        Register offsetReg = RegisterManager.getTempReg(ir.offset);
                        BinaryRegRegCode code = new BinaryRegRegCode(target,target,offsetReg, BinaryRegRegCode.Op.ADD);
                        code.print();
                        code.print();
                        code.print();
                        code.print();
                    }
                }else{
                    if(ir.offset instanceof ConstInteger){
                        Register target = RegisterManager.getTempReg(ir.res);
                        LoadAddressCode loadAddressCode = new LoadAddressCode(target,address);
                        loadAddressCode.print();
                        Integer sublength = ((ArrayType) type).length;
                        BinaryRegImmCode code = new BinaryRegImmCode(target,target,4*((ConstInteger)ir.offset).val*sublength, BinaryRegImmCode.Op.ADDI);
                        code.print();
                    }else{
                        Register target = RegisterManager.getTempReg(ir.res);
                        LoadAddressCode loadAddressCode = new LoadAddressCode(target,address);
                        loadAddressCode.print();
                        Register offsetReg = RegisterManager.getTempReg(ir.offset);
                        Integer sublength = ((ArrayType) type).length;
                        Register realOffsetReg = RegisterManager.getTempReg(new Value("temp",ValueType._i32));
                        BinaryRegImmCode code_1 = new BinaryRegImmCode(realOffsetReg,offsetReg,4*sublength, BinaryRegImmCode.Op.MULO);
                        code_1.print();
                        BinaryRegRegCode code_2 = new BinaryRegRegCode(target,target,realOffsetReg, BinaryRegRegCode.Op.ADD);
                        code_2.print();
                        RegisterManager.freeReg(realOffsetReg);
                    }
                }
            }else{//全局数组
                ValueType type = ((ArrayType)ir.array.type.pointto).elementType;
                if(!(type instanceof ArrayType)){
                    if(ir.offset instanceof ConstInteger){
                        Register target = RegisterManager.getTempReg(ir.res);
                        int offsetAsInt = ((ConstInteger)ir.offset).val*4;
                        Label label = new Label(ir.array.name.substring(1));
                        LoadAddressCode loadAddressCode = new LoadAddressCode(target, new Address(label,offsetAsInt));
                        loadAddressCode.print();
                    }else{
                        Register target = RegisterManager.getTempReg(ir.res);
                        Label label = new Label(ir.array.name.substring(1));
                        Register offsetReg = RegisterManager.getTempReg(ir.offset);
                        LoadAddressCode loadAddressCode = new LoadAddressCode(target, new Address(offsetReg,label));
                        loadAddressCode.print();
                        BinaryRegRegCode binaryRegRegCode = new BinaryRegRegCode(target,target,offsetReg, BinaryRegRegCode.Op.ADD);
                        binaryRegRegCode.print();
                        binaryRegRegCode.print();
                        binaryRegRegCode.print();
                    }
                }else{
                    if(ir.offset instanceof ConstInteger){
                        Register target = RegisterManager.getTempReg(ir.res);
                        Label label = new Label(ir.array.name.substring(1));
                        int offsetAsInt = ((ConstInteger)ir.offset).val*4;
                        Integer sublength = ((ArrayType) type).length;
                        LoadAddressCode loadAddressCode = new LoadAddressCode(target, new Address(label,offsetAsInt*sublength));
                        loadAddressCode.print();
                    }else{
                        Register target = RegisterManager.getTempReg(ir.res);
                        Label label = new Label(ir.array.name.substring(1));
                        Register offsetReg = RegisterManager.getTempReg(ir.offset);
                        Integer sublength = ((ArrayType) type).length;
                        Register realOffsetReg = RegisterManager.getTempReg(new Value("temp",ValueType._i32));
                        LoadAddressCode loadAddressCode = new LoadAddressCode(target, new Address(label));
                        loadAddressCode.print();
                        BinaryRegImmCode code_1 = new BinaryRegImmCode(realOffsetReg,offsetReg,4*sublength, BinaryRegImmCode.Op.MULO);
                        code_1.print();
                        LoadAddressCode code_2 = new LoadAddressCode(target, new Address(realOffsetReg,label));
                        code_2.print();
                        RegisterManager.freeReg(realOffsetReg);
                    }
                }
            }
        }else{
            //%2 = getelementptr i32, *i32 %1, i32 1
            ValueType type = ir.array.type.pointto;
            if(!(type instanceof ArrayType)){
                if(ir.offset instanceof ConstInteger){
                    Register target = RegisterManager.getTempReg(ir.res);
                    Register rs = RegisterManager.getTempReg(ir.array);
                    BinaryRegImmCode code = new BinaryRegImmCode(target,rs,4*((ConstInteger)ir.offset).val, BinaryRegImmCode.Op.ADDI);
                    code.print();
                }else{
                    Register target = RegisterManager.getTempReg(ir.res);
                    Register rs = RegisterManager.getTempReg(ir.array);
                    Register offsetReg = RegisterManager.getTempReg(ir.offset);
                    BinaryRegRegCode code_1 = new BinaryRegRegCode(target,rs,offsetReg, BinaryRegRegCode.Op.ADD);
                    code_1.print();
                    BinaryRegRegCode code_2 = new BinaryRegRegCode(target,target,offsetReg, BinaryRegRegCode.Op.ADD);
                    code_2.print();
                    code_2.print();
                    code_2.print();
                }
            }else{
                if(ir.offset instanceof ConstInteger){
                    Register target = RegisterManager.getTempReg(ir.res);
                    Register rs = RegisterManager.getTempReg(ir.array);
                    Integer sublength = ((ArrayType) type).length;
                    BinaryRegImmCode code = new BinaryRegImmCode(target,rs,4*((ConstInteger)ir.offset).val*sublength, BinaryRegImmCode.Op.ADDI);
                    code.print();
                }else{
                    Register target = RegisterManager.getTempReg(ir.res);
                    Register rs = RegisterManager.getTempReg(ir.array);
                    Register offsetReg = RegisterManager.getTempReg(ir.offset);
                    Integer sublength = ((ArrayType) type).length;
                    Register realOffsetReg = RegisterManager.getTempReg(new Value("temp",ValueType._i32));
                    BinaryRegImmCode code_1 = new BinaryRegImmCode(realOffsetReg,offsetReg,4*sublength, BinaryRegImmCode.Op.MULO);
                    code_1.print();
                    BinaryRegRegCode code_2 = new BinaryRegRegCode(target,rs,realOffsetReg, BinaryRegRegCode.Op.ADD);
                    code_2.print();
                    RegisterManager.freeReg(realOffsetReg);
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
                Register reg = RegisterManager.getTempReg(ir.params.get(0));
                MoveCode moveCode = new MoveCode(reg, RegisterManager.get$a0());
                moveCode.print();
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
            Register reg = RegisterManager.getTempReg(ir.loadto);
            MoveCode moveCode = new MoveCode(RegisterManager.getReturnReg(),reg);
            moveCode.print();
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
        RegisterManager.stageRegs();



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
                //如果寄存器里有这个值，直接store；如果没有，需要从地址那里加载到临时变量寄存器，再store
                if(RegisterManager.containsTempValue(value)){
                    StoreWordCode storeWordCode = new StoreWordCode(RegisterManager.getTempReg(value),AddressManager.getSPAddress());
                    storeWordCode.print();
                }else if(value instanceof VarPointer&&RegisterManager.containsGlobalValue(value)){
                    StoreWordCode storeWordCode = new StoreWordCode(RegisterManager.getGlobalReg((VarPointer) value),AddressManager.getSPAddress());
                    storeWordCode.print();
                }else{
                    Address address = AddressManager.find(value);
                    Register tempParamReg = RegisterManager.get$a0();
                    assert address!=null;
                    LoadWordCode loadWordCode = new LoadWordCode(tempParamReg,address);
                    loadWordCode.print();
                    StoreWordCode storeWordCode = new StoreWordCode(tempParamReg,AddressManager.getSPAddress());
                    storeWordCode.print();
                }
            }
        }

        for(int i=0;i<ir.params.size()&&i<4;i++){
            Value value = ir.params.get(i);
            if(value instanceof ConstInteger){
                LoadImmCode loadImmCode = new LoadImmCode(RegisterManager.getParamReg(i),((ConstInteger) value).val);
                loadImmCode.print();
            }else{
                if(RegisterManager.containsTempValue(value)){
                    MoveCode moveCode = new MoveCode(RegisterManager.getTempReg(value),RegisterManager.getParamReg(i));
                    moveCode.print();
                }else if(value instanceof VarPointer&&RegisterManager.containsGlobalValue(value)){
                    MoveCode moveCode = new MoveCode(RegisterManager.getGlobalReg((VarPointer) value),RegisterManager.getParamReg(i));
                    moveCode.print();
                }else{
                    Register target = RegisterManager.getParamReg(i);
                    Address address = AddressManager.find(value);
                    assert address!=null;
                    LoadWordCode loadWordCode = new LoadWordCode(target,address);
                    loadWordCode.print();
                }
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
        RegisterManager.restoreRegs();

        //8.加载返回值
        if(ir.loadto!=null){
            MoveCode moveReturnVal = new MoveCode(RegisterManager.getReturnReg(),RegisterManager.getTempReg(ir.loadto));
            moveReturnVal.print();
        }


    }

    private static void transBR(Instruction instruction) throws IOException {
        BR ir = (BR) instruction;
        if(ir.label_dst!=null){
            //无条件跳转
            JumpCode jumpCode = new JumpCode(new Label(curFunction.name+"_"+ir.label_dst.substring(1)));
            jumpCode.print();
        }else{
            //判断条件后跳转
            Register reg = RegisterManager.getTempReg(ir.cond);
            BrCondCode brCondCode = new BrCondCode(reg, new Label(curFunction.name+"_"+ir.label1.substring(1)), new Label(curFunction.name+"_"+ir.label2.substring(1)));
            brCondCode.print();
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
            AddressManager.storeArray(ir.pointer, AddressManager.getSPAddress());
        }else
            RegisterManager.getGlobalReg(ir.pointer);
    }

    private static void transADD(Instruction instruction) throws IOException {
        ADD ir = (ADD) instruction;
        Value v1 = ir.v1;
        Value v2 = ir.v2;
        Value res = ir.res;
        Register resReg = RegisterManager.getTempReg(res);
        if(v1 instanceof ConstInteger && v2 instanceof ConstInteger){
            LoadImmCode code = new LoadImmCode(resReg,((ConstInteger) v1).val+((ConstInteger) v2).val);
            code.print();
        }else if(v1 instanceof ConstInteger){
            Register reg2 = RegisterManager.getTempReg(v2);
            BinaryRegImmCode code = new BinaryRegImmCode(resReg,reg2, ((ConstInteger) v1).val, BinaryRegImmCode.Op.ADDI);
            code.print();
        }else if(v2 instanceof ConstInteger){
            Register reg1 = RegisterManager.getTempReg(v1);
            BinaryRegImmCode code = new BinaryRegImmCode(resReg,reg1, ((ConstInteger) v2).val, BinaryRegImmCode.Op.ADDI);
            code.print();
        }else{
            Register reg1 = RegisterManager.getTempReg(v1);
            Register reg2 = RegisterManager.getTempReg(v2);
            BinaryRegRegCode code = new BinaryRegRegCode(resReg, reg1, reg2, BinaryRegRegCode.Op.ADD);
            code.print();
        }
    }
}
