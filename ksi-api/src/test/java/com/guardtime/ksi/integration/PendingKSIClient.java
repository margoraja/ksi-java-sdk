/*
 * Copyright 2013-2017 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * "Guardtime" and "KSI" are trademarks or registered trademarks of
 * Guardtime, Inc., and no license to trademarks is granted; Guardtime
 * reserves and retains all trademark rights.
 */
package com.guardtime.ksi.integration;

import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.pdu.AggregationResponse;
import com.guardtime.ksi.pdu.ExtenderConfiguration;
import com.guardtime.ksi.pdu.ExtensionResponse;
import com.guardtime.ksi.service.Future;
import com.guardtime.ksi.service.client.KSIClientException;
import com.guardtime.ksi.service.client.KSIExtenderClient;
import com.guardtime.ksi.service.client.KSISigningClient;
import com.guardtime.ksi.service.client.ConfigurationListener;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Dummy KSI Client all of which's calls idle for ten seconds and then fail.
 */
public class PendingKSIClient implements KSISigningClient, KSIExtenderClient {
    
    public Future<ExtensionResponse> extend(Date aggregationTime, Date publicationTime)
            throws KSIException {
        sleep1M();
        throw new KSIClientException("Failure!");
    }

    public Future<AggregationResponse> sign(DataHash dataHash, Long level) throws KSIException {
        sleep1M();
        throw new KSIClientException("Failure!");
    }

    public List<KSISigningClient> getSubSigningClients() {
        return Collections.emptyList();
    }

    public void updateAggregationConfiguration() {
        throw new RuntimeException("Failure!");
    }


    public void registerAggregatorConfigurationListener(ConfigurationListener listener) {
        throw new RuntimeException("Failure!");
    }

    public void updateExtenderConfiguration() {
        throw new RuntimeException("Failure!");
    }

    public void registerExtenderConfigurationListener(ConfigurationListener<ExtenderConfiguration> listener) {
        throw new RuntimeException("Failure!");
    }

    public List<KSIExtenderClient> getSubExtenderClients() {
        return Collections.emptyList();
    }

    public void close() throws IOException {

    }

    private void sleep1M() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
