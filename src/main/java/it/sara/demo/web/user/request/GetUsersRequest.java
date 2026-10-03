package it.sara.demo.web.user.request;

import it.sara.demo.web.request.GenericRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUsersRequest extends GenericRequest {

    private String query;

    @Min(0)
    private int offset = 0;

    @Min(1)
    @Max(100)
    private int limit = 10;

    @NotNull(message = "Order type is required")
    private OrderType order;

    public enum OrderType {
        BY_FIRSTNAME,
        BY_FIRSTNAME_DESC,
        BY_LASTNAME,
        BY_LASTNAME_DESC
    }
}