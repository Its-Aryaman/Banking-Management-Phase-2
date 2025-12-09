package com.example.demo.clients;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "account-service")
public interface AccountClient {

  @GetMapping("/api/accounts/{accountNumber}")
  AccountDTO getAccount(@PathVariable("accountNumber") String accountNumber);

  @PutMapping("/api/accounts/{accountNumber}/balance")
  AccountDTO updateBalance(@PathVariable("accountNumber") String accountNumber,
                           @RequestBody Map<String,Object> body);
}
