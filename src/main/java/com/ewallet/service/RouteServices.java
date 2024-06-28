package com.ewallet.service;

import com.ewallet.caches.MerchantCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ewallet.constants.ResponseCode;
import com.ewallet.objects.Constants.EWalletResponseCode;
import com.ewallet.objects.LanguageObject;
import com.ewallet.objects.MerchantObject;
import com.ewallet.objects.ProcessObject;
import com.ewallet.objects.base.EWalletRequest;
import com.ewallet.objects.base.EWalletResponse;
import com.ewallet.util.SerializerUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.ewallet.objects.Constants.Language.VI;
import static com.ewallet.objects.Constants.LogFormat.*;
import static com.ewallet.objects.Constants.Request.USER_NAME;

@Service
@Slf4j
@RequiredArgsConstructor
public class RouteServices {

    private final MerchantCache merchantCache;

    private final ObjectMapper objectMapper;


    private final AsyncService asyncService;

    // step 1: convert processData and send monitor object.
    // step 2: process route function.
    // step 3: log time response, send end serviceMonitor.
    // step 4: check response code exists.
    // step 5: write log over time.
    public EWalletResponse routeFunction(EWalletRequest request,
                                         HttpSession session,
                                         String contentType,
                                         HttpServletRequest httpRequest
    ) {
        long start = System.currentTimeMillis();
        // step 1: convert process data and send monitor object
        EWalletResponse response = createResponse(request);
        ProcessObject processObject = createProcessObject(request, response, httpRequest, session, contentType);

        // step 2: process route function
        try {
            log.debug(REQUEST_FORMAT,
                    processObject.getLmid(), request.getFunctionName(),
                    request.getRequestID(), objectMapper.writeValueAsString(request));
            response = executeFunctionFactoryAsync(processObject);

        } catch (Exception ex) {
            log.error(ERROR_FORMAT, processObject.getLmid(), ex);
            response.setDescription(ResponseCode.ERROR.getName());
            response.setRespCode(ResponseCode.ERROR.getCode());
        }

        // step 3: log time response, send end serviceMonitor.
        long timeProcess = System.currentTimeMillis() - start;
        log.debug(DEBUG_FORMAT,
                processObject.getLmid(), timeProcess, response.getFunctionName(),
                response.getRequestID(), SerializerUtil.serialize(response));

        // step 4: check response code exists.


        // step 5: write log over time.
        if (timeProcess > 120000) {
            log.debug(TIME_OVER_FORMAT,
                    processObject.getLmid(), timeProcess, processObject.getMerchant().getMerchantName(),
                    processObject.getFunctionName(), processObject.getRequestID());
        }
        return response;
    }

    private EWalletResponse createResponse(EWalletRequest request) {
        return EWalletResponse.builder()
                .data(StringUtils.EMPTY)
                .description(StringUtils.EMPTY)
                .functionName(request.getFunctionName())
                .respCode(StringUtils.EMPTY)
                .requestDateTime(request.getRequestDateTime())
                .requestID(request.getRequestID())
                .sessionID(request.getSessionID())
                .build();
    }

    private ProcessObject createProcessObject(EWalletRequest request,
                                              EWalletResponse response,
                                              HttpServletRequest httpRequest,
                                              HttpSession session,
                                              String contentType
    ) {
        MerchantObject merchant = merchantCache.get(String.valueOf(httpRequest.getAttribute(USER_NAME)));
        return ProcessObject.builder()
                .lmid(generateLMID())
                .merchant(merchant)
                .sessionID(session.getId())
                .requestID(request.getRequestID())
                .functionName(request.getFunctionName())
                .contentType(contentType)
                .requestObject(request)
                .responseObject(response)
                .languageID(readLanguageObject(request.getData()))
                .createdDate(new Date())
                .messageError(null)
                .build();
    }

    private String readLanguageObject(String requestData) {
        try {
            LanguageObject languageObject = SerializerUtil.deserialize(requestData, LanguageObject.class);
            return Objects.nonNull(languageObject) ? languageObject.getLanguageID() : VI;
        } catch (Exception ex) {
            log.warn("FAILED while readLanguageObject from requestData {}", requestData);
            return VI;
        }
    }

    // CompletableFuture - timeout function
    private EWalletResponse executeFunctionFactoryAsync(ProcessObject processObject) {
        try {
            CompletableFuture<EWalletResponse> completableFuture = asyncService.execute(processObject);
            EWalletResponse eWalletResponse = completableFuture.get(getTimeout(processObject), TimeUnit.MILLISECONDS);
            if (eWalletResponse == null) {
                String response = processObject.responseError(EWalletResponseCode.DO_NOT_HONOR);
                return SerializerUtil.deserialize(response, EWalletResponse.class);
            }
            return eWalletResponse;
        } catch (TimeoutException ext) {
            log.warn(LMID_LOG_FORM_2, processObject.getLmid(), ext.toString());
            String response = processObject.responseError(EWalletResponseCode.TIMEOUT);
            return SerializerUtil.deserialize(response, EWalletResponse.class);
        } catch (InterruptedException | ExecutionException ex) {
            if (ex instanceof InterruptedException) {
                Thread thread = Thread.currentThread();
                if (thread != null) {
                    thread.interrupt();
                }
            }
            log.error(ERROR_FORMAT, processObject.getLmid(), ex);
            String response = processObject.responseError(EWalletResponseCode.DO_NOT_HONOR);
            return SerializerUtil.deserialize(response, EWalletResponse.class);
        }
    }

    private int getTimeout(ProcessObject processObject) {
        return 120000;
    }

    private static final Random _rn = new SecureRandom();


    public static String generateLMID() {
        StringBuilder lmid = new StringBuilder();

        for (int i = 0; i < 6; ++i) {
            String VALID_CHAR = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            int index = (int) (_rn.nextFloat() * (float) VALID_CHAR.length());
            lmid.append(VALID_CHAR.charAt(index));
        }

        return "AAAAAA".contentEquals(lmid) ? generateLMID() : lmid.toString();
    }
}
