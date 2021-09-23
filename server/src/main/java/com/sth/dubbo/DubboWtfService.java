package com.sth.dubbo;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

@Service("wtfService") // 用于dubbo:server 的ref 参数
@DubboService(version = "1.0.0", interfaceClass = WtfService.class,
        //registry = "registry1",
        delay = 5000, protocol = "dubbo")
public class DubboWtfService implements WtfService {

    @Override
    public String gfw(String sth) {
        return "wtf, --> " + sth;
    }
}
