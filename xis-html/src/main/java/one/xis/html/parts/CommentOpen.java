package one.xis.html.parts;


import lombok.Data;

@Data
public class CommentOpen implements Part, OpeningNode {
    @Override
    public int tokenCount() {
        return 1;
    }
}