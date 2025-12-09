package com.example.demo.models;

public class NortificationPayLoad {

		  private String transactionId;
		  private String message;
		  private String to; // account number or email
		  // getters/setters
		  public String getTransactionId() {
			  return transactionId;
		  }
		  public void setTransactionId(String transactionId) {
			  this.transactionId = transactionId;
		  }
		  public String getMessage() {
			  return message;
		  }
		  public void setMessage(String message) {
			  this.message = message;
		  }
		  public String getTo() {
			  return to;
		  }
		  public void setTo(String to) {
			  this.to = to;
		  }
		  
		  
		}


