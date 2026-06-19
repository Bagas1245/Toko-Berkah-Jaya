package com.mycompany.tokoberkahjayaa.process;

import com.mycompany.tokoberkahjayaa.model.Customer;
import com.mycompany.tokoberkahjayaa.data.CustomerRepository;
import java.util.List;

public class CustomerService {

    private final CustomerRepository customerRepo;

    public CustomerService() {
        this.customerRepo = new CustomerRepository();
    }

    public List<Customer> getAllCustomer() {
        return customerRepo.findAll();
    }

    public Customer getCustomerById(String id) {
        return customerRepo.findById(id);
    }

    public boolean simpanCustomer(Customer customer) {
        if (!validasiCustomer(customer)) return false;
        return customerRepo.save(customer);
    }

    public boolean updateCustomer(Customer customer) {
        if (!validasiCustomer(customer)) return false;
        return customerRepo.update(customer);
    }

    public boolean hapusCustomer(String idCustomer) {
        return customerRepo.delete(idCustomer);
    }

    public boolean validasiCustomer(Customer customer) {
        if (customer == null) return false;
        if (customer.getIdCustomer() == null || customer.getIdCustomer().trim().isEmpty()) return false;
        if (customer.getNamaCustomer() == null || customer.getNamaCustomer().trim().isEmpty()) return false;
        if (customer.getAlamat() == null || customer.getAlamat().trim().isEmpty()) return false;
        if (customer.getTelepon() == null || customer.getTelepon().trim().isEmpty()) return false;
        return true;
    }
    
    public String generateNewId() {
        String lastId = customerRepo.getLastId();
        if (lastId == null || lastId.trim().isEmpty()) {
            return "CST01";
        }

        String numberPart = lastId.replaceAll("[^0-9]", "");
        String letterPart = lastId.replaceAll("[0-9]", "");

        if (numberPart.isEmpty()) {
            return lastId + "1";
        }

        int nextNumber = Integer.parseInt(numberPart) + 1;
        String format = "%0" + numberPart.length() + "d";
        return letterPart + String.format(format, nextNumber);
    }
}
