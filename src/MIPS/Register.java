package MIPS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Register {
    public final String name;
    public RegisterType type;
    private Register(String name, RegisterType type) {
        this.name = name;
        this.type = type;
    }

    public static Register $zero = new Register("$zero", RegisterType.Zero);

    public static Register $at = new Register("$at", RegisterType.At);

    public static Register $v0 = new Register("$v0", RegisterType.Return);
    public static Register $v1 = new Register("$v1", RegisterType.Temp);//暂时按临时寄存器管理

    public static Register $a0 = new Register("$a0", RegisterType.Param);
    public static Register $a1 = new Register("$a1", RegisterType.Param);
    public static Register $a2 = new Register("$a2", RegisterType.Param);
    public static Register $a3 = new Register("$a3", RegisterType.Param);

    public static Register $t0 = new Register("$t0", RegisterType.Temp);
    public static Register $t1 = new Register("$t1", RegisterType.Temp);
    public static Register $t2 = new Register("$t2", RegisterType.Temp);
    public static Register $t3 = new Register("$t3", RegisterType.Temp);
    public static Register $t4 = new Register("$t4", RegisterType.Temp);
    public static Register $t5 = new Register("$t5", RegisterType.Temp);
    public static Register $t6 = new Register("$t6", RegisterType.Temp);
    public static Register $t7 = new Register("$t7", RegisterType.Temp);
    public static Register $t8 = new Register("$t8", RegisterType.Temp);
    public static Register $t9 = new Register("$t9", RegisterType.Temp);

    public static Register $s0 = new Register("$s0", RegisterType.Global);
    public static Register $s1 = new Register("$s1", RegisterType.Global);
    public static Register $s2 = new Register("$s2", RegisterType.Global);
    public static Register $s3 = new Register("$s3", RegisterType.Global);
    public static Register $s4 = new Register("$s4", RegisterType.Global);
    public static Register $s5 = new Register("$s5", RegisterType.Global);
    public static Register $s6 = new Register("$s6", RegisterType.Global);
    public static Register $s7 = new Register("$s7", RegisterType.Global);

    public static Register $gp = new Register("$gp",RegisterType.Gp);
    public static Register $sp = new Register("$fp",RegisterType.Sp);
    public static Register $fp = new Register("$sp",RegisterType.Fp);
    public static Register $ra = new Register("$ra",RegisterType.Ra);


    public static List<Register> regs = new ArrayList<Register>(Arrays.asList($zero,$at,$v0,$v1,$a0,$a1,$a2,$a3,$t0,$t1,
            $t2,$t3,$t4,$t5,$t6,$t7,$t8,$t9,$s0,$s1,$s2,$s3,$s4,$s5,$s6,$s7,$gp,$sp));
}
