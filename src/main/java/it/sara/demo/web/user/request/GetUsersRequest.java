package it.sara.demo.web.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import it.sara.demo.web.request.GenericRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Search, pagination and sort parameters for the user list. The entire body is optional — omitting it applies the defaults shown below.")
public class GetUsersRequest extends GenericRequest {

    @Schema(description = "Case-insensitive contains filter applied to firstName, lastName and email. Omit or leave blank to return all users.", example = "mario")
    private String query;

    @Min(0)
    @Schema(description = "Zero-based index of the first record to return", example = "0", defaultValue = "0")
    private int offset = 0;

    @Min(1)
    @Max(100)
    @Schema(description = "Maximum number of records per page (1–100)", example = "10", defaultValue = "10")
    private int limit = 10;

    @NotNull(message = "Order type is required")
    @Schema(description = "Sort field and direction", defaultValue = "BY_LASTNAME")
    private OrderType order;

    public GetUsersRequest() {
        this.order = OrderType.BY_LASTNAME;
    }

    @Schema(description = "Available sort options for the user list")
    public enum OrderType {
        @Schema(description = "Sort by first name ascending")  BY_FIRSTNAME,
        @Schema(description = "Sort by first name descending") BY_FIRSTNAME_DESC,
        @Schema(description = "Sort by last name ascending")  BY_LASTNAME,
        @Schema(description = "Sort by last name descending") BY_LASTNAME_DESC
    }
}