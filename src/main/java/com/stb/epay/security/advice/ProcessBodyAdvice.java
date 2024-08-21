package com.stb.epay.security.advice;

import com.stb.epay.lib.crypto.TripleDESHelper;
import com.stb.epay.objects.base.EWalletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Base64;
import java.util.Objects;

import static com.stb.epay.objects.Constants.Request.*;

@ControllerAdvice
@Slf4j
public class ProcessBodyAdvice implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @SneakyThrows
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof EWalletResponse eWalletResponse) {

            if (StringUtils.hasText(eWalletResponse.getData())) {
                String pw = (String) ((ServletServerHttpRequest) request).getServletRequest().getAttribute(PASSWORD);

                // Encrypt data
                if (request.getHeaders().get(SESSION_KEY) != null) {
                    // user session key to encrypt data
                    eWalletResponse.setData(TripleDESHelper.encrypt(eWalletResponse.getData(),
                            Objects.requireNonNull(request.getHeaders().get(SESSION_KEY)).get(0)));
                    // remove session key before response
                    request.getHeaders().remove(SESSION_KEY);
                } else if (pw != null) {
                    // user Password to encrypt data
                    // pass no-encrypt
                    if (request.getHeaders().get(NO_CRYPT) == null ||
                            Objects.requireNonNull(request.getHeaders().get(NO_CRYPT)).isEmpty()) {
                        eWalletResponse.setData(TripleDESHelper.encrypt(eWalletResponse.getData(), pw));
                    }
                    // remove Password before response
                    ((ServletServerHttpRequest) request).getServletRequest().removeAttribute(PASSWORD);
                } else {
                    eWalletResponse.setData(Base64.getEncoder().encodeToString(eWalletResponse.getData().getBytes()));
                }
            }

            return eWalletResponse;
        }
        return body;
    }

}
