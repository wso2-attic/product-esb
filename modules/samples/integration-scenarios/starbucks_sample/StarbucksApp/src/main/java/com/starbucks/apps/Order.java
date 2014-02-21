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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents an order for a drink as submitted by a customer.
 */
public class Order {
    
    private String orderId;
    private String drink;
    private String additions;
    private double cost;
    private boolean fromNode = true;
    
    public Order(String orderId, String response) {        
        this(orderId, XmlUtils.parseXmlFile(response).getFirstChild());
        this.fromNode = false;
    }
    
    public Order(String orderId, Node orderNode) {
        this.orderId = orderId;
        NodeList children = orderNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if ("drink".equals(child.getNodeName())) {
                this.drink = child.getTextContent();
            } else if ("additions".equals(child.getNodeName())) {
                this.additions = child.getTextContent();
            } else if ("cost".equals(child.getNodeName())) {
                this.cost = Double.parseDouble(child.getTextContent());
            }
        }
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public String getDrink() {
        return drink;
    }
    
    public String getAdditions() {
        return additions;
    }
    
    public double getCost() {
        return cost;
    }   
    
    public String toString() {
        if (!fromNode) {
            StringBuilder builder = new StringBuilder();
            builder.append("Order ID: ").append(orderId).append("\n").
                    append("Drink: ").append(drink).append("\n").
                    append("Additions: ").append(additions).append("\n").
                    append("Cost: $").append(cost).append("\n");        
            return builder.toString();        
        } else {
            return orderId;
        }
    }
    
    
    
}
