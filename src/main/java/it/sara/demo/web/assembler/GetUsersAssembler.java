package it.sara.demo.web.assembler;

import it.sara.demo.service.user.criteria.CriteriaGetUsers;
import it.sara.demo.service.user.result.GetUsersResult;
import it.sara.demo.web.response.GenericResponse;
import it.sara.demo.web.user.request.GetUsersRequest;
import it.sara.demo.web.user.response.GetUsersResponse;
import org.springframework.stereotype.Component;

@Component
public class GetUsersAssembler {

    public CriteriaGetUsers toCriteria(GetUsersRequest request) {
        return CriteriaGetUsers.builder()
                .query(request.getQuery())
                .offset(request.getOffset())
                .limit(request.getLimit())
                .order(mapOrder(request.getOrder()))
                .build();
    }

    public GetUsersResponse toResponse(GetUsersResult result) {
        return GetUsersResponse.builder()
                .status(GenericResponse.success("OK").getStatus())
                .users(result.getUsers())
                .total(result.getTotal())
                .offset(result.getOffset())
                .limit(result.getLimit())
                .pageCount(computePageCount(result.getTotal(), result.getLimit()))
                .build();
    }

    private CriteriaGetUsers.OrderType mapOrder(GetUsersRequest.OrderType order) {
        return CriteriaGetUsers.OrderType.valueOf(order.name());
    }

    private int computePageCount(int total, int limit) {
        if (limit <= 0) return 0;
        return (int) Math.ceil((double) total / limit);
    }
}