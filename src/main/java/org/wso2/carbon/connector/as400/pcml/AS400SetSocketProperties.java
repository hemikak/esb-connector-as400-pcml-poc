/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.connector.as400.pcml;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.SocketProperties;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.util.ConnectorUtils;

/**
 * Invoked when "as400.pcml.socket" synapse is called.
 */
public class AS400SetSocketProperties extends AbstractConnector {
    /**
     * {@inheritDoc}
     * <p>
     *     Adds a {@link SocketProperties} instance to the message context.
     * </p>
     */
    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        SocketProperties socketProperties = new SocketProperties();
        try {
            // Sets keepalive value
            Object keepAlive = ConnectorUtils.lookupTemplateParamater(messageContext,
                                                                    AS400Constants.AS400_SOCKET_PROPERTY_KEEP_ALIVE);
            if (null != keepAlive) {
                socketProperties.setKeepAlive(Boolean.parseBoolean((String)keepAlive));
            }

            // Sets login timeout
            Object loginTimeout = ConnectorUtils.lookupTemplateParamater(messageContext,
                                                                    AS400Constants.AS400_SOCKET_PROPERTY_LOGIN_TIMEOUT);
            if (null != loginTimeout) {
                socketProperties.setLoginTimeout(Integer.parseInt((String)loginTimeout));
            }

            // Sets receive buffer size
            Object receiveBufferSize = ConnectorUtils.lookupTemplateParamater(messageContext,
                                                            AS400Constants.AS400_SOCKET_PROPERTY_RECEIVE_BUFFER_SIZE);
            if (null != receiveBufferSize) {
                socketProperties.setReceiveBufferSize(Integer.parseInt((String)receiveBufferSize));
            }

            // Sets send buffer size
            Object sendBufferSize = ConnectorUtils.lookupTemplateParamater(messageContext,
                                                                AS400Constants.AS400_SOCKET_PROPERTY_SEND_BUFFER_SIZE);
            if (null != sendBufferSize) {
                socketProperties.setSendBufferSize(Integer.parseInt((String)sendBufferSize));
            }

            // Sets socket linger value
            Object socketLinger = ConnectorUtils.lookupTemplateParamater(messageContext,
                                                                    AS400Constants.AS400_SOCKET_PROPERTY_SOCKET_LINGER);
            if (null != socketLinger) {
                socketProperties.setSoLinger(Integer.parseInt((String)socketLinger));
            }

            // Sets socket timeout value
            Object socketTimeout = ConnectorUtils.lookupTemplateParamater(messageContext,
                                                                AS400Constants.AS400_SOCKET_PROPERTY_SOCKET_TIMEOUT);
            if (null != socketTimeout) {
                socketProperties.setSoTimeout(Integer.parseInt((String)socketTimeout));
            }

            // Sets TCP no delay
            Object tcpNoDelay = ConnectorUtils.lookupTemplateParamater(messageContext,
                                                                    AS400Constants.AS400_SOCKET_PROPERTY_TCP_NO_DELAY);
            if (null != tcpNoDelay) {
                socketProperties.setTcpNoDelay(Boolean.parseBoolean((String)tcpNoDelay));
            }

            // Sets socket properties to the AS400 instance if it exists.
            Object as400InstanceProperty = messageContext.getProperty(AS400Constants.AS400_INSTANCE);
            if (null != as400InstanceProperty) {
                AS400 as400 = (AS400) as400InstanceProperty;
                as400.setSocketProperties(socketProperties);
            }
        } catch (Exception exception) {
            // Error occurred when setting socket properties
            log.error(exception);
            AS400Utils.handleException(exception, "203", messageContext);
            throw new SynapseException(exception);
        } finally {
            // Adding the socketProperties object to message context.
            messageContext.setProperty(AS400Constants.AS400_SOCKET_PROPERTIES, socketProperties);
        }
    }
}
