package com.ewallet.caches;

import com.ewallet.objects.Constants;
import com.ewallet.objects.MerchantObject;
import jakarta.annotation.PostConstruct;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ewallet.objects.Constants.SecurityProps.NOOP;

@Component
public class MerchantCache extends BaseCaches<MerchantObject> {


    private final Map<String, MerchantObject> merchantMap = new HashMap<>();

    private final Map<String, UserDetails> userDetailsMap = new HashMap<>();


    public MerchantCache() {
        this.setPrefixKey(Constants.PREFIX_MERCHANT_CACHE);
    }

    @PostConstruct
    public void initMerchantObject() {
        Map<String, MerchantObject.ServiceObject> mapServ = new HashMap<>();
        mapServ.put("sfsdfsd", null);
        Map<String, MerchantObject.FunctionObject> mapFunc = new HashMap<>();
        mapFunc.put("eWalletHandShake", null);
        mapFunc.put("eWalletHandShake2", null);
        mapFunc.put("eWalletHandShake3", null);
        mapFunc.put("eWalletHandShake4", null);
        mapFunc.put("eWalletHandShake5", null);
        mapFunc.put("eWalletHandShake6", null);
        mapFunc.put("eWalletHandShake7", null);
        mapFunc.put("eWalletHandShake8", null);

        MerchantObject merchant = MerchantObject.builder()
                .username("admin")
                .password("123456")
                .status(true)
                .services(mapServ)
                .functions(mapFunc)
                .build();
        UserDetails user = User.builder()
                .username(merchant.getUsername())
                .password(NOOP + merchant.getPassword())
                .authorities(merchant.getServices().keySet().stream()
                        .map(SimpleGrantedAuthority::new).collect(Collectors.toList()))
                .build();
        merchantMap.put(merchant.getUsername(), merchant);
        userDetailsMap.put(merchant.getUsername() + ":" + merchant.getPassword(), user);
    }

    @Override
    public MerchantObject get(String key) {
        return merchantMap.get(key);
    }


    public Map<String, UserDetails> getUserDetailsMap() {
        return userDetailsMap;
    }
}
