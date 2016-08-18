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
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import com.ibm.as400.data.XmlException;
import javax.xml.stream.XMLStreamException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.util.ConnectorUtils;

import java.beans.PropertyVetoException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A connector component that calls an AS400 program using PCML.
 */
public class AS400CallProgram extends AbstractConnector {
    public void connect(MessageContext synCtx) throws ConnectException {
        SynapseLog log = getLog(synCtx);
        AS400 as400 = null;
        try {
            Axis2MessageContext axis2smc = (Axis2MessageContext) synCtx;

            // Get properties that are required for logging in.
            String systemName = (String) axis2smc.getAxis2MessageContext().getOperationContext().getProperty(AS400Constants.AS400_PCML_INIT_SYSTEM_NAME);
            String userID = (String) axis2smc.getAxis2MessageContext().getOperationContext().getProperty(AS400Constants.AS400_PCML_INIT_USER_ID);
            String password = (String) axis2smc.getAxis2MessageContext().getOperationContext().getProperty(AS400Constants.AS400_PCML_INIT_PASSWORD);

            if (log.isTraceOrDebugEnabled()) {
                log.traceOrDebug("Authenticating with AS400.");
            }

            // Initializing as400 instance
            as400 = new AS400(systemName, userID, password);

            // Disabling GUI feature provided by JTOpen library.
            as400.setGuiAvailable(false);

            // Get PCML source file name
            String pcmlFileName = (String) ConnectorUtils.lookupTemplateParamater(synCtx, AS400Constants.AS400_PCML_PCML_FILE_NAME);
            // Get program name to call
            String programName = (String) ConnectorUtils.lookupTemplateParamater(synCtx, AS400Constants.AS400_PCML_PROGRAM_NAME);

            // Create program document with the given PCML source file
            ProgramCallDocument pcmlDocument = new ProgramCallDocument(as400, pcmlFileName);

            // Get input parameters to pass to the PCML document
            List<PCMLInputParam> inputParams = AS400Utils.getInputParams(synCtx, log);
            // Apply input parameters
            if (!inputParams.isEmpty()) {
                for (PCMLInputParam inputParam : inputParams) {
                    if (null == inputParam.getIndices()) {
                        pcmlDocument.setValue(inputParam.getQualifiedName(), inputParam.getValue());
                    } else {
                        pcmlDocument.setValue(inputParam.getQualifiedName(), inputParam.getIndices(), inputParam.getValue());
                    }
                }
            }

            if (log.isTraceOrDebugEnabled()) {
                log.traceOrDebug("Calling program '" + programName + "' in file '" + pcmlFileName + "'.");
            }

            // Call the AS400 program
            boolean success = pcmlDocument.callProgram(programName);
            if (!success) {
                // When the call is unsuccessful, throw an exception with the list of messages received from AS400 server.
                AS400Message[] msgs = pcmlDocument.getMessageList(programName);
                StringBuffer errorMessage = new StringBuffer();
                for (AS400Message message : msgs) {
                    errorMessage = errorMessage.append(message.getID()).append(" - ").append(message.getText()).append(System.lineSeparator());
                }
                throw new AS400PCMLConnectorException("Calling program '" + programName + "' was not successful.", msgs);
            } else {
                // Generate the XPCML document which consists of all input and output data
                ByteArrayOutputStream xpcmlOutputStream = new ByteArrayOutputStream();
                pcmlDocument.generateXPCML(programName, xpcmlOutputStream);
                OMElement omElement = AXIOMUtil.stringToOM(xpcmlOutputStream.toString(StandardCharsets.UTF_8.toString()));
                synCtx.getEnvelope().getBody().addChild(omElement);
            }
        } catch (PropertyVetoException guiDisableException) {
            // Unable to disable GUI mode
            log.error(guiDisableException);
            AS400Utils.handleException(guiDisableException, "201", synCtx);
            throw new SynapseException(guiDisableException);
        } catch (PcmlException pcmlException) {
            // Unable to connect to AS400 server
            log.error(pcmlException);
            AS400Utils.handleException(pcmlException, "202", synCtx);
            throw new SynapseException(pcmlException);
        } catch (AS400PCMLConnectorException pcmlException) {
            // Unable to connect to AS400 server
            log.error(pcmlException);
            AS400Utils.handleException(pcmlException, "203", synCtx);
            throw new SynapseException(pcmlException);
        } catch (XmlException e) {
            // Error occurred while processing the output payload
            e.printStackTrace();
        } catch (IOException e) {
            // Error occurred while writing data to output payload
            e.printStackTrace();
        } catch (XMLStreamException xmlStreamException) {
            // Error converting XPCML to payload
            log.error(xmlStreamException);
            AS400Utils.handleException(xmlStreamException, "204", synCtx);
            throw new SynapseException(xmlStreamException);
        } finally {
            if (null != as400 && as400.isConnected()) {
                if (log.isTraceOrDebugEnabled()) {
                    log.traceOrDebug("Disconnecting from all AS400 services.");
                }
                as400.disconnectAllServices();
            }
        }
    }
}
