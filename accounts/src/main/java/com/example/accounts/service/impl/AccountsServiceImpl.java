package com.example.accounts.service.impl;

import com.example.accounts.constants.AccountsConstants;
import com.example.accounts.dto.AccountsDto;
import com.example.accounts.dto.AccountsMsgDto;
import com.example.accounts.dto.CustomerDto;
import com.example.accounts.entity.Accounts;
import com.example.accounts.entity.Customer;
import com.example.accounts.exception.CustomerAlreadyExistsException;
import com.example.accounts.exception.ResourceNotFoundException;
import com.example.accounts.mapper.AccountsMapper;
import com.example.accounts.mapper.CustomerMapper;
import com.example.accounts.repository.AccountsRepository;
import com.example.accounts.repository.CustomerRepository;
import com.example.accounts.service.IAccountsService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AccountsServiceImpl implements IAccountsService {

    private static final Logger log = LoggerFactory.getLogger(AccountsServiceImpl.class);

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;
    private final StreamBridge streamBridge;

    /**
     * @param customerDto - CustomerDto Object
     */
    @Override
    public void createAccount(CustomerDto customerDto) {
        Customer customer = CustomerMapper.mapToCustomer(customerDto, new Customer());
        Optional<Customer> optionalCustomer = customerRepository.findByMobileNumber(customerDto.getMobileNumber());
        if(optionalCustomer.isPresent()){
            throw new CustomerAlreadyExistsException("Customer already registered with given mobile number " + customerDto.getMobileNumber());
        }
        Customer savedCustomer = customerRepository.save(customer);
        Accounts savedAccount = accountsRepository.save(createNewAccount(savedCustomer));

        sendCommunication(savedAccount,savedCustomer);
    }

    private void sendCommunication(Accounts account, Customer customer){
        AccountsMsgDto accountsMsgDto = new AccountsMsgDto(
                account.getAccountNumber(), customer.getName(), customer.getEmail(), customer.getMobileNumber()
        );
        log.info("Sending Communication request for the details: {}",accountsMsgDto);
        boolean result = streamBridge.send("sendCommunication-out-0",accountsMsgDto);
        log.info("Is the Communication request successfully processed? : {}",result);
    }

    /**
     * @param mobileNumber - Input Mobile Number
     * @return Account details based on a given mobile number
     */
    @Override
    public CustomerDto fetchAccount(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer","mobile number",mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Account","mobile number",mobileNumber)
                );
        CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer, new CustomerDto());
        customerDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));
        return customerDto;
    }

    /**
     *
     * @param customer - Customer Object
     * @return the new account details
     */
    private Accounts createNewAccount(Customer customer){
        Accounts newAccount = new Accounts();
        newAccount.setCustomerId(customer.getCustomerId());
        long randomAccNumber = 1000000000L + new Random().nextInt(900000000);

        newAccount.setAccountNumber(randomAccNumber);
        newAccount.setAccountType(AccountsConstants.SAVINGS);
        newAccount.setBranchAddress(AccountsConstants.ADDRESS);
        return newAccount;
    }

    /**
     * @param customerDto - CustomerDto Object
     * @return boolean indicating if the update of account details is successful or not
     */
    @Override
    public boolean updateAccount(CustomerDto customerDto) {
        boolean isUpdated = false;
        AccountsDto accountsDto = customerDto.getAccountsDto();
        if(accountsDto != null){
            Accounts accounts = accountsRepository.findById(accountsDto.getAccountNumber())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Account","account number",accountsDto.getAccountNumber().toString())
                    );
            AccountsMapper.mapToAccounts(accountsDto,accounts);
            accounts = accountsRepository.save(accounts);

            Long customerId = accounts.getCustomerId();
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Customer","Customer ID",customerId.toString())
                    );
            CustomerMapper.mapToCustomer(customerDto,customer);
            customerRepository.save(customer);
            isUpdated = true;
        }
        return isUpdated;
    }

    /**
     * @param mobileNumber - Input Mobile Number
     * @return boolean indicating if the delete of Account details is successful or not
     */
    @Override
    public boolean deleteAccount(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Customer","mobile number",mobileNumber)
                );
        accountsRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());

        return true;
    }

    /**
     * @param accountNumber - Long
     * @return boolean indicating if the update of communication is successful or not
     */
    @Override
    public boolean updateCommunicationStatus(Long accountNumber) {
        boolean isUpdated = false;
        if(accountNumber != null){
            Accounts accounts = accountsRepository.findById(accountNumber).orElseThrow(
                    () -> new ResourceNotFoundException("Account","AccountNumber",accountNumber.toString())
            );
            accounts.setCommunicationSw(true);
            accountsRepository.save(accounts);
            isUpdated = true;
        }
        return isUpdated;
    }
}
