package org.wso2.carbon.mediator.command.test.classes;

public class MediatorCommandWithProps {

    private String symbol;
    private String exchange;

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getExchange() {
        return exchange;
    }

    public void execute() {

        exchange = this.symbol;
    }

}

