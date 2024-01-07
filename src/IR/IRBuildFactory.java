package IR;

import IR.Array.Array;
import IR.Array.ArrayType;
import IR.NewSymbol.NewSymbolManager;
import IR.Type.PointerType;
import IR.Type.Type;
import IR.Type.ValueType;
import IR.Value.*;
import IR.Value.Instructions.*;
import Lexer.Token;
import Lexer.TokenType;
import Parser.NonTerminators.FuncFParam;
import Parser.NonTerminators.FuncFParams;

import java.util.ArrayList;
import java.util.List;

public class IRBuildFactory {
    public static Function buildFunction(IRModule module, String name, ValueType type){
        Function res=new Function(name,type);
        module.addFunction(res);
//        NewSymbolManager.addValue(res);
        return res;
    }

    public static BasicBlock buildBasicBlock(String name,Function function){
        BasicBlock res=new BasicBlock(name,function);
        function.addBlock(res);
        return res;
    }

    public static void buildRET(BasicBlock curBasicBlock,Value value)
    {
        RET res = new RET(curBasicBlock,value);
        curBasicBlock.addInstruction(res);
    }
    public static void buildRET(BasicBlock curBasicBlock)
    {
        RET res = new RET(curBasicBlock);
        curBasicBlock.addInstruction(res);
    }


    public static void buildADDInstruction(BasicBlock curBasicBlock, Value v1, Value v2, Value res) {
        curBasicBlock.addInstruction(new ADD(curBasicBlock,v1,v2,res));
    }

    public static void buildSUBInstruction(BasicBlock curBasicBlock, Value v1, Value v2, Value res) {
        curBasicBlock.addInstruction(new SUB(curBasicBlock,v1,v2,res));
    }

    public static void buildMULInstruction(BasicBlock curBasicBlock, Value v1, Value v2, Value res) {
        curBasicBlock.addInstruction(new MUL(curBasicBlock,v1,v2,res));
    }

    public static void buildSDIVInstruction(BasicBlock curBasicBlock, Value v1, Value v2, Value res) {
        curBasicBlock.addInstruction(new SDIV(curBasicBlock,v1,v2,res));
    }

    public static Value buildConstInt(String num) {
        return new ConstInteger(num);
    }

    public static ConstInteger buildConstInt(int num) {
        return new ConstInteger(num);
    }

    public static void buildSREMInstruction(BasicBlock curBasicBlock, Value v1, Value v2, Value new_var) {
        curBasicBlock.addInstruction(new SREM(curBasicBlock,v1,v2,new_var));
    }

    public static GlobalVar buildGlobalVar(String name,boolean isConst,PointerType type,int val){
        return new GlobalVar(name,isConst,type,val);
    }

    public static GlobalVar buildGlobalVar(String name,boolean isConst,PointerType type){
        return new GlobalVar(name,isConst,type);
    }

    public static GlobalVar buildGlobalVar(String name,boolean isConst,PointerType type,Array array){
        return new GlobalVar(name,isConst,type,array);
    }

    public static VarPointer buildVarPointer(String name, PointerType type) {
        return new VarPointer(name,type);
    }

    public static void buildALLOCA(BasicBlock curBasicBlock, VarPointer v1){
        ALLOCA allocA = new ALLOCA(curBasicBlock,v1);
        curBasicBlock.addInstruction(allocA);
    }

    public static void buildSTORE(BasicBlock curBasicBlock,Value v1,VarPointer v2){
        STORE store = new STORE(curBasicBlock,v1,v2);
        curBasicBlock.addInstruction(store);
    }

    public static void buildLOAD(BasicBlock curBasicBlock,Value loadto,VarPointer loadfrom){
        if(loadto instanceof VarPointer) loadto.isAddressMode=true;
        LOAD load = new LOAD(curBasicBlock,loadto,loadfrom);
        curBasicBlock.addInstruction(load);
    }

    public static Value buildValue(String name,ValueType type){
        return new Value(name,type);
    }

    public static void buildCall(BasicBlock curBasicBlock,String name,ValueType type,List<Value> args){
        curBasicBlock.addInstruction(new CALL(curBasicBlock,name,type,args));
    }

    public static void buildCall(BasicBlock curBasicBlock,String name,ValueType type,List<Value> args,Value loadto){
        curBasicBlock.addInstruction(new CALL(curBasicBlock,name,type,args,loadto));
    }

    public static Argument buildArgument(String name,ValueType type){
        return new Argument(name,type);
    }

    public static BR buildBR(BasicBlock curBasicBlock,Value cond,String label_1,String label_2){
        BR res = new BR(curBasicBlock,cond,label_1,label_2);
        curBasicBlock.addInstruction(res);
        return res;
    }

    public static BR buildBR(BasicBlock curBasicBlock,String label_dst){
        BR res = new BR(curBasicBlock,label_dst);
        curBasicBlock.addInstruction(res);
        return res;
    }

    public static void buildZEXTTO(BasicBlock curBasicBlock,Value zextfrom,Value zextto){
        curBasicBlock.addInstruction(new ZEXTTO(curBasicBlock,zextfrom,zextto));
    }

//    public static void buildGETELEMENTPTR(BasicBlock curBasicBlock, VarPointer res, Array array, int offset){
//        curBasicBlock.addInstruction(new GETELEMENTPTR(curBasicBlock,res,array,offset));
//
//    }

    public static void buildGETELEMENTPTR(BasicBlock curBasicBlock, VarPointer res, VarPointer array, Value offset,int type){
        res.isAddressMode=true;
        curBasicBlock.addInstruction(new GETELEMENTPTR(curBasicBlock,res,array,offset,type));

    }

    public static void buildGETELEMENTPTR(BasicBlock curBasicBlock, VarPointer res, GlobalVar array, Value offset,int type){
        res.isAddressMode=true;
        curBasicBlock.addInstruction(new GETELEMENTPTR(curBasicBlock,res,array,offset,type));

    }

    public static void buildICMP(BasicBlock curBasicBlock, Value v1, Value v2,Value res,Token token){
        switch (token.getType()) {
            case LSS ->
                //<
                    curBasicBlock.addInstruction(new ICMP(curBasicBlock, v1, v2, ICMPType.slt, res));
            case LEQ ->
                //<=
                    curBasicBlock.addInstruction(new ICMP(curBasicBlock, v1, v2, ICMPType.sle, res));
            case GRE ->
                //>
                    curBasicBlock.addInstruction(new ICMP(curBasicBlock, v1, v2, ICMPType.sgt, res));
            case GEQ ->
                //>=
                    curBasicBlock.addInstruction(new ICMP(curBasicBlock, v1, v2, ICMPType.sge, res));
            case EQL ->
                //==
                    curBasicBlock.addInstruction(new ICMP(curBasicBlock, v1, v2, ICMPType.eq, res));
            case NEQ ->
                //==
                    curBasicBlock.addInstruction(new ICMP(curBasicBlock, v1, v2, ICMPType.ne, res));
        }
    }

    public static Array buildArray(String name, ArrayType type, List<ConstInteger> vals) {
        return new Array(name,type,vals);
    }

    public static Array buildArray(String name, ArrayType type, List<Array> arrays,int de) {
        return new Array(name,type,arrays,de);
    }

    public static Array buildArray(String name, ArrayType type) {
        return new Array(name,type);
    }

//    public static GlobalArray buildGlobalArray(String name, boolean isConst,PointerType type,ValueType childType,int length,List<ConstInteger> vals, int blank){
//        return new GlobalArray(name,isConst,type,childType,length,vals,blank);
//    }

//    public static GlobalArray buildGlobalArray(String name, boolean isConst, PointerType type, ArrayType childType, int length, List<GlobalArray> arrays){
//        return new GlobalArray(name,isConst,type,childType,length,arrays);
//    }
}
