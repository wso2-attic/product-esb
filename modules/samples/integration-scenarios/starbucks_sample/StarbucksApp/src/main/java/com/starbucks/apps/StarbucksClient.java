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

import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.text.DefaultCaret;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;

/**
 * Starbucks customer view
 */
public class StarbucksClient extends javax.swing.JFrame {
    
    private static final int STATE_READY = 0;
    private static final int STATE_ORDER_SUBMITTED = 1;
    private static final int STATE_PAID = 2;
    
    private String url;
    private String orderId;
    private int state;
    
    /** Creates new form StarbucksClient */
    public StarbucksClient(String host, int port) {        
        this.url = "http://" + host + ":" + port;
        System.out.println("Using ESB URL: " + url);
        
        initComponents();        
        ((DefaultCaret) statusArea.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        ((DefaultCaret) requestView.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        ((DefaultCaret) responseView.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        setApplicationState(STATE_READY);
    }
    
    private void setApplicationState(int state) {
        this.state = state;
        switch (state) {
            case STATE_READY:
                customerNameField.setEditable(false);
                cardNumberField.setEditable(false);
                dateField.setEditable(false);
                amountField.setEditable(false);
                checkoutButton.setEnabled(false);
                resetButton.setEnabled(false);
                paymentReviewButton.setEnabled(false);
                orderReviewButton.setEnabled(false);
                orderStatusButton.setEnabled(false);
                drinkSelect.setEnabled(true);
                peppermintBox.setEnabled(true);
                milkBox.setEnabled(true);
                whipCreamBox.setEnabled(true);
                caramelBox.setEnabled(true);
                orderSubmitButton.setEnabled(true);
                orderSubmitButton.setText("Submit");
                requestView.setText("");
                responseView.setText("");
                statusArea.setText("Ready...");
                break;
                
            case STATE_ORDER_SUBMITTED:
                customerNameField.setEditable(true);
                cardNumberField.setEditable(true);
                dateField.setEditable(true);
                amountField.setEditable(true);
                checkoutButton.setEnabled(true);
                resetButton.setEnabled(false);
                paymentReviewButton.setEnabled(false);
                orderReviewButton.setEnabled(true);
                orderStatusButton.setEnabled(true);
                drinkSelect.setEnabled(true);
                peppermintBox.setEnabled(true);
                milkBox.setEnabled(true);
                whipCreamBox.setEnabled(true);
                caramelBox.setEnabled(true);
                orderSubmitButton.setEnabled(true);
                orderSubmitButton.setText("Update");
                break;
                
            case STATE_PAID:
                customerNameField.setEditable(false);
                cardNumberField.setEditable(false);
                dateField.setEditable(false);
                amountField.setEditable(false);
                checkoutButton.setEnabled(false);
                resetButton.setEnabled(true);
                paymentReviewButton.setEnabled(true);
                orderReviewButton.setEnabled(true);
                orderStatusButton.setEnabled(true);
                drinkSelect.setEnabled(false);
                peppermintBox.setEnabled(false);
                milkBox.setEnabled(false);
                whipCreamBox.setEnabled(false);
                caramelBox.setEnabled(false);
                orderSubmitButton.setEnabled(false);
                orderSubmitButton.setText("Update");
                break;
        }
    }
    
    private String getOrderPayload() {
        String drink = drinkSelect.getSelectedItem().toString();
        String additions = "";
        if (peppermintBox.isSelected()) {
            additions += "Peppermint ";
        }
        if (caramelBox.isSelected()) {
            additions += "Caramel ";
        }
        if (milkBox.isSelected()) {
            additions += "Milk ";
        }
        if (whipCreamBox.isSelected()) {
            additions += "WhipCream ";
        }
        
        StringBuilder payloadBuilder = new StringBuilder("<order xmlns=\"http://starbucks.example.org\">").
                append("<drink>").append(drink).append("</drink>");
        if (additions.length() > 0) {
            payloadBuilder.append("<additions>").append(additions.trim()).append("</additions>");
        }
        payloadBuilder.append("</order>");
        
        return XmlUtils.prettyPrint(payloadBuilder.toString());
    }
    
    private boolean addNewOrder() {                
        String payload = getOrderPayload();
        try {
            HttpInvocationContext context = HttpUtils.doPost(payload, "application/xml", url + "/order");
            HttpResponse response = context.getHttpResponse();
            requestView.setText(context.getRequestData());
            responseView.setText(context.getResponseData());
            
            if (response.getStatusLine().getStatusCode() == 201) {
                String location = response.getFirstHeader(HttpHeaders.LOCATION).getValue();
                this.orderId = location.substring(location.lastIndexOf('/') + 1);
                Order order = new Order(this.orderId, context.getResponsePayload());
                String status = "Order submitted successfully...\n" + order;
                statusArea.setText(status);
                amountField.setText(String.valueOf(order.getCost()));
                return true;
            } else {
                statusArea.setText("Order submission failed");
                return false;
            }
        } catch (IOException e) {
            handleError("IO error while submitting new order", e);
            return false;
        }
    }
    
    private boolean updateOrder() {
        String payload = getOrderPayload();
        try {
            HttpInvocationContext context = HttpUtils.doPut(payload, "application/xml", url + "/order/" + orderId);
            HttpResponse response = context.getHttpResponse();
            requestView.setText(context.getRequestData());
            responseView.setText(context.getResponseData());
            
            if (response.getStatusLine().getStatusCode() == 200) {                               
                Order order = new Order(this.orderId, context.getResponsePayload());
                String status = "Order updated successfully...\n" + order;
                statusArea.setText(status);
                amountField.setText(String.valueOf(order.getCost()));
                return true;
            } else {
                statusArea.setText("Order update failed");
                return false;
            }
        } catch (IOException e) {
            handleError("IO error while updating order", e);
            return false;
        }
    }
    
    private void handleError(String msg, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        drinkSelect = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        peppermintBox = new javax.swing.JCheckBox();
        milkBox = new javax.swing.JCheckBox();
        caramelBox = new javax.swing.JCheckBox();
        whipCreamBox = new javax.swing.JCheckBox();
        orderSubmitButton = new javax.swing.JButton();
        orderReviewButton = new javax.swing.JButton();
        orderStatusButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        customerNameField = new javax.swing.JTextField();
        cardNumberField = new javax.swing.JTextField();
        dateField = new javax.swing.JTextField();
        amountField = new javax.swing.JTextField();
        checkoutButton = new javax.swing.JButton();
        paymentReviewButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        statusArea = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        requestView = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        responseView = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Starbucks Client");
        setResizable(false);
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Order"));
        jLabel1.setText("Drink");

        drinkSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Caffe Misto", "Clover Brewed Coffee", "Decaf Pike", "Iced Coffee", "Pike Place Roast", "Caffe Americano", "Caffe Latte", "Caffe Mocha", "Cappuccino", "Caramel Machiato", "Espresso", "Hot Chocolate" }));

        jLabel2.setText("Additions");

        peppermintBox.setText("Peppermint");
        peppermintBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        peppermintBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        milkBox.setText("Milk");
        milkBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        milkBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        caramelBox.setText("Caramel");
        caramelBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        caramelBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        whipCreamBox.setText("Whip Cream");
        whipCreamBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        whipCreamBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        orderSubmitButton.setText("Submit");
        orderSubmitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orderSubmitButtonActionPerformed(evt);
            }
        });

        orderReviewButton.setText("Review");
        orderReviewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orderReviewButtonActionPerformed(evt);
            }
        });

        orderStatusButton.setText("Status");
        orderStatusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orderStatusButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(drinkSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(peppermintBox)
                            .addComponent(caramelBox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(whipCreamBox)
                            .addComponent(milkBox))))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(orderSubmitButton, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                    .addComponent(orderStatusButton, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                    .addComponent(orderReviewButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(drinkSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(orderSubmitButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(peppermintBox)
                            .addComponent(milkBox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(caramelBox)
                            .addComponent(whipCreamBox)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(orderReviewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(orderStatusButton)))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Payment"));
        jLabel3.setText("Customer Name");

        jLabel4.setText("Card Number");

        jLabel5.setText("Expiry Date");

        jLabel6.setText("Amount");

        customerNameField.setText("Peter Parker");

        cardNumberField.setText("1234-5678-9010");

        dateField.setText("12/15");

        checkoutButton.setText("Checkout");
        checkoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkoutButtonActionPerformed(evt);
            }
        });

        paymentReviewButton.setText("Review");
        paymentReviewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paymentReviewButtonActionPerformed(evt);
            }
        });

        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cardNumberField)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(dateField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(amountField))
                    .addComponent(customerNameField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(resetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(paymentReviewButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(checkoutButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(checkoutButton)
                    .addComponent(customerNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cardNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(paymentReviewButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(dateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(amountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resetButton))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Status"));
        statusArea.setColumns(20);
        statusArea.setEditable(false);
        statusArea.setRows(5);
        jScrollPane1.setViewportView(statusArea);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 887, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Message Tracer"));
        requestView.setColumns(20);
        requestView.setEditable(false);
        requestView.setRows(5);
        jScrollPane2.setViewportView(requestView);

        responseView.setColumns(20);
        responseView.setEditable(false);
        responseView.setRows(5);
        jScrollPane3.setViewportView(responseView);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 428, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        setApplicationState(STATE_READY);
    }//GEN-LAST:event_resetButtonActionPerformed

    private void paymentReviewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paymentReviewButtonActionPerformed
        try {
            HttpInvocationContext context = HttpUtils.doGet(url + "/payment/order/" + orderId);
            HttpResponse response = context.getHttpResponse();
            requestView.setText(context.getRequestData());
            responseView.setText(context.getResponseData());

            if (response.getStatusLine().getStatusCode() == 200) {                                
                Payment payment = new Payment(this.orderId, context.getResponsePayload());
                String status = "Payment executed successfully...\n" + payment;
                statusArea.setText(status);                                
            } else {
                statusArea.setText("Payment details retreival failed");
            }
        } catch (IOException e) {
            handleError("IO error while retreiving payment details", e);
        }
    }//GEN-LAST:event_paymentReviewButtonActionPerformed

    private void checkoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkoutButtonActionPerformed
        String cardNo = cardNumberField.getText();
        if (cardNo == null || "".equals(cardNo)) {
            JOptionPane.showMessageDialog(this, "Credit card number not specified", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String expDate = dateField.getText();
        if (expDate == null || "".equals(expDate)) {
            JOptionPane.showMessageDialog(this, "Credit card expiry date not specified", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String name = customerNameField.getText();
        if (name == null || "".equals(name)) {
            JOptionPane.showMessageDialog(this, "Customer name not specified", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String amount = amountField.getText();
        if (amount == null || "".equals(amount)) {
            JOptionPane.showMessageDialog(this, "Billing amount not specified", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Double.parseDouble(amount);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid billing amount", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        StringBuilder payloadBuilder = new StringBuilder();
        payloadBuilder.append("<payment xmlns=\"http://starbucks.example.org\">").
                append("<cardNo>").append(cardNo).append("</cardNo>").
                append("<expires>").append(expDate).append("</expires>").
                append("<name>").append(name).append("</name>").
                append("<amount>").append(amount).append("</amount>").
                append("</payment>");
        String payload = XmlUtils.prettyPrint(payloadBuilder.toString());
        try {
            HttpInvocationContext context = HttpUtils.doPut(payload, "application/xml", url + "/payment/order/" + orderId);
            HttpResponse response = context.getHttpResponse();
            requestView.setText(context.getRequestData());
            responseView.setText(context.getResponseData());
            
            if (response.getStatusLine().getStatusCode() == 201) {
                String location = response.getFirstHeader(HttpHeaders.LOCATION).getValue();                
                Payment payment = new Payment(this.orderId, context.getResponsePayload());
                String status = "Payment executed successfully...\n" + "Location: " + 
                	location + "\n" + payment;
                statusArea.setText(status);                
                setApplicationState(STATE_PAID);
            } else {
                statusArea.setText("Payment operation failed");
            }
        } catch (IOException e) {
            handleError("IO error while processing the payment", e);
        }
    }//GEN-LAST:event_checkoutButtonActionPerformed

    private void orderStatusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orderStatusButtonActionPerformed
        try {
            HttpInvocationContext context = HttpUtils.doOptions(url + "/order/" + orderId);
            HttpResponse response = context.getHttpResponse();
            requestView.setText(context.getRequestData());
            responseView.setText(context.getResponseData());

            if (response.getStatusLine().getStatusCode() == 200) {                                
                String status = "Order details retrieved successfully...\n";
                String allow = response.getFirstHeader(HttpHeaders.ALLOW).getValue();
                if (allow.contains("PUT")) {
                    status += "Order can be modified";
                } else {
                    status += "Order is being processed\nNo modifications allowed";
                }
                statusArea.setText(status);
            } else {
                statusArea.setText("Order details retreival failed");
            }
        } catch (IOException e) {
            handleError("IO error while retreiving order details", e);
        }
    }//GEN-LAST:event_orderStatusButtonActionPerformed

    private void orderReviewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orderReviewButtonActionPerformed
        try {
            HttpInvocationContext context = HttpUtils.doGet(url + "/order/" + orderId);
            HttpResponse response = context.getHttpResponse();
            requestView.setText(context.getRequestData());
            responseView.setText(context.getResponseData());

            if (response.getStatusLine().getStatusCode() == 200) {           
                Order order = new Order(this.orderId, context.getResponsePayload());
                String status = "Order details retrieved successfully...\n" + order;
                statusArea.setText(status);
            } else {
                statusArea.setText("Order details retreival failed");
            }
        } catch (IOException e) {
            handleError("IO error while retreiving order details", e);
        }
    }//GEN-LAST:event_orderReviewButtonActionPerformed

    private void orderSubmitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orderSubmitButtonActionPerformed
        if (state == STATE_READY) {
            if (addNewOrder()) {
                setApplicationState(STATE_ORDER_SUBMITTED);
            }
        } else {
            updateOrder();            
        }        
    }//GEN-LAST:event_orderSubmitButtonActionPerformed
      
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField amountField;
    private javax.swing.JCheckBox caramelBox;
    private javax.swing.JTextField cardNumberField;
    private javax.swing.JButton checkoutButton;
    private javax.swing.JTextField customerNameField;
    private javax.swing.JTextField dateField;
    private javax.swing.JComboBox drinkSelect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JCheckBox milkBox;
    private javax.swing.JButton orderReviewButton;
    private javax.swing.JButton orderStatusButton;
    private javax.swing.JButton orderSubmitButton;
    private javax.swing.JButton paymentReviewButton;
    private javax.swing.JCheckBox peppermintBox;
    private javax.swing.JTextArea requestView;
    private javax.swing.JButton resetButton;
    private javax.swing.JTextArea responseView;
    private javax.swing.JTextArea statusArea;
    private javax.swing.JCheckBox whipCreamBox;
    // End of variables declaration//GEN-END:variables
    
}
