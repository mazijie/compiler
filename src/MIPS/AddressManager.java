package MIPS;

import IR.Value.Value;
import IR.Value.VarPointer;
import MIPS.Code.BinaryRegImmCode;
import MIPS.Code.LoadWordCode;
import MIPS.Code.StoreWordCode;

import java.io.IOException;
import java.util.HashMap;

public class AddressManager {

    static int offsetFromFP=0;
//    static int tempOffsetFromFP=0;
    public static void setOffsetZero(){
        offsetFromFP=0;
    }
    public static Address getSPAddress(){
        return new Address(FP,offsetFromFP);
    }
    public static void addSP() throws IOException {
//        BinaryRegImmCode code = new BinaryRegImmCode(SP,SP,-4, BinaryRegImmCode.Op.ADDI);
        offsetFromFP-=4;
//        code.print();
    }
    public static void subSP() throws IOException {
//        BinaryRegImmCode code = new BinaryRegImmCode(SP,SP,4, BinaryRegImmCode.Op.ADDI);
        offsetFromFP+=4;
//        code.print();
    }
    static Register SP = Register.$sp;
    static Register FP = Register.$fp;
    private static HashMap<Address, Value> valueOfAddress = new HashMap<Address, Value>();
    private static HashMap<Value, Address> addressOfValue = new HashMap<Value, Address>();

    public static void clearAddressOfValue(){
        addressOfValue.clear();
    }
    public static void setFPtoSP() throws IOException {
        //当调用新函数的时候，需要将当前位置的FP保存，以SP为新FP为新函数的帧指针，此时SP=FP
        addSP();
        StoreWordCode storeFP = new StoreWordCode(RegisterManager.getFp(),getSPAddress());
        storeFP.print();
        BinaryRegImmCode moveSP2FP = new BinaryRegImmCode(FP,FP,offsetFromFP, BinaryRegImmCode.Op.ADDIU);
//        MoveCode moveSP2FP = new MoveCode(RegisterManager.getSp(),RegisterManager.getFp());
        moveSP2FP.print();
//        tempOffsetFromFP=offsetFromFP;
//        offsetFromFP=0;
    }
    public static void setSPtoFP() throws IOException {
        //当函数结束调用的时候，应释放全部FP和SP之间的内存，并将FP恢复到调用者函数的FP值
        /*
            move $sp, $fp    # 恢复栈指针到帧指针的位置
            lw $fp, 0($sp)   # 恢复帧指针到调用函数的帧指针的位置
            addi $sp, $sp, 4 # 弹出返回地址
            jr $ra           # 跳转回调用函数
        */
//        MoveCode moveFPtoSP = new MoveCode(RegisterManager.getFp(),RegisterManager.getSp());//将sp回归到fp
//        moveFPtoSP.print();
        LoadWordCode loadWordCode = new LoadWordCode(RegisterManager.getFp(),new Address(FP));
        loadWordCode.print();
//        offsetFromFP=tempOffsetFromFP;
        subSP();
    }
    //Thinking:当溢出的时候，是把原来的某个寄存器清空，用来放新的值
    public static Address store(Value value) throws IOException {
        //当寄存器溢出的时候调用函数，把寄存器的值存内存，如果已经有了这个值，就不需要存了
        Address address = find(value);
        if(address==null){
            addSP();
            address =new Address(FP,offsetFromFP);
            addressOfValue.put(value,address);
        }
        return address;
    }


    public static boolean contains(Value value) {
        for(Value item : addressOfValue.keySet()){
            if(item.name.equals(value.name)) return true;
        }
        return false;
    }

    public static Address find(Value value) {
        for(Value item : addressOfValue.keySet()){
            if(item.name.equals(value.name)) return addressOfValue.get(item);
        }
        return null;
    }

    static HashMap<VarPointer,Address> arrayAddresses = new HashMap<VarPointer,Address>();

    public static void storeArray(VarPointer pointer, Address address){
        arrayAddresses.put(pointer,address);
    }

    public static void clearArray(){
        arrayAddresses.clear();
    }

    public static Address getArrayAddress(VarPointer pointer){
        for(VarPointer varPointer:arrayAddresses.keySet()){
            if(varPointer.name.equals(pointer.name)) return arrayAddresses.get(varPointer);
        }return null;
    }

    public static boolean existsArray(VarPointer pointer){
        for(VarPointer varPointer:arrayAddresses.keySet()){
            if(varPointer.name.equals(pointer.name)) return true;
        }return false;
    }
}
