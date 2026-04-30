package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class PaymentDTO {

    private Long paymentId;
//    private Long addressId;
    private String paymentMethod;
    private String pgStatus;
    private String pgPaymentId;
    private String pgName;
    private String pgResponseMessage;
}
