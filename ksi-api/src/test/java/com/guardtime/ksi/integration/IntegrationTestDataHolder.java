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

package com.guardtime.ksi.integration;

import com.guardtime.ksi.KSI;
import com.guardtime.ksi.KSIBuilder;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.pdu.ExtensionRequest;
import com.guardtime.ksi.pdu.ExtensionResponse;
import com.guardtime.ksi.pdu.ExtensionResponseFuture;
import com.guardtime.ksi.pdu.KSIRequestContext;
import com.guardtime.ksi.pdu.PduFactory;
import com.guardtime.ksi.pdu.RequestContextFactory;
import com.guardtime.ksi.pdu.v2.PduV2Factory;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.publication.PublicationsFile;
import com.guardtime.ksi.publication.inmemory.InMemoryPublicationsFileFactory;
import com.guardtime.ksi.service.Future;
import com.guardtime.ksi.service.KSIExtendingClientServiceAdapter;
import com.guardtime.ksi.service.KSIExtendingService;
import com.guardtime.ksi.service.client.KSIExtenderClient;
import com.guardtime.ksi.service.client.KSIServiceCredentials;
import com.guardtime.ksi.service.client.http.CredentialsAwareHttpSettings;
import com.guardtime.ksi.service.client.http.HttpSettings;
import com.guardtime.ksi.service.http.simple.SimpleHttpPublicationsFileClient;
import com.guardtime.ksi.service.http.simple.SimpleHttpSigningClient;
import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.trust.JKSTrustStore;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.inmemory.InMemoryKsiSignatureComponentFactory;
import com.guardtime.ksi.unisignature.verifier.AlwaysSuccessfulPolicy;
import com.guardtime.ksi.unisignature.verifier.VerificationContext;
import com.guardtime.ksi.unisignature.verifier.VerificationContextBuilder;
import com.guardtime.ksi.unisignature.verifier.VerificationErrorCode;
import com.guardtime.ksi.util.Base16;
import com.guardtime.ksi.util.Util;

import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Date;

import static com.guardtime.ksi.CommonTestUtil.load;
import static com.guardtime.ksi.TestUtil.calculateHash;
import static com.guardtime.ksi.integration.AbstractCommonIntegrationTest.createCertSelector;
import static com.guardtime.ksi.integration.AbstractCommonIntegrationTest.createKeyStore;
import static com.guardtime.ksi.integration.AbstractCommonIntegrationTest.loadExtenderSettings;
import static com.guardtime.ksi.integration.AbstractCommonIntegrationTest.loadPublicationsFileSettings;
import static com.guardtime.ksi.integration.AbstractCommonIntegrationTest.loadSignerSettings;

public class IntegrationTestDataHolder implements Closeable {

    private String testFile;
    private final IntegrationTestAction action;
    private final VerificationErrorCode errorCode;
    private final String errorMessage;
    private final long inputHashLevel;
    private final DataHash inputHash;
    private final DataHash chcInputHash;
    private final DataHash chchOutputHash;
    private final Date aggregationTime;
    private final Date publicationTime;
    private final PublicationData userPublication;
    private final boolean extendingPermitted;
    private final String responseFile;
    private final String publicationsFile;
    private final String certFile;

    private KSIExtenderClient extenderClient;
    private KSI ksi;
    private final CredentialsAwareHttpSettings extenderSettings;

    public IntegrationTestDataHolder(String testFilePath, String[] inputData, KSIExtenderClient httpClient) throws Exception {
        notNull(inputData, "Input data");
        for (int i = 0; i < inputData.length; i++) {
            inputData[i] = inputData[i].trim();
        }

        notNull(httpClient, "Extender http client");
        extenderClient = httpClient;

        notEmpty(inputData[0], "Test file");
        if (testFilePath != null && !(testFilePath.trim().length() == 0)) {
            testFile = testFilePath + inputData[0];
            responseFile = inputData[12].length() == 0 ? null : testFilePath + inputData[12];
            publicationsFile = inputData[13].length() == 0 ? null : testFilePath + inputData[13];
            certFile = inputData[14].length() == 0 ? null : testFilePath + inputData[14];
        } else {
            testFile = inputData[0];
            responseFile = inputData[12].length() == 0 ? null : inputData[12];
            publicationsFile = inputData[13].length() == 0 ? null : inputData[13];
            certFile = inputData[14].length() == 0 ? null : inputData[14];
        }

        notEmpty(inputData[1], "Action");
        action = IntegrationTestAction.getByName(inputData[1]);

        errorCode = getErrorCodeByName(inputData[2]);
        errorMessage = inputData[3].length() == 0 ? null : inputData[3];
        inputHashLevel = inputData[4].length() == 0 ? 0 : Long.decode(inputData[4]);
        inputHash = inputData[5].length() == 0 ? null : new DataHash(Base16.decode(inputData[5]));
        chcInputHash = inputData[6].length() == 0 ? null : new DataHash(Base16.decode(inputData[6]));
        chchOutputHash = inputData[7].length() == 0 ? null : new DataHash(Base16.decode(inputData[7]));
        aggregationTime = inputData[8].length() == 0 ? null : new Date(Long.decode(inputData[8]) * 1000L);
        publicationTime = inputData[9].length() == 0 ? null : new Date(Long.decode(inputData[9]) * 1000L);
        userPublication = inputData[10].length() == 0 ? null : new PublicationData(inputData[10]);
        extendingPermitted = inputData[11].length() == 0 ? false : Boolean.valueOf(inputData[11]);

        this.extenderSettings = loadExtenderSettings();
        buildKsi();
    }

    private void buildKsi() throws IOException, KSIException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        CredentialsAwareHttpSettings signerSettings = loadSignerSettings();
        HttpSettings publicationSettings = loadPublicationsFileSettings();
        KSIBuilder builder = new KSIBuilder();
        builder.setPublicationsFilePkiTrustStore(getKeyStore()).
                setKsiProtocolExtendingService(getExtendingService()).
                setKsiProtocolSignerClient(new SimpleHttpSigningClient(signerSettings)).
                setPublicationsFileTrustedCertSelector(createCertSelector()).
                setDefaultVerificationPolicy(new AlwaysSuccessfulPolicy()).
                setDefaultSigningHashAlgorithm(HashAlgorithm.SHA2_256);

        if (publicationsFile == null) {
            builder.setKsiProtocolPublicationsFileClient(new SimpleHttpPublicationsFileClient(publicationSettings));
        } else {
            builder.setKsiProtocolPublicationsFileClient(new PublicationsFileClientFromFile(publicationsFile));
        }

        this.ksi = builder.build();
    }

    public VerificationContext getVerificationContext(KSISignature signature) throws KSIException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {

        VerificationContextBuilder builder = new VerificationContextBuilder();

        builder.setSignature(signature).
                setExtendingService(getExtendingService()).
                setPublicationsFile(publicationsFile == null ? ksi.getPublicationsFile() : getPublicationsFile()).
                setUserPublication(userPublication).
                setExtendingAllowed(extendingPermitted).
                setDocumentHash(inputHash, inputHashLevel);
        VerificationContext context = builder.build();
        context.setKsiSignatureComponentFactory(new InMemoryKsiSignatureComponentFactory());
        return context;
    }

    private KSIExtendingService mockExtenderService() throws KSIException, IOException {
        KSIExtendingService mockClient = Mockito.mock(KSIExtendingService.class);
        final Future<TLVElement> mockedFuture = Mockito.mock(Future.class);
        Mockito.when(mockedFuture.isFinished()).thenReturn(Boolean.TRUE);
        final TLVElement responseTLV = TLVElement.create(IOUtils.toByteArray(load(responseFile)));
        Mockito.when(mockedFuture.getResult()).thenReturn(responseTLV);

        Mockito.when(mockClient.extend(Mockito.any(Date.class), Mockito.any
                (Date.class))).then(new Answer<Future>() {
            public Future<ExtensionResponse> answer(InvocationOnMock invocationOnMock) throws Throwable {
                KSIServiceCredentials credentials = new KSIServiceCredentials("anon", "anon");
                Date aggregationTime = (Date) invocationOnMock.getArguments()[0];
                Date publicationTime = (Date) invocationOnMock.getArguments()[1];

                PduFactory factory = new PduV2Factory();
                KSIRequestContext context = RequestContextFactory.DEFAULT_FACTORY.createContext();
                ExtensionRequest request = factory.createExtensionRequest(context, credentials, aggregationTime, publicationTime);
                ByteArrayInputStream bais = new ByteArrayInputStream(request.toByteArray());
                TLVElement requestElement = TLVElement.create(Util.toByteArray(bais));

                responseTLV.getFirstChildElement(0x2).getFirstChildElement(0x01).setLongContent(
                        requestElement.getFirstChildElement(0x2).getFirstChildElement(0x1).getDecodedLong()
                );

                responseTLV.getFirstChildElement(0x1F).setDataHashContent(
                        calculateHash(
                                responseTLV,
                                responseTLV.getFirstChildElement(0x1F).getDecodedDataHash().getAlgorithm(),
                                extenderSettings.getCredentials().getLoginKey()
                        )
                );
                return new ExtensionResponseFuture(mockedFuture, context, credentials, factory);
            }
        });
        return mockClient;
    }

    private PublicationsFile getPublicationsFile() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, KSIException {
        InMemoryPublicationsFileFactory factory = new InMemoryPublicationsFileFactory(new JKSTrustStore(getKeyStore(), createCertSelector()));
        return factory.create(load(publicationsFile));
    }

    private KeyStore getKeyStore() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        KeyStore keyStore = createKeyStore();
        if (certFile != null) {
            keyStore.load(null);
            InputStream fis = load(certFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            while (bis.available() > 0) {
                Certificate cert = cf.generateCertificate(bis);
                keyStore.setCertificateEntry("custom"+bis.available(), cert);
            }
        }
        return keyStore;
    }

    private KSIExtendingService getExtendingService() throws IOException, KSIException {
        if (responseFile == null) {
            return new KSIExtendingClientServiceAdapter(extenderClient);
        } else {
            return mockExtenderService();
        }
    }

    private void notEmpty(String object, String name) {
        if (object.trim().length() == 0) {
            throw new IllegalArgumentException(name + " is empty.");
        }
    }

    private void notNull(Object object, String name) {
        if (object == null) {
            throw new IllegalArgumentException(name + " is null.");
        }
    }

    private VerificationErrorCode getErrorCodeByName(String code) {
        if(code.length() == 0) {
            return null;
        } else {
            for (VerificationErrorCode errorCode : VerificationErrorCode.values()) {
                if (errorCode.getCode().equals(code)) {
                    return errorCode;
                }
            }
        }
        throw new IllegalArgumentException("Unknown verification error code: " + code);
    }

    public String toString() {
        return "TestData{" +
                " testFile=" + testFile +
                ", action=" + action.getName() +
                ", errorCode=" + (errorCode == null ? "" : errorCode.getCode()) +
                ", errorMessage=" + errorMessage +
                ", inputHash=" + inputHash +
                ", chcInputHash=" + chcInputHash +
                ", chchOutputHash=" + chchOutputHash +
                ", aggregationTime=" + (aggregationTime == null ? "" : aggregationTime.getTime()) +
                ", publicationTime=" + (publicationTime == null ? "" : publicationTime.getTime()) +
                ", userPublication=" + (userPublication == null ? "" : userPublication.getPublicationString()) +
                ", extendingPermitted=" + extendingPermitted +
                ", responseFile=" + responseFile +
                ", publicationsFile=" + publicationsFile +
                ", certFile=" + certFile +
                " }";
    }

    public String getTestFile() {
        return testFile;
    }

    public IntegrationTestAction getAction() {
        return action;
    }

    public VerificationErrorCode getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public DataHash getInputHash() {
        return inputHash;
    }

    public DataHash getChcInputHash() {
        return chcInputHash;
    }

    public DataHash getChchOutputHash() {
        return chchOutputHash;
    }

    public Date getAggregationTime() {
        return aggregationTime;
    }

    public Date getPublicationTime() {
        return publicationTime;
    }

    public PublicationData getUserPublication() {
        return userPublication;
    }

    public boolean isExtendingPermitted() {
        return extendingPermitted;
    }

    public String getResponseFile() {
        return responseFile;
    }

    public KSI getKsi() {
        return ksi;
    }

    public void close() throws IOException {
        if (ksi != null) ksi.close();
        if (extenderClient != null) extenderClient.close();
    }
}
