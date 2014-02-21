package com.starbucks.ws;

public class PaymentStatus {
	
	private String status;
	private Payment payment;
	
	public PaymentStatus(String status, Payment payment) {
		this.status = status;
		this.payment = payment;
	}

	public String getStatus() {
		return status;
	}

	public Payment getPayment() {
		return payment;
	}		

}
