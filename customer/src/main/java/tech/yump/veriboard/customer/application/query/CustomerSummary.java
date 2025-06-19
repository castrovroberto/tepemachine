package tech.yump.veriboard.customer.application.query;

import lombok.Builder;
import lombok.Data;

/**
 * Read model for customer summary information.
 * Part of CQRS pattern - optimized for query operations.
 */
@Data
@Builder
public class CustomerSummary {
    
    private Integer customerId;
    private String fullName;
    private String email;
    private String registrationStatus;
    private String riskLevel;
} 