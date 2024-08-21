package com.stb.epay.objects.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j

public class EWalletRequest {
    @JsonProperty("Data")
    @Schema(description = "StringCryptData", example = "nWqfchR1eogv3Jw47UaozmrKpuPeLE6cHbWjba2Ddj5jDLJWX4")
    private String data;

    @JsonProperty("FunctionName")
    @Schema(description = "FunctionName", example = "eWalletCardAccountBalanceInquiry")
    private String functionName;

    @JsonProperty("RequestDateTime")
    @Schema(description = "RequestDateTime", defaultValue = "2023-06-07T00:14:44+07:00", example = "2023-06-07T00:14:44+07:00")
    private String requestDateTime;

    @JsonProperty("RequestID")
    @Schema(description = "RequestID", defaultValue = "3d446caa-c255-4d45-b49b-76d1f84265fa", example = "3d446caa-c255-4d45-b49b-76d1f84265fa")
    private String requestID;

    @JsonProperty("SessionID")
    @Schema(description = "SessionID", defaultValue = "", example = "HGU9348TH94FJF02F")
    private String sessionID;

}
