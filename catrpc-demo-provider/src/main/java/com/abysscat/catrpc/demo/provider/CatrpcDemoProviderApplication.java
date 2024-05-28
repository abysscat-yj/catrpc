package com.abysscat.catrpc.demo.provider;

import com.abysscat.catconfig.client.annocation.EnableCatConfig;
import com.abysscat.catrpc.core.api.RpcRequest;
import com.abysscat.catrpc.core.api.RpcResponse;
import com.abysscat.catrpc.core.config.ProviderConfig;
import com.abysscat.catrpc.core.config.ProviderProperties;
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
@EnableCatConfig
@Slf4j
public class CatrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatrpcDemoProviderApplication.class, args);
    }

//    /**
//     * apollo配置监听器
//     */
//    @Bean
//    ApolloChangedListener listener(){
//        return new ApolloChangedListener();
//    }

    @Autowired
    SpringBootTransport transport;

    @Autowired
    UserService userService;

    @Autowired
    ProviderProperties providerProperties;

    @RequestMapping("/ports")
    public RpcResponse<String> ports(@RequestParam("ports") String ports) {
        userService.setTimeoutPorts(ports);
        RpcResponse<String> response = new RpcResponse<>();
        response.setStatus(true);
        response.setData("OK:" + ports);
        return response;
    }

    @RequestMapping("/metas")
    public RpcResponse<Object> metas() {
        RpcResponse<Object> response = new RpcResponse<>();
        response.setStatus(true);
        response.setData(providerProperties.getMetas());
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


//            System.out.println("Provider Case 1. >>===[复杂测试：测试流量并发控制]===");
//            for (int i = 0; i < 120; i++) {
//                try {
//                    Thread.sleep(1000);
//                    RpcResponse<Object> r = transport.invoke(request);
//                    System.out.println(i + " ***>>> " +r.getData());
//                } catch (RpcException e) {
//                    // ignore
//                    System.out.println(i + " ***>>> RpcException: " +e.getMessage() + " -> " + e.getCode());
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
        };
    }

}
