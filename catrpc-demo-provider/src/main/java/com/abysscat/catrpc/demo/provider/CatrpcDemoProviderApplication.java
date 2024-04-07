package com.abysscat.catrpc.demo.provider;

import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import com.abysscat.catrpc.core.provider.ProviderConfig;
import com.abysscat.catrpc.core.transport.SpringBootTransport;
import com.abysscat.catrpc.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/3/7 1:04
 */
@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
@Slf4j
public class CatrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatrpcDemoProviderApplication.class, args);
    }

    @Autowired
    SpringBootTransport transport;

    @Autowired
    UserService userService;

    @RequestMapping("/ports")
    public RpcResponse<String> ports(@RequestParam("ports") String ports) {
        userService.setTimeoutPorts(ports);
        RpcResponse<String> response = new RpcResponse<>();
        response.setStatus(true);
        response.setData("OK:" + ports);
        return response;
    }

    @Bean
    ApplicationRunner providerRun() {
        return x -> {
            RpcRequest request = new RpcRequest();
            request.setService("com.abysscat.catrpc.demo.api.UserService");
            request.setMethodSign("bd8865f6f7cf984189f489fa16c34db3");
            request.setArgs(new Object[]{123});

            RpcResponse<Object> response = transport.invoke(request);
            log.info("providerRun return: " + response.getData());
        };
    }

}
