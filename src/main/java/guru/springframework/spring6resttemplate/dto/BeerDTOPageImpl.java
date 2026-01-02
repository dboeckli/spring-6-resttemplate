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
            PageRequest.of(
                Objects.requireNonNull(page.number(), "page.number must not be null"),
                Objects.requireNonNull(page.size(), "page.size must not be null")
            ),
            Objects.requireNonNull(page.totalElements(), "page.totalElements must not be null")
        );
    }

    public BeerDTOPageImpl(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public BeerDTOPageImpl(List<T> content) {
        super(content);
    }
}
