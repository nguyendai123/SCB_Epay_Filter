package com.ewallet.objects;

import com.ewallet.objects.base.EWalletRequest;
import com.ewallet.objects.base.EWalletResponse;
import com.ewallet.util.SerializerUtil;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessObject {
    private String lmid;
    private String requestID;
    private String functionName;
    private String contentType;
    private String requestMessage;
    private EWalletRequest requestObject;
    private Date createdDate;
    private String messageError;
    private MerchantObject merchant;
    private String sessionID;
    private EWalletResponse responseObject;
    private String languageID;
    private boolean isTransaction;

    public ProcessObject(String lmid) {
        this.createdDate = new Date();
        this.lmid = lmid;
    }

    public String responseError(String responseCode) {
        return String.format(messageError, responseCode);
    }

    public String response(Object responseObject) {
        return SerializerUtil.serialize(contentType, responseObject);
    }

}
