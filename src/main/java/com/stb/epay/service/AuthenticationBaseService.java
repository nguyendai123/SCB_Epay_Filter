package com.stb.epay.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stb.epay.caches.CardObjectCache;
import com.stb.epay.dto.CardObject;
import com.stb.epay.dto.authentication.AuthDescProperties;
import com.stb.epay.dto.authentication.accountverifypassword.AccountVerificationPassword;
import com.stb.epay.dto.authentication.authverification.AuthVerification;
import com.stb.epay.dto.authentication.verification.VerificationAuthObject;
import com.stb.epay.encryptor.application.service.EncryptorSV;
import com.stb.epay.http_adapter.auth.utils.HttpClientUtil;
import com.stb.epay.http_adapter.service.AuthenticationService;
import com.stb.epay.lib.common.CommonUtil;
import com.stb.epay.lib.crypto.TripleDESHelper;
import com.stb.epay.objects.Constants;
import com.stb.epay.objects.GlobalObjectBean;
import com.stb.epay.repository.EWalletDAO;
import com.stb.epay.service.omni.OmniCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.stb.epay.objects.Constants.AuthenType.*;
import static com.stb.epay.objects.Constants.Authentication.*;
import static com.stb.epay.objects.Constants.EWalletResponseCode.CAN_NOT_SEND_NEXT_PROCESS;
import static com.stb.epay.objects.Constants.EWalletResponseCode.SUCCESS;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationBaseService {

    private final EWalletDAO eWalletDAO;
    private final OmniCardService omniCardService;
    private final HttpClientUtil httpClientUtil;
    private final CardObjectCache cardCache;
    private final EncryptorSV encryptorSV;
    private final AuthDescProperties authDesc;
    private final GlobalObjectBean globalObjectBean;
    private final ObjectMapper objectMapper;
    private final AuthenticationService authenticationService;

    @Value("${appSetting.MNSSmsUrl:' '}")
    private String mnsSmsUrl;

    @Value("${appSetting.MNSSmsProvider: ''}")
    private String mnsSmsProvider;

    @Value("${appSetting.NASSmsUrl: ''}")
    private String nasSmsUrl;

    @Value("${appSetting.NASSmsUser: ''}")
    private String nasSmsUser;

    @Value("${appSetting.NASSmsPassword: ''}")
    private String nasSmsPassword;

    @Value("${appSetting.NASSmsTimeout: '0'}")
    private String nasSmsTimeout;

    public List<AuthDesc> getAuthTypeDesc() {
        try {
            return authDesc.handleData();
        } catch (Exception ex) {
            log.error(">> ", ex);
            return new ArrayList<>();
        }
    }

    @PostConstruct
    private void setNasSmsPassword() {
        String nasSmsPasswordDecrypt = encryptorSV.decrypt(nasSmsPassword);
        if (StringUtils.hasText(nasSmsPasswordDecrypt)) {
            log.info("NASSmsPassword - Decrypt ok");
            nasSmsPassword = nasSmsPasswordDecrypt;
        } else {
            log.error(">> NASSmsPassword - Failed decrypt");
        }
    }

    public AuthVerification authVerificationAuth(VerificationAuthObject verificationAuthObject) {
        AuthVerification authVerification = AuthVerification.builder()
                .verification(false)
                .respCode("")
                .build();

        String authType = (SMART_OTP.equals(verificationAuthObject.getAuthType())) ? INTER_APP : verificationAuthObject.getAuthType();

        if (M_PASS.equals(authType)) {
            String rc = pMassVerifications(verificationAuthObject.getMobileNo(),
                    verificationAuthObject.getEWalletID(),
                    verificationAuthObject.getPks1Parameter(),
                    verificationAuthObject.getRrn());

            if (SUCCESS.equals(rc)) {
                authVerification.setVerification(true);
                log.debug("{}: Response code Verification mPass: {}", verificationAuthObject.getRequestID(), rc);
                return authVerification;
            }
        }

        if (Arrays.asList(SMS_OTP, HARD_TOKEN, M_CODE, M_CONNECTED, ADV_TOKEN, INTER_APP)
                .contains(verificationAuthObject.getAuthType())) {

            String authCode = "";
            String transactionCode = "";

            if (!M_CONNECTED.equals(verificationAuthObject.getAuthType())) {
                authCode = TripleDESHelper.decrypt(verificationAuthObject.getOtp(), globalObjectBean.tripleDesKey);
            }

            if (ADV_TOKEN.equals(verificationAuthObject.getAuthType())) {
                String[] temp = authCode.split("#");
                authCode = temp[0].replace(" ", "");
                transactionCode = temp[1].replace(" ", "");
                getDataFromAuth(verificationAuthObject, authVerification, authCode, transactionCode);
                log.debug("{}: Response code Verification Auth: {}", verificationAuthObject.getRequestID(), authVerification.getRespCode());
            } else {
                log.error("{}: Invalid auth type", verificationAuthObject.getRequestID());
                authVerification.setRespCode(Constants.EWalletResponseCode.INVALID_INPUT);
            }
        }

        return authVerification;
    }

    public String mPassVerifications(
            String mobileNo,
            String eWalletId,
            String mPassEncrypted,
            String pkcs1Parameter,
            String rn,
            String requestId) {

        // Step 1: Get account verify password, setup data
        AccountVerificationPassword accountVerifyPassword =
                eWalletDAO.getAccountVerifyPassword(eWalletId, mobileNo);

        if (accountVerifyPassword == null) {
            return CAN_NOT_SEND_NEXT_PROCESS;
        }

        String passwordOffset = CommonUtil.toStringEmpty(accountVerifyPassword.getPasswordOffset());
        String isUpdated = CommonUtil.toStringEmpty(accountVerifyPassword.getIsUpdate());
        String cardToken = CommonUtil.toStringEmpty(accountVerifyPassword.getCardToken());
        String resCode;

        // Step 2: Verify mPass with HSM
        log.debug("{}: toHSM mPassVerification", requestId);

        if (!IS_UPDATED.equals(isUpdated)) {
            resCode = encryptorSV.oBMVerifyPasswords(
                    mPassEncrypted,
                    pkcs1Parameter,
                    rn,
                    passwordOffset,
                    CommonUtil.toStringEmpty(accountVerifyPassword.getParameter2()),
                    CommonUtil.toStringEmpty(accountVerifyPassword.getParameter1()),
                    requestId
            );
        } else {
            resCode = encryptorSV.oBMVerifyPasswords(
                    mPassEncrypted,
                    pkcs1Parameter,
                    rn,
                    passwordOffset,
                    eWalletId,
                    cardToken,
                    requestId
            );
        }

// Step 3: Check !IS_UPDATED and update data
        if (SUCCESS.equals(resCode) && !IS_UPDATED.equals(isUpdated) && StringUtils.hasText(cardToken)) {
            // Update PasswordOffset & database
            String newPasswordOffset = encryptorSV.oBMSetPassword(
                    mPassEncrypted,
                    pkcs1Parameter,
                    rn,
                    eWalletId,
                    cardToken,
                    requestId
            );
            eWalletDAO.updatePasswordOffset(mobileNo, eWalletId, newPasswordOffset, 1);

            CardObject cardObject = cardCache.get(cardToken);
            if (cardObject != null && SUCCESS.equals(omniCardService.pinSelection(
                    cardObject.getCardNo(),
                    mPassEncrypted,
                    pkcs1Parameter,
                    rn,
                    cardObject.getExpiryDate(),
                    requestId,
                    false))) {

                log.debug("{}: Select new Pin success", requestId);
            } else {
                log.debug("{}: Select new Pin error", requestId);
            }
            return resCode; // Return the response code
        }
        // Step 4: Convert respCode
        switch (resCode) {
            case SUCCESS:
            case TIMEOUT:
                break; // No action needed, process continues normally

            case HONOR_IDENTIFICATION:
                log.debug(M_P_VERIFICATION_CONVERT, requestId, resCode, VERIFY_FAIL);
                resCode = VERIFY_FAIL; // Set response code to verify fail
                break;

            default:
                log.debug(M_P_VERIFICATION_CONVERT, requestId, resCode, CAN_NOT_SEND_NEXT_PROCESS);
                resCode = CAN_NOT_SEND_NEXT_PROCESS; // Handle unexpected response codes
                break;
        }

        return resCode; // Return the final response code
    }
    // Step 1: Send SMS_MODE_01
// Step 2: Send SMS_MODE_02
    public Boolean sendSMS(String lnid, String modeSendSMS, String refNumber, String mobileNo, String content, String priority, String countryCode) {
        try {
            Map<String, String> header = new HashMap<>();
            String usernameColonPassword = nassMsUser + ":" + nassMsPassword;
            String basicAuth = BASIC_AUTH + " " + Base64.getEncoder().encodeToString(usernameColonPassword.getBytes(StandardCharsets.UTF_8));
            header.put(AUTHORIZATION, basicAuth);
            int timeout = Integer.parseInt(nassSmsTimeout);

            if (SMS_MODE_01.equals(modeSendSMS)) {
                SmsObject smsObject = SmsObject.builder()
                        .provider(mnsSmsProvider)
                        .moSequence("") // Add logic for sequence if needed
                        .referenceID(refNumber)
                        .from(WL)
                        .country(countryCode)
                        .to(mobileNo)
                        .body(content)
                        .priority(priority)
                        .build();

                String jsonRequest = objectMapper.writeValueAsString(smsObject);
                log.debug("Send to MNS Gateway: {}", lnid, jsonRequest);
                String httpResponse = httpClientUtil.sendPost(nmsSmsUrl, jsonRequest, null, header, timeout);
                log.debug("Response from MNS: {}", lnid, objectMapper.writeValueAsString(httpResponse));
            } else if (SMS_MODE_02.equals(modeSendSMS)) {
                SmsNasDataObject smsDataObject = SmsNasDataObject.builder()
                        .from(CARD)
                        .country(countryCode)
                        .to(mobileNo)
                        .text(content)
                        .service(priority) // Adjust as per your requirements
                        .appName(SCP)
                        .build();

                String jsonRequest = objectMapper.writeValueAsString(smsDataObject);
                log.debug("Send to NAS: {}", lnid, jsonRequest);
                String httpResponse = httpClientUtil.sendPost(nasUrl, jsonRequest, null, header, timeout);
                log.debug("Response from NAS: {}", lnid, objectMapper.writeValueAsString(httpResponse));
                // Building the SMS NAS object
                SmsNasObject smsObject = SmsNasObject.builder()
                        .requestID(refNumber) // Unique identifier for the request
                        .functionName(SEND_SMS) // Function being executed
                        .requestDateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) // Current timestamp
                        .data(smsDataObject) // SMS data
                        .build();

// Convert the SMS object to JSON
                String jsonRequest = objectMapper.writeValueAsString(smsObject);
                log.debug("To NAS queue: {}", lnid, jsonRequest); // Log the request to NAS

// Sending the request to NAS and capturing the response
                String httpResponse = httpClientUtil.sendPost(nasSmsUrl, jsonRequest, null, header, timeout);
                log.debug("From NAS: {}", lnid, objectMapper.writeValueAsString(httpResponse)); // Log the response from NAS
            }
                return true; // Indicate success
            } catch (Exception ex) {
                log.error("Error in sending SMS: {}", lnid, ex); // Log any errors encountered
                return false; // Indicate failure
            }
    }
    private void getDataFromAuth(VerificationAuthObject verificationAuthObject, AuthVerification authVerification, String authCode, String transactionCode) {
        String functionName;

        // Determine the function name based on the auth type
        if (INTER_APP.equals(verificationAuthObject.getAuthType())) {
            functionName = AUTH.VERIFICATION_INTER_APP;
        } else {
            functionName = !CONNECTED.equals(verificationAuthObject.getAuthType()) ?
                    AUTH_VERIFICATION : AUTH_VERIFICATION_M_CONNECTED;
        }

        // Build the authentication request
        AuthResponse authResponse = authenticationService.sendAuthRequest(AuthRequest.builder()
                .requested(verificationAuthObject.getRequestID())
                .country(verificationAuthObject.getCountryCode())
                .mobile(verificationAuthObject.getMobileNo())
                .fnNumber(verificationAuthObject.getFnNumber())
                .otp(authCode)
                .content(verificationAuthObject.hasText(verificationAuthObject.getAuthDescription()))
                ? (Arrays.asList(N_CONNECTED, INTER_APP).contains(verificationAuthObject.getAuthType()))
                ? verificationAuthObject.getInterAppBlob() : ""
                : ""
                .transactionId(transactionCode)
                .build());

        // Set response code and update verification status
        authVerification.setResponseCode(authResponse.getResponseCode());
        if (SUCCESS.equals(authResponse.getResponseStatus())) {
            authVerification.setVerified(true);
        }
    }
}