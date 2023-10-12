package Parser.NonTerminators;

import Lexer.Token;
import Parser.NTTypes;
import Parser.Parser;
import utils.IOUtils;

import java.io.IOException;
import java.util.List;

public class Block {
    //Block → '{' { BlockItem } '}'
    public Token left;
    public Token right;
    public List<BlockItem> blockItems;

    public Block(Token left,List<BlockItem> blockItems,Token right){
        this.blockItems=blockItems;
        this.left=left;
        this.right=right;
    }

    public void print() throws IOException {
        IOUtils.write(this.left.toString());
        if(blockItems!=null){
            for(BlockItem item : blockItems){
                item.print();
            }
        }
        IOUtils.write(this.right.toString());
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.Block));
    }
}
