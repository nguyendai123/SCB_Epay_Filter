package com.ewallet.objects.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class EwalletBodyBase {
    @JsonProperty("LanguageID")
    private String languageID;
    @JsonProperty("RefNumber")
    private String refNumber;
    @JsonProperty("CountryCode")
    private String countryCode;
    @JsonProperty("eWalletID")
    private String eWalletID;
}
