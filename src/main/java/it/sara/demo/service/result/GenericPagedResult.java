package it.sara.demo.service.result;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class GenericPagedResult extends GenericResult {
    private int total;
    private int offset;
    private int limit;
}