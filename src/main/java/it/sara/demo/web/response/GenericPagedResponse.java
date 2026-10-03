package it.sara.demo.web.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class GenericPagedResponse extends GenericResponse {
    private int total;
    private int offset;
    private int limit;
    private int pageCount;
}