package org.wso2.carbon.connector.as400.pcml;

import com.ibm.as400.access.AS400Message;

import java.util.Arrays;

/**
 * Created by pta75590 on 8/11/2016.
 */
public class AS400PCMLConnectorException extends Exception {
    String as400messages;

    public AS400PCMLConnectorException(String message, AS400Message[] as400messages) {
        super(message);
        this.as400messages = Arrays.toString(as400messages);
    }

    public String getAs400messages() {
        return as400messages;
    }

    public void setAs400messages(AS400Message[] as400messages) {
        this.as400messages = Arrays.toString(as400messages);
    }

    @Override
    public String toString() {
        return "AS400PCMLConnectorException{" +
                "as400messages='" + as400messages + '\'' +
                '}';
    }
}
