package IR.Value.Instructions;

import IR.Value.BasicBlock;

import java.io.IOException;

public class OR extends Instruction{
    public OR(InstructionType type, BasicBlock basicBlock) {
        super(type, basicBlock);
    }

    @Override
    public void print() throws IOException {

    }
}
