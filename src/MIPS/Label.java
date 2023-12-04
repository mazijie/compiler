package MIPS;

import IR.Value.BasicBlock;

public class Label {
    public String name;
    public Label(String name) {
        this.name = name;
    }
    public static Label getLabel(BasicBlock block) {
        return new Label(block.function.name+"_"+block.name.substring(1));
    }
}
