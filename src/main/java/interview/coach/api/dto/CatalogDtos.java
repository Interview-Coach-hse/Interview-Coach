package interview.coach.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record CatalogItemRequest(
            @NotBlank String code,
            @NotBlank String name
    ) {
    }

    public record CatalogItemResponse(
            UUID id,
            String code,
            String name
    ) {
    }
}
