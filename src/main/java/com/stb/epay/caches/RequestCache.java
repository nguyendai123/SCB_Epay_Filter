package com.stb.epay.caches;

import com.stb.epay.objects.Constants;
import com.stb.epay.objects.RequestObject;
import org.springframework.stereotype.Component;

@Component
public class RequestCache extends BaseCaches<RequestObject> {
    public RequestCache() {
        this.setPrefixKey(Constants.PREFIX_REQUEST_OBJECT_CACHE);
        this.setDuration(360000 / 1000);
        this.setUseRedis(true);
    }
}
