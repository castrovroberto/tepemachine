package tech.yump.msapp.customer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> registerCustomer(
            @RequestBody CustomerRegistrationRequest customerRegistrationRequest) {
        
        log.info("New customer registration request: {}", customerRegistrationRequest);
        
        Customer customer = customerService.registerCustomer(customerRegistrationRequest);
        
        Map<String, Object> response = Map.of(
            "message", "Customer registered successfully",
            "customerId", customer.getId(),
            "email", customer.getEmail()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
