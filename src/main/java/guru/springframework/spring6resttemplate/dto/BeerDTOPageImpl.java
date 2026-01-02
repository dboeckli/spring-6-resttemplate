package guru.springframework.spring6resttemplate.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true, value = "pageable")
public class BeerDTOPageImpl<T> extends PageImpl<T> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public BeerDTOPageImpl(@JsonProperty("content") List<T> content,
                           @JsonProperty("page") PageMeta page) {
        super(
            content,
            page != null && page.number() != null && page.size() != null
                ? PageRequest.of(page.number(), page.size())
                : PageRequest.of(0, 1),
            page != null && page.totalElements() != null
                ? page.totalElements()
                : 1
        );
    }

    public BeerDTOPageImpl(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public BeerDTOPageImpl(List<T> content) {
        super(content);
    }
}
