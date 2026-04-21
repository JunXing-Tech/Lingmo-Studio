package tech.jxing.lingmostudiobackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
public class LingmoStudioBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LingmoStudioBackendApplication.class, args);
    }

}
