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

package org.wso2.carbon.connector.pcml.test.integration;

import com.ibm.as400.access.Trace;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.connector.pcml.AS400Constants;
import org.wso2.connector.integration.test.base.ConnectorIntegrationTestBase;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests related to trace synapse that uses AS400 tracing features.
 */
public class AS400TraceTest extends ConnectorIntegrationTestBase {

    private Map<String, String> esbRequestHeadersMap = new HashMap<>();

    /**
     * Uploads the connector and adds request headers needed for requests.
     *
     * @throws Exception Error occurred while uploading the connector.
     */
    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        init("pcml-connector-1.0.0");
        esbRequestHeadersMap.put(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.displayName());
        esbRequestHeadersMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);
    }

    /**
     * Testing to see that default values are in place initially.
     */
    @Test(groups = {"wso2.esb"}, description = "Test case to check that default values are in place.")
    public void defaultTraceValuesTestCase() {
        Assert.assertTrue(!Trace.isTraceConversionOn(), "Conversion trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTraceDatastreamOn(), "Datastream trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTraceDiagnosticOn(), "Diagnostics trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTraceErrorOn(), "Error trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTraceInformationOn(), "Information trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTracePCMLOn(), "PCML trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTraceWarningOn(), "Warning trace level is enabled by default.");

        Assert.assertEquals(Trace.getFileName(), null, "Trace is already been set to a path.");
    }

    /**
     * Calls a proxy which consists of a trace synapse without enabling any log levels.
     * This also confirms that the log file is created at {@link AS400Constants#AS400_DEFAULT_LOG_PATH}.
     *
     * @throws IOException        Error reading a response.
     * @throws XMLStreamException Error converting response to XML.
     */
    @Test(groups = {"wso2.esb"}, dependsOnMethods = "defaultTraceValuesTestCase",
            description = "Test case to see if logging file is created.")
    public void emptyTraceSynapseTestCase() throws IOException, XMLStreamException {
        sendXmlRestRequest(proxyUrl, "GET", esbRequestHeadersMap, "traceCreateLogFileTestProxy.xml");

        Assert.assertTrue(!Trace.isTraceConversionOn(), "Conversion trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTraceDatastreamOn(), "Datastream trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTraceDiagnosticOn(), "Diagnostics trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTraceErrorOn(), "Error trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTraceInformationOn(), "Information trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTracePCMLOn(), "PCML trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTraceWarningOn(), "Warning trace level is enabled by default.");

        Assert.assertEquals(Trace.getFileName(), AS400Constants.AS400_DEFAULT_LOG_PATH, "Log file path has been set " +
                                                                                                "to a different path.");
        File logFile = new File(AS400Constants.AS400_DEFAULT_LOG_PATH);
        Assert.assertTrue(logFile.exists(), "Log file has not been created.");
    }

    /**
     * Calls a proxy which enables 'datastream' and 'warning' log levels
     * @throws IOException        Error reading a response.
     * @throws XMLStreamException Error converting response to XML.
     */
    @Test(groups = {"wso2.esb"}, dependsOnMethods = "emptyTraceSynapseTestCase",
            description = "Test case to see when several log levels are enabled from proxy.")
    public void enableDatastreamAndWarningLogsTestCase() throws IOException, XMLStreamException {
        sendXmlRestRequest(proxyUrl, "GET", esbRequestHeadersMap, "traceDatastreamAndWarningLogsTestProxy.xml");

        Assert.assertTrue(!Trace.isTraceConversionOn(), "Conversion trace level is enabled by default.");
        Assert.assertTrue(Trace.isTraceDatastreamOn(), "Datastream trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTraceDiagnosticOn(), "Diagnostics trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTraceErrorOn(), "Error trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTraceInformationOn(), "Information trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTracePCMLOn(), "PCML trace level is enabled by default.");
        Assert.assertTrue(Trace.isTraceWarningOn(), "Warning trace level is enabled by default.");
    }

    /**
     * Calls a proxy which enables all log levels.
     *
     * @throws IOException        Error reading a response.
     * @throws XMLStreamException Error converting response to XML.
     */
    @Test(groups = {"wso2.esb"}, dependsOnMethods = "enableDatastreamAndWarningLogsTestCase",
            description = "Test case to see when all logs are enabled.")
    public void enableAllLogsTestCase() throws IOException, XMLStreamException {
        sendXmlRestRequest(proxyUrl, "GET", esbRequestHeadersMap, "traceAllLogsTestProxy.xml");

        Assert.assertTrue(Trace.isTraceConversionOn(), "Conversion trace level is enabled by default.");
        Assert.assertTrue(Trace.isTraceDatastreamOn(), "Datastream trace level is enabled by default.");
        Assert.assertTrue(Trace.isTraceDiagnosticOn(), "Diagnostics trace level is enabled by default.");
        Assert.assertTrue(Trace.isTraceErrorOn(), "Error trace level is enabled by default.");
        Assert.assertTrue(Trace.isTraceInformationOn(), "Information trace level is enabled by default.");
        Assert.assertTrue(Trace.isTracePCMLOn(), "PCML trace level is enabled by default.");
        Assert.assertTrue(Trace.isTraceWarningOn(), "Warning trace level is enabled by default.");
    }

    /**
     * Calls a proxy which disables all log levels.
     *
     * @throws IOException        Error reading a response.
     * @throws XMLStreamException Error converting response to XML.
     */
    @Test(groups = {"wso2.esb"}, dependsOnMethods = "enableAllLogsTestCase",
            description = "Test case to see when all logs are disabled.")
    public void disableAllLogsTestCase() throws IOException, XMLStreamException {
        sendXmlRestRequest(proxyUrl, "GET", esbRequestHeadersMap, "traceDisableAllLogsProxy.xml");

        Assert.assertTrue(!Trace.isTraceConversionOn(), "Conversion trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTraceDatastreamOn(), "Datastream trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTraceDiagnosticOn(), "Diagnostics trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTraceErrorOn(), "Error trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTraceInformationOn(), "Information trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTracePCMLOn(), "PCML trace level is enabled by default.");
        Assert.assertTrue(!Trace.isTraceWarningOn(), "Warning trace level is enabled by default.");
    }
}
