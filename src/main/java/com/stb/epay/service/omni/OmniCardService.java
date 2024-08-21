package com.stb.epay.service.omni;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stb.epay.dto.omni.card.OmniFuncObject;
import com.stb.epay.dto.omni.card.OmniFunctionObject;
import com.stb.epay.encryptor.application.service.EncryptorSV;
import com.stb.epay.util.PinRepository;
import com.stb.epay.util.ServiceMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.stb.epay.lib.crypto.TripleDESHelper.encrypt;
import static com.stb.epay.objects.Constants.ConstantOmniCard.*;
import static com.stb.epay.objects.Constants.EWalletResponseCode.DO_NOT_HONOR;
import static com.stb.epay.objects.Constants.EWalletResponseCode.INVALID_RESPONSE;
import static com.stb.epay.objects.Constants.LogFormat.ERROR_FORMAT;

@Service
@Slf4j
@RequiredArgsConstructor
public class OmniCardService {

    // Start send to Omni
    @Value("${appSetting.Omni. 60000}")
    private int timeout;

    @Value("${appSetting.Omni. ''}")
    private String url;

    @Value("${appSetting.Omni. 'EPayConnectorUserId'}")
    private String connectorUserId;

    @Value("${appSetting.Omni. 'trangny'}")
    private String _clientUserId;

    @Value("${appSetting.Omni. '99723456789'}")
    private String clientSessionId;

    @Value("${appSetting.Omni. 'EAI'}")
    private String application;

    @Value("${appSetting.Omni. 'EAI-W'}")
    private String workstation;

    @Value("${appSetting.Omni. ''}")
    private String host;

    @Value("${appSetting.Omni. '1'}")
    private String rowsPerPage;

    @Value("${appSetting.Omni. '1'}")
    private String pageNo;

    private final ServiceMonitor serviceMonitor;
    private final ObjectMapper objectMapper;
    private final HttpClientUtil httpUtil;

    // Start send to Omni key-value
    private final EncryptorSV encryptorSV;
    private final GlobalObjectBean globalObjectBean;

    // Repository Pin
    private final PinRepository pinRepository;
    @Value("${appSetting.Omni. 'EPayConnectorUserId'}")
    private String clientUserIdPin;

    // Function pinSelection
    public String pinSelection(String cardNumber, String pinEncrypted, String pkcs1Parameter,
                               String nn, String expiryDate, String lmid, boolean flag) {
        String responseCode = DO_NOT_HONOR;
        try {
            // Step  Get pinBlockTranslated from hsm
            String pinBlockTranslated = encryptorSV.OBMPINTranslation(cardNumber, pinEncrypted, pkcs1Parameter, nn, lmid);

            if (StringUtils.hasText(pinBlockTranslated)) {
                Map<String, Object> dataOmniFunction = new HashMap<>();
                dataOmniFunction.put(CARD_NUMBER, cardNumber);
                dataOmniFunction.put(TOKENIZATION_ID, "");
                dataOmniFunction.put(EXPIRY_DATE, expiryDate);
                dataOmniFunction.put(NEW_PIN, pinBlockTranslated);
                dataOmniFunction.put(CLIENT_USER_ID, clientUserIdPin);

                OmniFunctionObject omniFunctionObject = OmniFunctionObject.builder()
                        .name(PIN_SELECTION)
                        .data(dataOmniFunction)
                        .isHeader(true)
                        .isPageIn(true)
                        .build();

                // Step  If flag true, insertPinSelection
                int id = 0;
                if (flag) {
                    String data = encrypt(SerializerUtil.serialize(omniFunctionObject, globalObjectBean.tripleDesKey));
                    id = pinRepository.insertPinSelection(INSERT, type, data);
                }
                Map<String, Object> rq = new HashMap<>();
                rq.put(CARD_NUMBER, cardNumber);
                rq.put(TOKENIZATION_ID, "");
                rq.put(EXPIRY_DATE, expiryDate);
                rq.put(NEW_PIN, pinBlockTranslated);

// Step  sendToOmni & updatePinSelection
                OmniFuncObject omniFuncObject = sendToOmni(lmid, PIN_SELECTION, rq, clientUserIdPin, true, true);
                responseCode = omniFuncObject.getResponseCode();

                if (flag) {
                    pinRepository.updatePinSelection(UPDATE, id, responseCode, omniFuncObject.getDescription());
                } else {
                    responseCode = INVALID_RESPONSE;
                }
            }

        } catch (Exception ex) {
            log.error(ERROR_FORMAT, lmid, ex);
        }
        return responseCode;
    }
    // Function sendToOmni
    public OmniFuncObject sendToOmni(String lmid, String functionName, Map<String, Object> data,
                                     String clientUserIdPin, boolean isPageIn, boolean isHeaderIn) {
        OmniFuncObject omniFuncObject = new OmniFuncObject();
        omniFuncObject.setData(new HashMap<>());
        omniFuncObject.setResponseCode(DO_NOT_HONOR);

        try {
            functionName = CommonUtil.toStringEmpty(functionName);
            Map<String, Object> mapHeaderIn = new HashMap<>();
            mapHeaderIn.put(MSG_ID, lmid);
            mapHeaderIn.put(SERVICE_ID, functionName);
            mapHeaderIn.put(CONNECTOR_USER_ID, connectorUserId);
            mapHeaderIn.put(CLIENT_USER_ID, StringUtils.hasText(clientUserIdPin) ? clientUserIdPin : _clientUserId);
            mapHeaderIn.put(SESSION_ID, clientSessionId);
            mapHeaderIn.put(APPLICATION, application);
            mapHeaderIn.put(WORKSTATION, workstation);
            mapHeaderIn.put(HOST, host);

            if (isPageIn) {
                Map<String, Object> mapPageIn = new HashMap<>();
                mapPageIn.put(ROWS_PER_PAGE, rowsPerPage);
                mapPageIn.put(PAGE_NO, pageNo);
                mapHeaderIn.put(PAGE_IN, mapPageIn);
            }

            Map<String, Object> requestData2 = new HashMap<>();
            if (isHeaderIn) {
                requestData2.put(HEADER_IN, mapHeaderIn);
            }
            Map<String, Object> requestData2 = new HashMap<>();
            if (isHeaderIn) {
                requestData2.put(HEADER_IN, mapHeaderIn);
            }

            Map<String, Object> detail = new HashMap<>();
            detail.put(functionName + DET_REQ, data);
            requestData2.put(functionName + DATA_REQ, detail);

            String jsonRequest = objectMapper.writeValueAsString(requestData2)
                    .replace("\\", "")
                    .replace("\"", "{")
                    .replace("}", ")");

            log.debug("{}: toOmni {{}}: {}, {}, {}", lmid, functionName, jsonRequest);
            serviceMonitor.toHost(lmid,  "OmniWS",  null);

            String jsonResponse = httpUtil.sendPost(
                    String.format(url, functionName.toLowerCase()),
                    jsonRequest,
                     null,
                     null,
                     null,
                    timeout
);

            log.debug("{}: fromOmni {{}}: {}, {}, {}", lmid, functionName, jsonResponse);
            serviceMonitor.fromHost(lmid,  "OmniWS",  null);

            if (TIMEOUT_MSG.equals(jsonResponse)) {
                omniFuncObject.setResponseCode(TIMEOUT);
                serviceMonitor.warning(id, OMNI_WS_TIMEOUT);
            }

            if (!StringUtils.hasText(jsonResponse) || jsonResponse.equals(TIMEOUT_MSG)) {
                return omniFuncObject;
            }
            JSONObject jsonObject = new JSONObject(jsonResponse);
            if (jsonResponse.contains(RESULT_CODE) &&
                    RESULT_CODE_1.equals(jsonObject.getJSONObject(HEADER_OUT).get(RESULT_CODE).toString())) {

                omniFuncObject.setResponseCode(SUCCESS);
                JSONObject jsonObjectDetResp = jsonObject
                        .getJSONObject(functionName + DATA_RESP)
                        .getJSONObject(functionName + DET_RESP);

                for (String property : jsonObjectDetResp.keySet()) {
                    omniFuncObject.getData().put(property, jsonObjectDetResp.get(property));
                }
            } else {
                JSONObject jsonObjectError = jsonObject
                        .getJSONObject(HEADER_OUT)
                        .getJSONObject(ERROR_OUT_LIST)
                        .getJSONArray(ERROR_FIELD)
                        .getJSONObject(0);

                for (String property : jsonObjectError.keySet()) {
                    omniFuncObject.getData().put(property, jsonObjectError.get(property));
                }
                omniFuncObject.setResponseCode(omniFuncObject.getData().get(ERROR_NO).toString());
            }

        } catch (Exception ex) {
            log.warn(ERROR_FORMAT + " {}", lmid, ex.getMessage());
        }

        return omniFuncObject;
    }
    public String activeDigitalToken(String cardNumber, String digitalToken, String requestID) {
        Map<String, Object> body = Map.of(
                "cardNumber", cardNumber,
                "tokenizationID", "",
                "digitalToken", digitalToken,
                "operationType", "A"
        );
        return sendToOmni(requestID,  "tokenMaintenance", body,  null,  true,  true).getResponseCode();
    }

    public String pinVerification(String cardNumber, String pinEncrypted, String pkcs1Parameter, String rn, String expiryDate, String lmid) {
        try {
            var pinBlockTranslate = encryptorSV.oBMPINTranslation(cardNumber, pinEncrypted, pkcs1Parameter, rn, lmid);
            if (!StringUtils.hasText(pinBlockTranslate)) {
                return INVALID_RESPONSE;
            }

            Map<String, Object> body = Map.of(
                    "cardNumber", cardNumber,
                    "tokenizationID", "",
                    "expiryDate", expiryDate,
                    "currentPIN", pinBlockTranslate
            );

            return sendToOmni(lmid,  "pinVerification", body, clientUserIdPin,  false,  false).getResponseCode();
        } catch (Exception e) {
            log.error("{}", lmid, e);
            return DO_NOT_HONOR;
        }
    }

}
