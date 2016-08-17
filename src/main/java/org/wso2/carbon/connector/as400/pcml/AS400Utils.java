package org.wso2.carbon.connector.as400.pcml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.core.util.ConnectorUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by pta75590 on 8/11/2016.
 */
public class AS400Utils {
    private static final OMFactory fac = OMAbstractFactory.getOMFactory();
    private static final OMNamespace omNs = fac.createOMNamespace("http://wso2.org/as400/pcml/adaptor", "pcml");
    public static List<PCMLInputParam> getInputParams(MessageContext synCtx, SynapseLog logger) {
        List<PCMLInputParam> inputParameters = new ArrayList<>();
        try {
            String strPCMLObjects = (String) ConnectorUtils.lookupTemplateParamater(synCtx, AS400Constants.AS400_PCML_PROGRAM_INPUTS);
            OMElement sObjects = AXIOMUtil.stringToOM(strPCMLObjects);
            Iterator<OMElement> pcmlObjects = sObjects.getChildElements();
            while (pcmlObjects.hasNext()) {
                OMElement pcmlObject = pcmlObjects.next();
                if (AS400Constants.AS400_PCML_PROGRAM_INPUT.equals(pcmlObject.getLocalName())) {
                    // qualified name is a required parameter
                    String qualifiedName = pcmlObject.getAttributeValue(new QName("qualifiedName"));
                    if (null != qualifiedName && !qualifiedName.isEmpty()) {
                        logger.error("'qualifiedName' attribute not found for a " + AS400Constants.AS400_PCML_PROGRAM_INPUT + ".");
                        continue;
                    }
                    // indices are not a required parameter, therefore it can be null per each unique qualified name
                    int[] indices = getIndices(pcmlObject.getAttributeValue(new QName("indices")), logger);
                    String value = pcmlObject.getText();
                    inputParameters.add(new PCMLInputParam(qualifiedName, indices, value));
                } else {
                    logger.error("Invalid element found when parsing children of " + AS400Constants.AS400_PCML_PROGRAM_INPUTS + ". Please check input parameters.");
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        return inputParameters;
    }

    private static int[] getIndices(String indicesAsString, SynapseLog logger) {
        int[] indices = null;
        try {
            if (null != indicesAsString) {
                String[] split = indicesAsString.split(",");
                indices = new int[split.length];
                for (int i = 0; i < split.length; i++) {
                    indices[i] = Integer.parseInt(split[i].trim());
                }
            } else {
                indices = null;
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid content found for indices. Make sure that indices attribute consists of integers separated by commas.");
        }

        return indices;
    }

    public static void preparePayload(MessageContext messageContext, OMElement element) {
        SOAPBody soapBody = messageContext.getEnvelope().getBody();
        for (Iterator itr = soapBody.getChildElements(); itr.hasNext(); ) {
            OMElement child = (OMElement) itr.next();
            child.detach();
        }
        for (Iterator itr = element.getChildElements(); itr.hasNext(); ) {
            OMElement child = (OMElement) itr.next();
            soapBody.addChild(child);
        }
    }

    public static void preparePayload(MessageContext messageContext, Exception e) {
        OMElement omElement = fac.createOMElement("error", omNs);
        OMElement subValue = fac.createOMElement("errorMessage", omNs);
        subValue.addChild(fac.createOMText(omElement, e.getMessage()));
        omElement.addChild(subValue);
        preparePayload(messageContext, omElement);
    }

    public static void handleException(Exception exception, String errorCode, MessageContext messageContext) {
        messageContext.setProperty(SynapseConstants.ERROR_EXCEPTION, exception);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, exception.getMessage());
        messageContext.setProperty(SynapseConstants.ERROR_CODE, errorCode);

        if (exception instanceof AS400PCMLConnectorException) {
            AS400PCMLConnectorException connectorException = (AS400PCMLConnectorException) exception;
            messageContext.setProperty(SynapseConstants.ERROR_DETAIL, connectorException.getAs400messages());
        }

        preparePayload(messageContext, exception);
    }
}
