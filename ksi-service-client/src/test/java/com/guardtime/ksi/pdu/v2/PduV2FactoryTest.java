/*
 * Copyright 2013-2016 Guardtime, Inc.
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
package com.guardtime.ksi.pdu.v2;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.pdu.*;
import com.guardtime.ksi.pdu.exceptions.InvalidMessageAuthenticationCodeException;
import com.guardtime.ksi.service.KSIProtocolException;
import com.guardtime.ksi.service.client.KSIServiceCredentials;
import com.guardtime.ksi.tlv.GlobalTlvTypes;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVParserException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

import static com.guardtime.ksi.CommonTestUtil.loadTlv;

public class PduV2FactoryTest {

    private static final long DEFAULT_LEVEL = 0L;
    public static final KSIServiceCredentials CREDENTIALS = new KSIServiceCredentials("anon", "anon");
    private PduV2Factory pduFactory = new PduV2Factory();
    private DataHash dataHash;
    private KSIRequestContext requestContext;
    private KSIRequestContext extensionContext;

    @BeforeMethod
    public void setUp() throws Exception {
        this.dataHash = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);
        this.requestContext = new KSIRequestContext(CREDENTIALS, 42275443333883166L, 42L, 42L);
        this.extensionContext = new KSIRequestContext(CREDENTIALS, 5546551786909961666L, 42L, 42L);
    }

    @Test
    public void testCreateAggregationRequest_Ok() throws Exception {
        AggregationRequest request = pduFactory.createAggregationRequest(requestContext, dataHash, DEFAULT_LEVEL);
        Assert.assertNotNull(request);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "KsiRequestContext can not be null")
    public void testCreateAggregationRequestWithoutContext_ThrowsNullPointerException() throws Exception {
        pduFactory.createAggregationRequest(null, dataHash, DEFAULT_LEVEL);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "DataHash can not be null")
    public void testCreateAggregationRequestWithoutDataHash_ThrowsNullPointerException() throws Exception {
        pduFactory.createAggregationRequest(requestContext, null, DEFAULT_LEVEL);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Only non-negative integer values are allowed")
    public void testCreateAggregationRequestWithNegativeLevel_ThrowsNullPointerException() throws Exception {
        pduFactory.createAggregationRequest(requestContext, dataHash, -42L);
    }

    @Test(expectedExceptions = InvalidMessageAuthenticationCodeException.class, expectedExceptionsMessageRegExp = "Invalid MAC code. Expected.*")
    public void testAggregationResponseContainsInvalidMac_ThrowsInvalidMessageAuthenticationCodeException() throws Exception {
        pduFactory.readAggregationResponse(requestContext, loadTlv("pdu/aggregation/aggregation-response-v2-invalid-mac.tlv"));
    }

    @Test(expectedExceptions = KSIProtocolException.class, expectedExceptionsMessageRegExp = "Aggregation response payload with requestId 1 wasn't found")
    public void testAggregationResponseContainsInvalidRequestId_ThrowsKSIProtocolException() throws Exception {
        pduFactory.readAggregationResponse(new KSIRequestContext(new KSIServiceCredentials("anon", "anon"), 1L, 42L, 42L), loadTlv("pdu/aggregation/aggregation-response-v2.tlv"));
    }

    @Test(expectedExceptions = KSIProtocolException.class, expectedExceptionsMessageRegExp = "Invalid response message. Response message must contain at least one payload element")
    public void testAggregationResponseDoesNotContainResponseTlvTag_ThrowsKSIProtocolException() throws Exception {
        pduFactory.readAggregationResponse(requestContext, loadTlv("pdu/aggregation/aggregation-response-v2-missing-payload.tlv"));
    }

    @Test(expectedExceptions = KSIProtocolException.class, expectedExceptionsMessageRegExp = "Error was returned by server. Error status is .* Error message from server: 'The request could not be authenticated'")
    public void testAggregationResponseContains03ErrorMessage_ThrowsKSIProtocolException() throws Exception {
        pduFactory.readAggregationResponse(requestContext, loadTlv("pdu/aggregation/aggregation-response-v2-invalid-login-key.tlv"));
    }

    @Test(expectedExceptions = KSIProtocolException.class, expectedExceptionsMessageRegExp = "Error was returned by server. Error status is .* Error message from server: .*")
    public void testAggregationResponseContainsErrorMessageInside02Element_ThrowsKSIProtocolException() throws Exception {
        pduFactory.readAggregationResponse(new KSIRequestContext(new KSIServiceCredentials("anon", "anon"), 8530358545345979581L, 42L, 42L), loadTlv("pdu/aggregation/aggregation-response-v2-with-error.tlv"));
    }

    @Test(expectedExceptions = KSIProtocolException.class, expectedExceptionsMessageRegExp = "Received PDU v1 response to PDU v2 request. Configure the SDK to use PDU v1 format for the given Aggregator")
    public void testReadV2AggregationResponse() throws Exception {
        pduFactory.readAggregationResponse(requestContext, loadTlv("aggregation-203-error.tlv"));
    }

    @Test(expectedExceptions = KSIProtocolException.class, expectedExceptionsMessageRegExp = "Error was returned by server. Error status is 0x101. Error message from server: 'this-error-should-be-thrown'")
    public void testReadV2AggregationResponseWithErrorPayloadAndMac_ThrowsKSIProtocolException() throws Exception {
        pduFactory.readAggregationResponse(requestContext, loadTlv("pdu/aggregation/aggregation-response-v2-multi-payload-with-error-payload.tlv"));
    }

    @Test(expectedExceptions = TLVParserException.class, expectedExceptionsMessageRegExp = "Unknown critical TLV element with tag=.* encountered")
    public void testReadV2AggregationResponseWithUnknownCriticalElement_ThrowsTLVParserException() throws Exception {
        pduFactory.readAggregationResponse(requestContext, loadTlv("pdu/aggregation/aggregation-response-v2-multi-payload-with-critical-unknown-element.tlv"));
    }

    @Test(expectedExceptions = InvalidMessageAuthenticationCodeException.class, expectedExceptionsMessageRegExp = "Invalid MAC code. Expected.*")
    public void testReadV2AggregationResponseWithEditedFlagAndUnchangedMac_ThrowsInvalidMessageAuthenticationCodeException() throws Exception {
        pduFactory.readAggregationResponse(requestContext, loadTlv("pdu/aggregation/aggregation-response-v2-changed-flag-but-unchanged-hmac.tlv"));
    }

    @Test(expectedExceptions = TLVParserException.class, expectedExceptionsMessageRegExp = "Invalid PDU header element. Expected element 0x01, got 0x.*")
    public void testReadV2AggregationResponseHeaderNotFirst_ThrowsInvalidMessageAuthenticationCodeException() throws Exception {
        pduFactory.readAggregationResponse(requestContext, loadTlv("pdu/aggregation/aggregation-response-v2-header-not-first.tlv"));
    }

    @Test(expectedExceptions = InvalidMessageAuthenticationCodeException.class, expectedExceptionsMessageRegExp = "Invalid MAC code. Expected.*")
    public void testExtensionResponseInvalidHMAC_ThrowsInvalidMessageAuthenticationCodeException() throws Exception {
        pduFactory.readExtensionResponse(extensionContext, loadTlv("pdu/extension/extension-response-v2-invalid-mac.tlv"));
    }

    @Test(expectedExceptions = KSIProtocolException.class, expectedExceptionsMessageRegExp = "Extension response payload with requestId 5546551786909961666 wasn't found")
    public void testExtensionRequestIdsMismatch() throws Exception {
        pduFactory.readExtensionResponse(extensionContext, loadTlv("pdu/extension/extension-response-v2.tlv"));
    }

    @Test(expectedExceptions = KSIProtocolException.class, expectedExceptionsMessageRegExp = "Error was returned by server. Error status is .* Error message from server: 'The request could not be authenticated'")
    public void testExtensionResponseContains03ErrorMessage_ThrowsKSIProtocolException() throws Exception {
        pduFactory.readExtensionResponse(requestContext, loadTlv("pdu/extension/extension-response-v2-invalid-login-key.tlv"));
    }

    @Test(expectedExceptions = KSIProtocolException.class, expectedExceptionsMessageRegExp = "Error was returned by server. Error status is .* Error message from server: 'The request contained invalid payload'")
    public void testExtensionResponseContains02ErrorMessage_ThrowsKSIProtocolException() throws Exception {
        pduFactory.readExtensionResponse(new KSIRequestContext(CREDENTIALS, 98765L, 42L, 42L), loadTlv("pdu/extension/extension-response-v2-with-error.tlv"));
    }

    @Test(expectedExceptions = KSIProtocolException.class, expectedExceptionsMessageRegExp = "Invalid KSI response. Missing MAC.")
    public void testExtensionResponseWithoutMac_ThrowsKSIProtocolException() throws Exception {
        pduFactory.readExtensionResponse(extensionContext, loadTlv("pdu/extension/extension-response-v2-missing-mac.tlv"));
    }

    @Test(expectedExceptions = KSIProtocolException.class, expectedExceptionsMessageRegExp = "Received PDU v1 response to PDU v2 request. Configure the SDK to use PDU v1 format for the given Extender")
    public void testReadV2ExtensionResponse() throws Exception {
        pduFactory.readExtensionResponse(extensionContext, loadTlv("pdu/extension/extension-response-v1-ok-request-id-4321.tlv"));
    }

    @Test(expectedExceptions = KSIProtocolException.class, expectedExceptionsMessageRegExp = "Error was returned by server. Error status is 0x101. Error message from server: 'this-error-should-be-thrown'")
    public void testExtensionResponseWithErrorPayloadAndMac_ThrowsKSIProtocolException() throws Exception {
        pduFactory.readExtensionResponse(extensionContext, loadTlv("pdu/extension/extension-response-v2-multi-payload-with-error-payload.tlv"));
    }

    @Test(expectedExceptions = TLVParserException.class, expectedExceptionsMessageRegExp = "Unknown critical TLV element with tag=.* encountered")
    public void testReadV2ExtensionResponseWithUnknownCriticalElement_ThrowsTLVParserException() throws Exception {
        pduFactory.readExtensionResponse(extensionContext, loadTlv("pdu/extension/extension-response-v2-with-unknown-critical-element.tlv"));
    }

    @Test(expectedExceptions = InvalidMessageAuthenticationCodeException.class, expectedExceptionsMessageRegExp = "Invalid MAC code. Expected.*")
    public void testReadV2ExtensionResponseWithEditedFlagAndUnchangedMac_ThrowsInvalidMessageAuthenticationCodeException() throws Exception {
        pduFactory.readExtensionResponse(extensionContext, loadTlv("pdu/extension/extension-response-v2-with-changed-flag-but-unchanged-mac.tlv"));
    }

    @Test(expectedExceptions = TLVParserException.class, expectedExceptionsMessageRegExp = "Invalid PDU header element. Expected element 0x01, got 0x.*")
    public void testReadV2ExtensionResponseHeaderNotFirst_ThrowsInvalidMessageAuthenticationCodeException() throws Exception {
        pduFactory.readExtensionResponse(extensionContext, loadTlv("pdu/extension/extension-response-v2-header-not-first.tlv"));
    }

    @Test
    public void testReadV2ExtensionResponseContainingUnknownNonCriticalElement() throws Exception {
        ExtensionResponse response = pduFactory.readExtensionResponse(new KSIRequestContext(new KSIServiceCredentials("anon", "anon"), 8396215651691691389L, 42L, 42L), loadTlv("pdu/extension/extension-response-v2-unknown-non-critical-element.tlv"));
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getCalendarHashChain());
    }

    @Test
    public void testCreateAggregationConfigurationRequest() throws Exception {
        AggregationRequest request = pduFactory.createAggregatorConfigurationRequest(requestContext);
        Assert.assertNotNull(request);
        TLVElement tlv = TLVElement.create(request.toByteArray());
        Assert.assertEquals(tlv.getType(), GlobalTlvTypes.ELEMENT_TYPE_AGGREGATION_REQUEST_PDU_V2);
        Assert.assertEquals(tlv.getChildElements(0x04).size(), 1);
    }
    @Test
    public void testAggregationConfigurationResponseParsingWithMultiplePayloadsV2() throws Exception {
        AggregatorConfiguration cnf = pduFactory.readAggregatorConfigurationResponse(requestContext, loadTlv("pdu/aggregation/aggregator-response-with-conf-ack-and-signature.tlv"));

        Assert.assertEquals(cnf.getAggregationAlgorithm(), HashAlgorithm.SHA2_384);
        Assert.assertTrue(cnf.getAggregationPeriod().equals(12288L));
        Assert.assertTrue(cnf.getMaximumLevel().equals(19L));
        Assert.assertTrue(cnf.getMaximumRequests().equals(17L));
        Assert.assertTrue(cnf.getParents().size() == 3);
        for (String parent : cnf.getParents()){
            Assert.assertTrue(parent.contains(".url"));
        }
    }

    @Test
    public void testAggregationResponseParsingWithMultipleConfsV2() throws Exception {
        AggregatorConfiguration cnf = pduFactory.readAggregatorConfigurationResponse(requestContext, loadTlv("pdu/aggregation/aggregator-response-multiple-confs.tlv"));

        Assert.assertNotNull(cnf);
        Assert.assertEquals(cnf.getAggregationAlgorithm(), HashAlgorithm.RIPEMD_160);
        Assert.assertTrue(cnf.getAggregationPeriod().equals(2L));
        Assert.assertTrue(cnf.getMaximumLevel().equals(2L));
        Assert.assertTrue(cnf.getMaximumRequests().equals(2L));
        Assert.assertTrue(cnf.getParents().size() == 1);
        for (String parent : cnf.getParents()){
            Assert.assertEquals(parent, "anon");
        }
    }

    @Test
    public void testAggregationResponseParsingWithEmptyConfV2() throws Exception {
        AggregatorConfiguration cnf = pduFactory.readAggregatorConfigurationResponse(requestContext, loadTlv("pdu/aggregation/aggregator-response-with-empty-conf.tlv"));

        Assert.assertNotNull(cnf);
        Assert.assertNull(cnf.getAggregationAlgorithm());
        Assert.assertNull(cnf.getAggregationPeriod());
        Assert.assertNull(cnf.getMaximumLevel());
        Assert.assertNull(cnf.getMaximumRequests());
        Assert.assertTrue(cnf.getParents().isEmpty());
    }

    @Test
    public void testExtenderConfigurationResponseParsingWithMultiplePayloadsV2() throws Exception {
        ExtenderConfiguration cnf = pduFactory.readExtenderConfigurationResponse(extensionContext, loadTlv("pdu/extension/extender-response-with-conf-and-calendar.tlv"));

        Assert.assertTrue(cnf.getCalendarFirstTime().equals(new Date(5557150000L)));
        Assert.assertTrue(cnf.getCalendarLastTime().equals(new Date(1422630579000L)));
        Assert.assertTrue(cnf.getMaximumRequests().equals(4L));
        Assert.assertTrue(cnf.getParents().size() == 3);
        for (String parent : cnf.getParents()){
            Assert.assertTrue(parent.contains(".url"));
        }
    }

    @Test
    public void testExtenderResponseParsingWithMultipleConfsV2() throws Exception {
        ExtenderConfiguration cnf = pduFactory.readExtenderConfigurationResponse(extensionContext, loadTlv("pdu/extension/extender-response-multiple-confs.tlv"));

        Assert.assertNotNull(cnf);
        Assert.assertTrue(cnf.getCalendarFirstTime().equals(new Date(158000)));
        Assert.assertTrue(cnf.getCalendarLastTime().equals(new Date(40627000)));
        Assert.assertTrue(cnf.getMaximumRequests().equals(2L));
        Assert.assertTrue(cnf.getParents().size() == 1);
        for (String parent : cnf.getParents()){
            Assert.assertEquals(parent, "anon");
        }
    }

    @Test
    public void testExtenderResponseParsingWithEmptyConfV2() throws Exception {
        ExtenderConfiguration cnf = pduFactory.readExtenderConfigurationResponse(extensionContext, loadTlv("pdu/extension/extender-response-with-empty-conf.tlv"));

        Assert.assertNotNull(cnf);
        Assert.assertNull(cnf.getCalendarFirstTime());
        Assert.assertNull(cnf.getCalendarLastTime());
        Assert.assertNull(cnf.getMaximumRequests());
        Assert.assertTrue(cnf.getParents().isEmpty());
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "KsiRequestContext can not be null")
    public void testCreateAggregationConfigurationRequestWithoutContext() throws Exception {
        pduFactory.createAggregatorConfigurationRequest(null);
    }

    @Test
    public void testReadAggregatorConfigurationResponse() throws Exception {
        AggregatorConfiguration response = pduFactory.readAggregatorConfigurationResponse(requestContext, loadTlv("pdu/aggregation/aggregation-conf-response-ok.tlv"));
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getAggregationAlgorithm());
        Assert.assertNotNull(response.getAggregationPeriod());
        Assert.assertNotNull(response.getMaximumLevel());
        Assert.assertNotNull(response.getMaximumRequests());
        Assert.assertNotNull(response.getParents());
        Assert.assertEquals(response.getParents().size(), 3);
    }

    @Test(expectedExceptions = KSIProtocolException.class, expectedExceptionsMessageRegExp = "Received PDU v1 response to PDU v2 request. Configure the SDK to use PDU v1 format for the given Aggregator")
    public void testReadV1AggregatorConfigurationResponse() throws Exception {
        pduFactory.readAggregatorConfigurationResponse(requestContext, loadTlv("aggregation-203-error.tlv"));
    }

    @Test
    public void testCreateExtenderConfigurationRequest() throws Exception {
        ExtensionRequest request = pduFactory.createExtensionConfigurationRequest(requestContext);
        Assert.assertNotNull(request);
        TLVElement tlv = TLVElement.create(request.toByteArray());
        Assert.assertEquals(tlv.getType(), GlobalTlvTypes.ELEMENT_TYPE_EXTENSION_REQUEST_PDU_V2);
        Assert.assertEquals(tlv.getChildElements(0x04).size(), 1);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "KsiRequestContext can not be null")
    public void testCreateExtenderConfigurationRequestWithoutContext() throws Exception {
        pduFactory.createExtensionConfigurationRequest(null);
    }

    @Test(expectedExceptions = KSIProtocolException.class, expectedExceptionsMessageRegExp = "Received PDU v1 response to PDU v2 request. Configure the SDK to use PDU v1 format for the given Extender")
    public void testReadV1ExtensionConfigurationResponse() throws Exception {
        pduFactory.readExtenderConfigurationResponse(extensionContext, loadTlv("pdu/extension/extension-response-v1-ok-request-id-4321.tlv"));
    }

    @Test
    public void testReadExtenderConfigurationResponse() throws Exception {
        ExtenderConfiguration response = pduFactory.readExtenderConfigurationResponse(requestContext, loadTlv("pdu/extension/extender-conf-response-ok.tlv"));
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getCalendarFirstTime());
        Assert.assertNotNull(response.getCalendarLastTime());
        Assert.assertNotNull(response.getParents());
        Assert.assertEquals(response.getParents().size(), 3);
        Assert.assertEquals(response.getMaximumRequests(), Long.valueOf(4));
    }

}