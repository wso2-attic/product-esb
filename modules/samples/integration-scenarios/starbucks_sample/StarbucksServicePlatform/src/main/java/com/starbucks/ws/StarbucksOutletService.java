package com.starbucks.ws;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StarbucksOutletService {
	
	private Map<String, Order> ordersList = new ConcurrentHashMap<String, Order>();
	private Map<String, Payment> paymentRegister = new ConcurrentHashMap<String, Payment>();

	public Order addOrder(String drinkName, String additions) {		
		Order order = new Order(drinkName, additions);		
		ordersList.put(order.getOrderId(), order);
		return order;
	}
	
	public synchronized Order updateOrder(String orderId, String drinkName, String additions) {
		Order order = ordersList.get(orderId);
		if (order != null) {
			if (order.isLocked()) {
				return order;
			} else {
				if (drinkName != null && !"".equals(drinkName)) {
					order.setDrinkName(drinkName);
				}
				order.setAdditions(additions);
				return order;
			}			
		}
		return null;
	}
	
	public Order getOrder(String orderId) {
		return ordersList.get(orderId);
	}
	
	public Order[] getPendingOrders() {
		List<Order> orders = new ArrayList<Order>();
		for (Order order : ordersList.values()) {
			if (!order.isLocked()) {
				orders.add(order);
			}
		}
		return orders.toArray(new Order[orders.size()]);
	}
	
	public synchronized Order lockOrder(String orderId) {
		Order order = ordersList.get(orderId);
		if (order != null) {
			order.setLocked(true);
			return order;
		}
		return null;
	}
	
	public boolean removeOrder(String orderId) {
		boolean removed = ordersList.remove(orderId) != null;
		paymentRegister.remove(orderId);
		return removed;
	}
	
	public PaymentStatus doPayment(String orderId, String name, String cardNumber, 
			String expiryDate, double amount) {
		
		Payment payment = paymentRegister.get(orderId);
		if (payment != null) {
			return new PaymentStatus("Duplicate Payment", payment);
		}
		
		Order order = ordersList.get(orderId);
		if (order == null) {
			return new PaymentStatus("Invalid Order ID", null);
		}
		
		if (!order.isAmountAcceptable(amount)) {			
			return new PaymentStatus("Insufficient Funds", null);
		}
		
		payment = new Payment(orderId, amount);
		payment.setCardNumber(cardNumber);
		payment.setExpiryDate(expiryDate);
		payment.setName(name);
		paymentRegister.put(orderId, payment);
		return new PaymentStatus("Payment Accepted", payment);
	}
	
	public Payment getPayment(String orderId) {
		return paymentRegister.get(orderId);
	}		
	
}
