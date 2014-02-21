package com.starbucks.ws;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Payment {
	
	private static final NumberFormat currencyFormat = new DecimalFormat("#.##");
	
	private String orderId;
	private String name;
	private String cardNumber;
	private String expiryDate;
	private double amount;
	
	public Payment(String orderId, double amount) {
		this.orderId = orderId;
		this.amount = amount;
	}
	
	public String getOrderId() {
		return orderId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getCardNumber() {
		return cardNumber;
	}
	
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	
	public String getExpiryDate() {
		return expiryDate;
	}
	
	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}
	
	public String getAmount() {
		return currencyFormat.format(amount);
	}	

}
