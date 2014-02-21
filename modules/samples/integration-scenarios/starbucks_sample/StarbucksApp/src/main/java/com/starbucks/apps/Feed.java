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

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents a feed of pending orders. A feed may contain zero or more
 * entries where each entry contains metadata related to a pending order. 
 */
public class Feed {
    
    private Order[] orders;
    
    public Feed(String response) {
        Document document = XmlUtils.parseXmlFile(response);
        NodeList entries = document.getElementsByTagName("entry");
        if (entries != null) {
            List<Order> orderList = new ArrayList<Order>();
            for (int i = 0; i < entries.getLength(); i++) {
                Node entry = entries.item(i);
                NodeList children = entry.getChildNodes();
                String orderId = null;
                Node orderNode = null;
                for (int j = 0; j < children.getLength(); j++) {
                    Node entryChild = children.item(j);
                    if ("id".equals(entryChild.getNodeName())) {
                        orderId = entryChild.getTextContent();
                        orderId = orderId.substring(orderId.lastIndexOf('/') + 1);
                    } else if ("content".equals(entryChild.getNodeName())) {
                        NodeList contentChildren = entryChild.getChildNodes();
                        for (int k = 0; k < contentChildren.getLength(); k++) {
                            Node contentChild = contentChildren.item(k);
                            if ("order".equals(contentChild.getNodeName())) {
                                orderNode = contentChild;
                                break;
                            }
                        }                                                
                    }
                }
                
                if (orderId != null && orderNode != null) {
                    Order order = new Order(orderId, orderNode);                    
                    orderList.add(order);
                }
            }
            
            if (orderList.size() > 0) {
                this.orders = orderList.toArray(new Order[orderList.size()]);
            }
        }
    }
    
    public Order[] getOrders() {
        return orders;
    }
    
    public String toString() {
        if (orders == null || orders.length == 0) {
            return "No pendnig orders";
        } else {
            return orders.length + " pending orders found";
        }
    }
    
}
