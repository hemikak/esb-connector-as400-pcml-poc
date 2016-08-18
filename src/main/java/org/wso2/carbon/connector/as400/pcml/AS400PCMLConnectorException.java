/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.connector.as400.pcml;

import com.ibm.as400.access.AS400Message;

import java.util.Arrays;

/**
 * Exception class for the connector
 */
public class AS400PCMLConnectorException extends Exception {
    /**
     * A list of messages reveived when an as400 program call is unsuccessful.
     */
    String as400messages;

    public AS400PCMLConnectorException(String message, Throwable cause) {
        super(message, cause);
    }

    public AS400PCMLConnectorException(String message, AS400Message[] as400messages) {
        super(message);
        this.as400messages = Arrays.toString(as400messages);
    }

    public String getAS400messages() {
        return as400messages;
    }

    public void setAS400messages(AS400Message[] as400messages) {
        this.as400messages = Arrays.toString(as400messages);
    }

    @Override
    public String toString() {
        return "AS400PCMLConnectorException{" +
                "as400messages='" + as400messages + '\'' +
                '}';
    }
}
