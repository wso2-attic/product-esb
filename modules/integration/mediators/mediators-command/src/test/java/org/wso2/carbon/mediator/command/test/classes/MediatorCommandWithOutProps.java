package org.wso2.carbon.mediator.command.test.classes;

public class MediatorCommandWithOutProps {

    private String exchange;
    public String getExchange() {
        return exchange;
    }

    public void execute() {

        exchange = "Command Mediator execute Called";
    }

}

