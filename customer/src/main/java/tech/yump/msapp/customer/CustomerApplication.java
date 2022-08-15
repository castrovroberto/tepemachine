package tech.yump.msapp.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(
        scanBasePackages = {
                "tech.yump.msaap",
                "tech.yump.msapp.amqp",
                "tech.yump.msapp.customer"
        }
)
@EnableEurekaClient
@EnableFeignClients(
        basePackages = "tech.yump.msapp.clients"
)
public class CustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerApplication.class, args);
    }
}
