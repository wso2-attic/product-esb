/*
*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 
  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/


package org.wso2.carbon.mediator.clazz.test.classes;

import org.apache.synapse.MessageContext;

import org.apache.synapse.mediators.AbstractMediator;

public class SampleClassWithOutProperties extends AbstractMediator {

    public boolean mediate(MessageContext mc) {
        System.out.println("test mediator called");
        String originalString = mc.getEnvelope().getBody().getFirstElement().getFirstElement().getFirstElement().getText();
        System.out.println(originalString);
        String updatedString = "MSFT";
        mc.getEnvelope().getBody().getFirstElement().getFirstElement().getFirstElement().setText(updatedString);
        return true;
    }

    public String getType() {
        return null;
    }

    public void setTraceState(int traceState) {
        traceState = 0;
    }

    public int getTraceState() {
        return 0;
    }

}
