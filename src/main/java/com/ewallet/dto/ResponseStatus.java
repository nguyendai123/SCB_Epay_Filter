package com.ewallet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseStatus {
    @JsonProperty("ErrorCode")
    private String errorCode;
    @JsonProperty("Message")
    private String message;
    @JsonProperty("Errors")
    private List<String> errors;

}
