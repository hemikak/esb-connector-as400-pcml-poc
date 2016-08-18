/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import com.ibm.as400.access.AS400SecurityException;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.util.ConnectorUtils;

import java.beans.PropertyVetoException;
import java.io.IOException;

/**
 * Creates AS400 instance for PCML connector. Authenticates if user ID and password are provided.
 */
public class AS400Initialize extends AbstractConnector {

    /**
     * {@inheritDoc}
     * <p>
     *     Creates an AS400 instance and store it in the message context as {@link AS400Constants#AS400_INSTANCE}
     *     property. Authentication occurs only when user ID and password are provided.
     * </p>
     */
    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        SynapseLog log = getLog(messageContext);
        AS400 as400 = null;
        try {
            Axis2MessageContext axis2smc = (Axis2MessageContext) messageContext;

            // Get properties that are required for logging in.
            String systemName = (String) ConnectorUtils.lookupTemplateParamater(messageContext, AS400Constants.AS400_INIT_SYSTEM_NAME) ;
            String userID = (String) ConnectorUtils.lookupTemplateParamater(messageContext, AS400Constants.AS400_INIT_USER_ID) ;
            String password = (String) axis2smc.getAxis2MessageContext().getOperationContext().getProperty(AS400Constants.AS400_INIT_PASSWORD_PROPERTY);

            if (log.isTraceOrDebugEnabled()) {
                log.traceOrDebug("Creating AS400 Instance.");
            }

            // Initializing as400 instance.
            as400 = new AS400(systemName, userID, password);

            // Disabling GUI feature.
            as400.setGuiAvailable(false);

            // Authenticating user if user ID and password are set.
            if (null != userID && !userID.isEmpty() && null != password && !password.isEmpty()) {
                log.auditLog("Authenticating with AS400...");
                as400.authenticate(userID, password);
            }

        } catch (AS400SecurityException as400SecurityException){
            // When a security or authority error occurs
            log.error(as400SecurityException);
            AS400Utils.handleException(as400SecurityException, "203", messageContext);
            throw new SynapseException(as400SecurityException);
        } catch (IOException ioException){
            // Error occure when communicating to
            log.error(ioException);
            AS400Utils.handleException(ioException, "209", messageContext);
            throw new SynapseException(ioException);
        } catch (PropertyVetoException guiDisableException) {
            // Unable to disable GUI mode
            log.error(guiDisableException);
            AS400Utils.handleException(guiDisableException, "201", messageContext);
            throw new SynapseException(guiDisableException);
        } finally {
            // Adding the as400 object to message context.
            messageContext.setProperty(AS400Constants.AS400_INSTANCE, as400);
        }
    }
}