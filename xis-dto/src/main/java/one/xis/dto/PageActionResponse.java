package one.xis.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PageActionResponse extends Response {
    private String nexrPageuri;
}
