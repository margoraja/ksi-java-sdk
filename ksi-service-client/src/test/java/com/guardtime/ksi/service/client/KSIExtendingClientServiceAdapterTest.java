/*
 * Copyright 2013-2018 Guardtime, Inc.
 *
 *  This file is part of the Guardtime client SDK.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License").
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *  "Guardtime" and "KSI" are trademarks or registered trademarks of
 *  Guardtime, Inc., and no license to trademarks is granted; Guardtime
 *  reserves and retains all trademark rights.
 *
 */

package com.guardtime.ksi.service.client;

import com.guardtime.ksi.AsyncContext;
import com.guardtime.ksi.pdu.ExtenderConfiguration;
import com.guardtime.ksi.service.ConfigurationListener;
import com.guardtime.ksi.service.KSIExtendingClientServiceAdapter;
import com.guardtime.ksi.service.KSIExtendingService;
import com.guardtime.ksi.pdu.PduVersion;
import com.guardtime.ksi.service.Future;
import com.guardtime.ksi.tlv.TLVElement;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.Date;

import static org.testng.Assert.fail;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

public class KSIExtendingClientServiceAdapterTest {

    private DummyClient testClient;
    private KSIExtendingService testService;

    @BeforeTest
    public void setUp() {
        testClient = new DummyClient();
        testService = new KSIExtendingClientServiceAdapter(testClient);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "extending client failed")
    public void testExtend() throws Exception {
        testService.extend(new Date(), new Date()).getResult();
    }

    @Test
    public void testConfigurationAsking() throws Exception {
        final AsyncContext ac = new AsyncContext();
        testService.registerExtenderConfigurationListener(new ConfigurationListener<ExtenderConfiguration>() {
            public void updated(ExtenderConfiguration configuration) {
                try {
                    fail("Client was not supposed to succeed");
                } catch (AssertionError e) {
                    ac.fail(e);
                }
            }

            public void updateFailed(Throwable reason) {
                if (!reason.getMessage().equals("extending client failed")) {
                    try {
                        fail("Client failed for the wrong reason", reason);
                    } catch (AssertionError e) {
                        ac.fail(e);
                    }
                    return;
                }
                ac.succeed();
            }
        });
        testService.getExtendingConfiguration();
        ac.await();
    }

    @Test
    public void testGetSubExtendingServices() {
        assertTrue(testService.getSubExtendingServices().isEmpty());
    }

    @Test
    public void testClose() throws Exception {
        testService.close();
        assertTrue(testClient.isClosed());
    }

    @Test
    public void testToString() {
        assertEquals(testService.toString(), "KSIExtendingClientServiceAdapter{client=DummyClient}");
    }

    private static class DummyClient implements KSIExtenderClient {

        private boolean closed = false;

        public Future<TLVElement> extend(InputStream request) {
            throw new RuntimeException("extending client failed");
        }

        public ServiceCredentials getServiceCredentials() {
            return new KSIServiceCredentials("testUser", "testKey");
        }

        public PduVersion getPduVersion() {
            return PduVersion.V2;
        }

        public void close() {
            closed = true;
        }

        public String toString() {
            return "DummyClient";
        }

        boolean isClosed() {
            return closed;
        }
    }

}
