package com.example.accounts.service;

import com.example.accounts.dto.CustomerDetailsDto;

public interface ICustomerService {

    /**
     *
     * @param mobileNumber - Input Mobile Number
     * @param correlationId - Id to trace the request
     * @return Customer Details based on a given mobileNumber
     */

    CustomerDetailsDto fetchCustomerDetails(String mobileNumber, String correlationId);
}
