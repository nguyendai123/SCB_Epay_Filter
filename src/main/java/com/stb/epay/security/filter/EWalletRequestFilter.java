package com.stb.epay.security.filter;

import com.stb.epay.security.wrapper.RequestWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stb.epay.lib.crypto.TripleDESHelper;
import com.stb.epay.objects.MerchantObject;
import com.stb.epay.objects.base.EWalletRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.Base64;

import static com.stb.epay.objects.Constants.Request.*;

@Slf4j
public class EWalletRequestFilter {
    private EWalletRequestFilter() {

    }

    // step 1: Verify function
    // step 2: Verify signature
    // step 3: Decrypt data
    @SneakyThrows
    public static void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            HttpServletRequest req,
            HttpServletResponse res,
            String username,
            String password,
            EWalletRequest request,
            MerchantObject merchant,
            ObjectMapper objectMapper,
            FilterChain chain,
            EwalletReplayAttackFilter attackFilter
    ) {

        // step 1: Verify function
        if (!merchant.getFunctions().containsKey(request.getFunctionName())) {
            log.debug("From {} - {}: Invalid function with route {} and function {}",
                    merchant.getMerchantName(), request.getRequestID(),
                    req.getRequestURI(), request.getFunctionName());
            attackFilter.sendError(res, HttpStatus.BAD_REQUEST, "Invalid function");
            return;
        }

        // step 2: Verify signature
        if (!StringUtils.hasText(request.getData())) {
            log.debug("From {} - {}: Data required", merchant.getMerchantName(), request.getRequestID());
            attackFilter.sendError(res, HttpStatus.BAD_REQUEST, "Invalid request");
            return;
        }

        // step 3: Decrypt data
        if (merchant.isSession()) {
            log.error(">>> " + "{} - Not implement SessionEncrypt for merchant", merchant.getMerchantName());
            attackFilter.sendError(res, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        } else {
            // pass no-encrypt
            if (!StringUtils.hasText(req.getHeader(NO_CRYPT))) {
                request.setData(merchant.isEncryption() ?
                        TripleDESHelper.decrypt(request.getData(), password) :
                        new String(Base64.getDecoder().decode(request.getData())));
            }
            req.setAttribute(PASSWORD, password);
            req.setAttribute(USER_NAME, username);

            ((RequestWrapper) servletRequest).setBody(objectMapper.writeValueAsString(request));

            chain.doFilter(servletRequest, servletResponse);
        }

    }
}
