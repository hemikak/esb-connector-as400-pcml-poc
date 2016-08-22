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

package org.wso2.carbon.connector.as400.pcml.test.integration;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.testng.annotations.BeforeClass;
import org.wso2.connector.integration.test.base.ConnectorIntegrationTestBase;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests related to socket properties for the AS400.
 */
public class AS400SocketPropertiesTest extends ConnectorIntegrationTestBase {

    private Map<String, String> esbRequestHeadersMap = new HashMap<>();

    /**
     * Uploads the connector and adds request headers needed for requests.
     *
     * @throws Exception Error occurred while uploading the connector.
     */
    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        init("as400-pcml-connector-1.0.0-SNAPSHOT");
        esbRequestHeadersMap.put(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.displayName());
        esbRequestHeadersMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);
    }
}
