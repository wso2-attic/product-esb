package com.starbucks.ws;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Order {
	
	private static final Random rand = new Random();
	private static final Map<String, Double> priceList = new HashMap<String, Double>();
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
	private static final NumberFormat currencyFormat = new DecimalFormat("#.##");

	private String orderId;
	private String drinkName;
	private String additions;
	private double cost;
	private boolean locked;
	private long timestamp;	
	
	public Order(String drinkName, String additions) {
		this.orderId = UUID.randomUUID().toString();
		this.drinkName = drinkName;
		this.additions = additions;
		this.cost = calculateCost();
		this.timestamp = System.currentTimeMillis();
	}
	
	public String getOrderId() {
		return orderId;
	}	
	
	public String getDrinkName() {
		return drinkName;
	}
	
	public void setDrinkName(String drinkName) {
		this.drinkName = drinkName;
		this.cost = calculateCost();
		this.timestamp = System.currentTimeMillis();
	}
	
	public String getAdditions() {
		return additions;
	}
	
	public void setAdditions(String additions) {
		this.additions = additions;
		this.cost = calculateCost();
		this.timestamp = System.currentTimeMillis();
	}
	
	public String getCost() {
		return currencyFormat.format(cost);
	}	
	
	public boolean isLocked() {
		return locked;
	}
	
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	public String getTimestamp() {
		return dateFormat.format(new Date(timestamp));
	}
	
	boolean isAmountAcceptable(double amount) {
		return amount >= cost;
	}
	
	private double calculateCost() {
		double cost = getPrice(drinkName, false);
		if (additions != null && !"".equals(additions)) {
			String[] additionalItems = additions.split(" ");
			for (String item : additionalItems) {
				cost += getPrice(item, true);
			}
		}
		return Double.parseDouble(currencyFormat.format(cost));
	}
	
	private double getPrice(String item, boolean addition) {
		synchronized (priceList) {
			Double price = priceList.get(item);
			if (price == null) {
				if (addition) {
					price = rand.nextDouble() * 5;
				} else {
					price = rand.nextInt(8) + 2 - 0.01;				
				}
				priceList.put(item, price);
			}
			return price;
		}		
	}
	
}
