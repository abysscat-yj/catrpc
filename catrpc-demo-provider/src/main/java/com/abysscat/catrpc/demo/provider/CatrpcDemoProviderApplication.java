package com.abysscat.catrpc.demo.provider;

import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import com.abysscat.catrpc.core.provider.ProviderConfig;
import com.abysscat.catrpc.core.provider.ProviderInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
public class CatrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatrpcDemoProviderApplication.class, args);
    }

    // 使用HTTP + JSON 来实现序列化和通信

    @Autowired
    ProviderInvoker providerInvoker;

    @RequestMapping("/")
    public RpcResponse<Object> invoke(@RequestBody RpcRequest request) {
        return providerInvoker.invoke(request);
    }

    @Bean
    ApplicationRunner providerRun() {
        return x -> {
            RpcRequest request = new RpcRequest();
            request.setService("com.abysscat.catrpc.demo.api.UserService");
            request.setMethodSign("bd8865f6f7cf984189f489fa16c34db3");
            request.setArgs(new Object[]{123});

            RpcResponse<Object> response = invoke(request);
            System.out.println("providerRun return: " + response.getData());
        };
    }

}
