package com.ewallet.caches;

import com.ewallet.objects.Constants;
import com.ewallet.objects.RequestObject;
import org.springframework.stereotype.Component;

@Component
public class RequestCache extends BaseCaches<RequestObject> {
    public RequestCache() {
        this.setPrefixKey(Constants.PREFIX_REQUEST_OBJECT_CACHE);
        this.setDuration(360000 / 1000);
        this.setUseRedis(true);
    }
}
