package com.stb.epay.security.filter;

import com.stb.epay.caches.RequestCache;
import com.stb.epay.dto.ResponseError;
import com.stb.epay.dto.ResponseStatus;
import com.stb.epay.security.wrapper.RequestWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stb.epay.caches.MerchantCache;
import com.stb.epay.objects.MerchantObject;
import com.stb.epay.objects.RequestObject;
import com.stb.epay.objects.base.EWalletRequest;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
//import org.apache.hc.core5.http.ContentType;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;

import static com.stb.epay.objects.Constants.LogFormat.*;
import static com.stb.epay.objects.Constants.Request.*;

@Slf4j
@RequiredArgsConstructor
public class EwalletReplayAttackFilter implements Filter {

    private final RequestCache requestCache;

    private final ObjectMapper objectMapper;

    private final MerchantCache merchantCache;

//    private final GlobalObjectBean globalObjectBean;

    // step 1: Check exist basic authentication.
    // step 2: Verify basicAuth.
    // step 3: Verify body EWalletRequest.
    // step 4: Verify merchant.
    // step 5: Check exist requestID.
    // step 6: Verify requestID duplicate.
    // step 7: Cache request to redis.
    // step 8: Decrypt data use 3DES.
    @SneakyThrows
    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain chain) {


        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        // step 1: Check exist basic authentication.
        String authorization = req.getHeader(AUTHORIZATION);
        if (StringUtils.isBlank(authorization)
                || !authorization.toLowerCase().startsWith(BASIC_AUTH)
                || StringUtils.isBlank(authorization.substring(BASIC_LENGTH).trim())
        ) {
            log.debug(INVALID_BASIC_AUTH);
            sendError(res, HttpStatus.UNAUTHORIZED, INVALID_BASIC_AUTH);
            return;
        }

        // step 2: Verify basicAuth
        String credentialPlainText = getCredentialPlainText(authorization);
        if (StringUtils.isBlank(credentialPlainText)) {
            log.debug(INVALID_BASIC_AUTH);
            sendError(res, HttpStatus.UNAUTHORIZED, INVALID_BASIC_AUTH);
            return;
        }
        String[] credentials = credentialPlainText.split(":", 2); // credentials = username:password
        if (credentials.length != 2 || StringUtils.isBlank(credentials[0]) || StringUtils.isBlank(credentials[1])) {
            log.debug(INVALID_BASIC_AUTH);
            sendError(res, HttpStatus.UNAUTHORIZED, INVALID_BASIC_AUTH);
            return;
        }

        UserDetails userDetails = merchantCache.getUserDetailsMap().get(credentialPlainText);
        if (userDetails == null) {
            log.debug(INVALID_BASIC_AUTH);
            sendError(res, HttpStatus.UNAUTHORIZED, INVALID_BASIC_AUTH);
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        userDetails.getPassword(),
                        userDetails.getAuthorities()
                )
        );

        // step 3: Verify body EWalletRequest.
        servletRequest = new RequestWrapper((HttpServletRequest) servletRequest);
        EWalletRequest eWalletRequest = readRequestBody(servletRequest);
        if (Objects.isNull(eWalletRequest)) {
            log.debug(DATA_NOT_MATCH);
            sendError(res, HttpStatus.BAD_REQUEST, DATA_NOT_MATCH);
            return;
        }

        // step 4: Verify merchant.
        String username = credentials[0];
        MerchantObject merchant = merchantCache.get(username);
        if (Objects.isNull(merchant) || !merchant.isStatus()) {
            log.debug("From {} - {}: Invalid merchant", username, eWalletRequest.getRequestID());
            sendError(res, HttpStatus.BAD_REQUEST, INVALID_MERCHANT);
            return;
        }

        // step 5: Check exist requestID.
        if (Objects.isNull(eWalletRequest.getRequestID())) {
            log.debug(INVALID_REQUEST_ID_FORMAT, merchant.getMerchantName(), null);
            sendError(res, HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            return;
        }

        // step 6: Verify requestID duplicate.
        if (Objects.nonNull(requestCache.get(eWalletRequest.getRequestID()))) {
            log.debug(INVALID_REQUEST_ID_FORMAT, merchant.getMerchantName(), eWalletRequest.getRequestID());
            sendError(res, HttpStatus.CONFLICT, DUPLICATE_REQUEST);
            return;
        }

        long currentTime = System.currentTimeMillis();
        long replayAttackTimeout = currentTime - parseDateTime(eWalletRequest.getRequestDateTime());
        if (replayAttackTimeout > 360000) {
            log.debug(INVALID_REQUEST_DATE_TIME_FORMAT, merchant.getMerchantName(), eWalletRequest.getRequestID());
            sendError(res, HttpStatus.CONFLICT, DUPLICATE_REQUEST);
            return;
        }

        // step 7: Cache request to redis.
        RequestObject requestObject = RequestObject.builder()
                .requestID(eWalletRequest.getRequestID())
                .createDateTime(currentTime)
                .build();
        if (Objects.isNull(requestCache.add(eWalletRequest.getRequestID(), requestObject))) {
            log.debug(CANNOT_STORE_REQUEST_FORMAT, merchant.getMerchantName(), eWalletRequest.getRequestID());
            sendError(res, HttpStatus.CONFLICT, DUPLICATE_REQUEST);
            return;
        }

        long end1 = System.currentTimeMillis();
        // step 8: Decrypt data use 3DES.
        EWalletRequestFilter.doFilter(
                servletRequest,
                servletResponse,
                req,
                res,
                username,
                credentials[1],
                eWalletRequest,
                merchant,
                objectMapper,
                chain, this);
        long end2 = System.currentTimeMillis();
    }

    private EWalletRequest readRequestBody(ServletRequest servletRequest) {
        try {
            return objectMapper.readValue(((RequestWrapper) servletRequest).getBody(), EWalletRequest.class);
        } catch (JsonProcessingException ex) {
            log.debug(BODY_NOT_MATCH_FORMAT, ex);
            return null;
        }
    }

    private static long parseDateTime(String str) {
        try {
            DateFormat formatter = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS_X);
            return formatter.parse(str).getTime();
        } catch (Exception e) {
            log.error(">>> INVALID DATETIME");
            return 0;
        }
    }

    public void sendError(HttpServletResponse res, HttpStatus httpStatus, String msg) throws IOException {
        ResponseError body = ResponseError.builder()
                .responseStatus(ResponseStatus.builder()
                        .errorCode(httpStatus.getReasonPhrase())
                        .message(msg)
                        .errors(new ArrayList<>())
                        .build())
                .build();
        sendError(res, httpStatus, body);
    }

    public void sendError(HttpServletResponse res, HttpStatus httpStatus, ResponseError body) throws IOException {

        res.setStatus(httpStatus.value());
        res.getWriter().write(objectMapper.writeValueAsString(body));
        res.setContentType(APPLICATION_JSON);
    }

    private String getCredentialPlainText(String authorization) {
        try {
            String credentialBase64 = authorization.substring(BASIC_LENGTH).trim();
            return new String(Base64.getDecoder().decode(credentialBase64));
        } catch (Exception ex) {
            log.error(">>> Invalid Basic Credential");
            return null;
        }
    }

}
