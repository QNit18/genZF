package com.qnit18.main_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Base API response wrapper")
public class ApiBaseResponse<T> {
    @Builder.Default
    @Schema(description = "Response code", example = "1000")
    int code = 1000;
    
    @Builder.Default
    @Schema(description = "Response message", example = "Success")
    String message = "Success";
    
    @Schema(description = "Response data payload")
    T result;
}

