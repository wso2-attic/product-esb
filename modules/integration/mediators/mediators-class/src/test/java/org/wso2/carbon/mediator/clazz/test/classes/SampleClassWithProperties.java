package org.wso2.carbon.mediator.clazz.test.classes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

public class SampleClassWithProperties extends AbstractMediator{

    private static final Log log = LogFactory.getLog(SampleClassWithProperties.class);

    private String symbol = "";
    public SampleClassWithProperties() {}

    public boolean mediate(MessageContext mc) {
        log.debug("Mediate method clalled in SampleClassWithProperties.class");
        log.info("Mediate method clalled in SampleClassWithProperties.class");
        String originalString = mc.getEnvelope().getBody().getFirstElement().getFirstElement().getFirstElement().getText();
        log.info("Message Context = " +originalString);
        log.debug("Message Context = " +originalString);
        String updatedString = this.symbol; 
        mc.getEnvelope().getBody().getFirstElement().getFirstElement().getFirstElement().setText(updatedString);
        log.debug("Symbol updated");
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
    public void setSymbol(String currentSymbol )
    {
        log.debug("setSymbol class property method called");
        this.symbol = currentSymbol;
    }
}
