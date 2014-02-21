/*
 *  Copyright 2012 WSO2
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.starbucks.apps;

import org.w3c.dom.Document;

/**
 * Represnts a payment as submitted by a customer
 */
public class Payment {
    
    private String orderId;
    private String cardNo;
    private String expires;
    private String name;
    private double amount;
    
    public Payment(String orderId, String response) {
        this.orderId = orderId;
        Document document = XmlUtils.parseXmlFile(response);
        cardNo = document.getElementsByTagName("cardNo").
                item(0).getTextContent();
        expires = document.getElementsByTagName("expires").
                item(0).getTextContent();
        name = document.getElementsByTagName("name").
                item(0).getTextContent();
        amount = Double.parseDouble(document.getElementsByTagName("amount").
                item(0).getTextContent());
    }
    
    public String getCardNo() {
        return cardNo;
    }
    
    public String getExpires() {
        return expires;
    }
    
    public String getName() {
        return name;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Order ID: ").append(orderId).append("\n").
                append("Customer Name: ").append(name).append("\n").
                append("Card Number: ").append(cardNo).append("\n").
                append("Expiry Date: ").append(expires).append("\n").
                append("Amount: $").append(amount).append("\n");        
        return builder.toString();
    }
    
}
