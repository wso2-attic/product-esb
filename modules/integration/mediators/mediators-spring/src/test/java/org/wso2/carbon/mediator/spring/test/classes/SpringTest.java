package org.wso2.carbon.mediator.spring.test.classes;

import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;


public class SpringTest extends AbstractMediator{
private String name;

public void setName(String name){
this.name=name;
}

public String getName(){
return name;
}

public boolean mediate(MessageContext synCtx) {
    /*System.out.println("Message ID : " +synCtx.getMessageID());
    System.out.println("Response : " + name);*/

    String originalString = synCtx.getEnvelope().getBody().getFirstElement().getFirstElement().getFirstElement().getText();
    System.out.println(originalString);
    String updatedString = "MSFT";
    synCtx.getEnvelope().getBody().getFirstElement().getFirstElement().getFirstElement().setText(updatedString);
    return true;
    }
}


