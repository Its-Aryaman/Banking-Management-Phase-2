package com.example.demo.models;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Transaction {
  @Id 
  private String id;
  private String transactionId;
  private String type; // DEPOSIT, WITHDRAW, TRANSFER
  private double amount;
  private Instant timestamp;
  private String status; // SUCCESS/FAILED
  private String sourceAccount;
  private String destinationAccount;
  // getters/setters
  
  
}
