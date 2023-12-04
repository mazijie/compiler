package MIPS;

public class Address {
    Register base;
    Label label;
    Integer offset;

    public Address(Register base) {
        this.base = base;
    }

    public Address(Label label) {
        this.label = label;
    }

    public Address(Integer offset) {
        this.offset = offset;
    }

    public Address(Register base, Label label) {
        this.base = base;
        this.label = label;
    }

    public Address(Register base, Integer offset) {
        this.base = base;
        this.offset = offset;
    }

    public Address(Label label, Integer offset) {
        this.label = label;
        this.offset = offset;
    }

    public Address(Register base, Label label, Integer offset) {
        this.base = base;
        this.label = label;
        this.offset = offset;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(label!=null) sb.append(label.name);
        if(label!=null&&offset!=null&&offset>0) sb.append("+").append(offset);
        else if(label!=null&&offset!=null&&offset<0) sb.append(offset);
        else if(label==null&&offset!=null&&offset!=0) sb.append(offset);
        if(base!=null) sb.append("(").append(base.name).append(")");
        return sb.toString();
    }
}
