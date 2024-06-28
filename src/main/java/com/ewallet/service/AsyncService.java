package com.ewallet.service;

import com.ewallet.constants.ResponseCode;
import com.ewallet.objects.ProcessObject;
import com.ewallet.objects.base.EWalletResponse;
import com.ewallet.util.SerializerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static com.ewallet.objects.Constants.LogFormat.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncService {
    private final ApplicationContext context;

    @Async("asyncExecutor")
    public CompletableFuture<EWalletResponse> execute(ProcessObject processObject) {
        return CompletableFuture.completedFuture(executeFunctionFactory(processObject));
    }

    private EWalletResponse executeFunctionFactory(ProcessObject processObject) {
        long start = System.currentTimeMillis();
        String response;
        try {
            FunctionBase functionBase = (FunctionBase) context.getBean(processObject.getFunctionName());
            response = functionBase.execute(processObject);
            if (response == null) {
                log.error(ERROR_DATA_SERVICE_NULL_FORMAT, processObject.getLmid());
                response = processObject.responseError(ResponseCode.DO_NOT_HONOR.getCode());
            }
        } catch (NoSuchBeanDefinitionException ex) {
            log.debug(FUNCTION_INVALID_FORMAT, processObject.getLmid(), processObject.getFunctionName(), ex);
            response = processObject.responseError(ResponseCode.DO_NOT_HONOR.getCode());
        } catch (Exception ex) {
            log.error(ERROR_FORMAT, processObject.getLmid(), ex);
            response = processObject.responseError(ResponseCode.DO_NOT_HONOR.getCode());
        }
        return SerializerUtil.deserialize(processObject.getContentType(), response, EWalletResponse.class);
    }
}
