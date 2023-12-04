package MIPS;

import IR.Value.Argument;
import IR.Value.Value;
import IR.Value.VarPointer;
import MIPS.Code.LoadWordCode;
import MIPS.Code.MIPSCode;
import MIPS.Code.StoreWordCode;
import utils.Boom;

import java.io.IOException;
import java.util.*;

public class RegisterManager {
    static HashMap<Value,Register> regOfValue = new HashMap<Value,Register>();
    static HashMap<Register,Value> valueOfReg = new HashMap<Register,Value>();

    public static void init(){
        for(Register reg: Register.regs){
            switch (reg.type){
                case Temp -> tempRegs.add(reg);
                case Global -> globalRegs.add(reg);
            }
        }
    }

    public static void freeTempRegs(){
        while(!tempUsed.isEmpty()){
            Register reg = tempUsed.get(0);
            freeReg(reg);
        }
    }

    public static void freeGlobalRegs(){
        while(!globalUsed.isEmpty()){
            Register reg = globalUsed.get(0);
            freeReg(reg);
        }
    }

    public static void freeAllRegs(){
        freeTempRegs();
        freeGlobalRegs();
    }

    public static void freeReg(Register reg){
        Value v = valueOfReg.get(reg);
        valueOfReg.remove(reg);
        regOfValue.remove(v);
        tempUsed.remove(reg);
        globalUsed.remove(reg);

        switch (reg.type){
            case Temp -> tempRegs.add(reg);
            case Global -> globalRegs.add(reg);
        }
    }

    public static Register getZero(){
        return Register.$zero;
    }

    public static Register getSp(){
        return Register.$sp;
    }

    public static Register getGp(){
        return Register.$gp;
    }
    public static Register getFp(){
        return Register.$fp;
    }
    public static Register getRa(){
        return Register.$ra;
    }
    

    //对返回值的管理：将返回值寄存器或者立即数move到v0。
    //父函数：将v0的值move到目标寄存器。
    public static Register getReturnReg(){ return Register.$v0;}
    
    //对传参的管理：当参数个数小于4时，依次将参数的寄存器或者立即数move到a0\a1\a2\a3，如果大于等于4，多余参数依次存入内存
    //子函数：依据参数个数获得参数，取一个参数就将sp回升4。然后fp=sp。
    public static Register getParamReg(int index) {
        switch(index){
            case 0:
                return get$a0();
            case 1:
                return get$a1();
            case 2:
                return get$a2();
            case 3:
                return get$a3();
            default:
                return null;
        }
    }


    public static Register get$a0(){ return Register.$a0;}
    public static Register get$a1(){ return Register.$a1;}
    public static Register get$a2(){ return Register.$a2;}
    public static Register get$a3(){ return Register.$a3;}

    private static List<Register> tempRegs = new ArrayList<>();
    private static List<Register> tempUsed = new ArrayList<>();

    public static Register getTempReg(Value value) throws IOException {
        if(value==null) Boom.boom();
        //1.寄存器里有存这个值
        for(Value item : regOfValue.keySet())
        {
            if(tempUsed.contains(regOfValue.get(item))&&item.name.equals(value.name)) return regOfValue.get(item);
        }
        //2.内存有存这个值
        if(AddressManager.contains(value)) {
            Address addOfTarget = AddressManager.find(value);
            if(tempRegs.isEmpty()){
                Register regToFree = tempUsed.get(0);
                Value valueToStore = valueOfReg.get(regToFree);
                Address addToStore = AddressManager.store(valueToStore);
                StoreWordCode code_1 = new StoreWordCode(regToFree,addToStore);//把寄存器原来的值存到内存
                code_1.print();
                freeReg(regToFree);
            }
            Register regToUse = tempRegs.get(0);
            LoadWordCode code_2 = new LoadWordCode(regToUse,addOfTarget);//将目标值移到寄存器上
            code_2.print();
            return regToUse;
        }
        //3.这个值是新的，需要开一个寄存器
        //没有寄存器就腾一个
        if(tempRegs.isEmpty()){
            Register r = tempUsed.get(0);
            Value v = valueOfReg.get(r);
            freeReg(r);
            Address add = AddressManager.store(v);
            StoreWordCode code = new StoreWordCode(r,add);
            code.print();
        }
        Register r = tempRegs.get(0);
        tempRegs.remove(r);
        tempUsed.add(r);
        regOfValue.put(value,r);
        valueOfReg.put(r,value);
        return r;
    }


    private static List<Register> globalRegs = new ArrayList<>();
    private static List<Register> globalUsed = new ArrayList<>();

    public static Register getGlobalReg(VarPointer varPointer) throws IOException {
        //1.寄存器里有存这个值
        if(regOfValue.containsKey(varPointer)) return regOfValue.get(varPointer);
        //2.内存有存这个值
        if(AddressManager.contains(varPointer)) {
            if(globalRegs.isEmpty()){
                Register regToFree = globalUsed.get(0);
                Value valueToStore = valueOfReg.get(regToFree);
                Address addToStore = AddressManager.store(valueToStore);
                StoreWordCode code_1 = new StoreWordCode(regToFree,addToStore);//把寄存器原来的值存到内存
                code_1.print();
                freeReg(regToFree);
            }
            Address addOfTarget = AddressManager.find(varPointer);
            Register regToUse = globalRegs.get(0);
            globalRegs.remove(regToUse);
            globalUsed.add(regToUse);
            regOfValue.put(varPointer,regToUse);
            valueOfReg.put(regToUse,varPointer);
            LoadWordCode code_2 = new LoadWordCode(regToUse,addOfTarget);//将目标值移到寄存器上
            code_2.print();
            return regToUse;
        }
        //3.这个值是新的，需要开一个寄存器
        //没有寄存器就腾一个
        globalValues.add(varPointer);
        if(globalRegs.isEmpty()){
            Register r = globalUsed.get(0);
            Value v = valueOfReg.get(r);
            freeReg(r);
            Address add = AddressManager.store(v);
            StoreWordCode code = new StoreWordCode(r,add);
            code.print();
        }
        Register r = globalRegs.get(0);
        globalRegs.remove(r);
        globalUsed.add(r);
        regOfValue.put(varPointer,r);
        valueOfReg.put(r,varPointer);
        return r;
    }


    public static void stageRegs() throws IOException {
        for(int i=0;i<tempUsed.size();i++){
            AddressManager.addSP();
            Register reg = tempUsed.get(i);
            StoreWordCode storeWordCode = new StoreWordCode(reg, AddressManager.getSPAddress());
            storeWordCode.print();
        }
        for(int j=0;j<globalUsed.size();j++){
            AddressManager.addSP();
            Register reg = globalUsed.get(j);
            StoreWordCode storeWordCode = new StoreWordCode(reg, AddressManager.getSPAddress());
            storeWordCode.print();
        }
        AddressManager.addSP();
        StoreWordCode storeWordCode = new StoreWordCode(RegisterManager.getRa(), AddressManager.getSPAddress());
        storeWordCode.print();
    }

    public static void restoreRegs() throws IOException {
        LoadWordCode code = new LoadWordCode(RegisterManager.getRa(),AddressManager.getSPAddress());
        code.print();
        AddressManager.subSP();
        for(int j=globalUsed.size()-1;j>=0;j--){
            Register reg = globalUsed.get(j);
            LoadWordCode loadWordCode = new LoadWordCode(reg, AddressManager.getSPAddress());
            loadWordCode.print();
            AddressManager.subSP();
        }
        for(int i=tempUsed.size()-1;i>=0;i--){
            Register reg = tempUsed.get(i);
            LoadWordCode loadWordCode = new LoadWordCode(reg, AddressManager.getSPAddress());
            loadWordCode.print();
            AddressManager.subSP();
        }
    }

    public static boolean containsTempValue(Value value){
        for(Register reg : tempUsed){
            if(valueOfReg.get(reg)==null){
                int i=0;
                return true;
            }
            if(valueOfReg.get(reg).name.equals(value.name)) return true;
        }
        return false;
    }

    public static boolean containsGlobalValue(Value value){
        for(Register reg : globalUsed){
            if(valueOfReg.get(reg).name.equals(value.name)) return true;
        }
        return false;
    }

    static List<Value> globalValues = new ArrayList<Value>();

    public static boolean isGlobalValue(Value value){
        for(Value v: globalValues){
            if(v.name.equals(value.name)) return true;
        }return false;
    }
    public static void clearGlobalValues(){
        globalValues.clear();
    }

    public static Register getNewTempReg(Value value) throws IOException {
        if(value==null) Boom.boom();
        //3.这个值是新的，需要开一个寄存器
        //没有寄存器就腾一个
        //120501号行政令：批示全部形参即取即存，调用getNewTempReg获得寄存器，用完就释放
        if(tempRegs.isEmpty()){
            Register r = tempUsed.get(0);
            freeReg(r);
        }
        Register r = tempRegs.get(0);
        tempRegs.remove(r);
        tempUsed.add(r);
        regOfValue.put(value,r);
        valueOfReg.put(r,value);
        return r;
    }
}
