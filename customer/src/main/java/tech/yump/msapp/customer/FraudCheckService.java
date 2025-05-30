package tech.yump.msapp.customer;

public interface FraudCheckService {
    boolean isFraudulent(Integer customerId);
} 