package tech.yump.veriboard.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(
        scanBasePackages = {
                "tech.yump.veriboard",
                "tech.yump.veriboard.amqp",
                "tech.yump.veriboard.customer"
        }
)
@EnableFeignClients(
        basePackages = "tech.yump.veriboard.clients"
)
public class CustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerApplication.class, args);
    }
}
