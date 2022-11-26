package one.xis.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActionResponse extends Response {
    String nextPagePath;
    String nextWidget;
}
