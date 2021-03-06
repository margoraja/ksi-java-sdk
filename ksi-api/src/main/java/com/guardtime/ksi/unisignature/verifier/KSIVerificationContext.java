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

package com.guardtime.ksi.unisignature.verifier;

import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.pdu.ExtensionResponse;
import com.guardtime.ksi.service.KSIExtendingService;
import com.guardtime.ksi.pdu.PduFactory;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.publication.PublicationRecord;
import com.guardtime.ksi.publication.PublicationsFile;
import com.guardtime.ksi.publication.inmemory.CertificateNotFoundException;
import com.guardtime.ksi.service.Future;
import com.guardtime.ksi.service.KSIProtocolException;
import com.guardtime.ksi.tlv.TLVParserException;
import com.guardtime.ksi.unisignature.AggregationHashChain;
import com.guardtime.ksi.unisignature.CalendarAuthenticationRecord;
import com.guardtime.ksi.unisignature.CalendarHashChain;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.KSISignatureComponentFactory;
import com.guardtime.ksi.unisignature.RFC3161Record;

import java.security.cert.Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Verification context implementation.
 *
 * @see VerificationContext
 */
final class KSIVerificationContext implements VerificationContext {

    private PublicationsFile publicationsFile;
    private KSISignature signature;
    private PublicationData userPublication;
    private KSIExtendingService extendingService;
    private boolean extendingAllowed;
    private DataHash documentHash;
    private Long inputHashLevel;
    private Map<Date, CalendarHashChain> extendedSignatures = new HashMap<>();
    private CalendarHashChain calendarExtendedToHead;

    private KSISignatureComponentFactory signatureComponentFactory;

    KSIVerificationContext(PublicationsFile publicationsFile, KSISignature signature, PublicationData userPublication,
                           boolean extendingAllowed, KSIExtendingService extendingService, DataHash documentHash, Long inputHashLevel) {
        this.publicationsFile = publicationsFile;
        this.signature = signature;
        this.userPublication = userPublication;
        this.extendingAllowed = extendingAllowed;
        this.extendingService = extendingService;
        this.documentHash = documentHash;
        this.inputHashLevel = inputHashLevel;
    }

    public void setKsiSignatureComponentFactory(KSISignatureComponentFactory signatureComponentFactory) {
        this.signatureComponentFactory = signatureComponentFactory;
    }

    public KSISignature getSignature() {
        return signature;
    }

    public CalendarHashChain getExtendedCalendarHashChain(Date publicationTime) throws KSIException {
        if (publicationTime == null) {
            return getExtendedCalendarHashChain();
        }
        if (!extendedSignatures.containsKey(publicationTime)) {
            extendedSignatures.put(publicationTime, extend(publicationTime));
        }
        return extendedSignatures.get(publicationTime);
    }

    public CalendarHashChain getExtendedCalendarHashChain() throws KSIException {
        if (calendarExtendedToHead == null) {
            calendarExtendedToHead = extend(null);
        }
        return calendarExtendedToHead;
    }

    public AggregationHashChain[] getAggregationHashChains() {
        return getSignature().getAggregationHashChains();
    }

    public CalendarHashChain getCalendarHashChain() {
        return getSignature().getCalendarHashChain();
    }

    public AggregationHashChain getLastAggregationHashChain() {
        AggregationHashChain[] chains = getAggregationHashChains();
        return chains[chains.length - 1];
    }

    public CalendarAuthenticationRecord getCalendarAuthenticationRecord() {
        return getSignature().getCalendarAuthenticationRecord();
    }

    public RFC3161Record getRfc3161Record() {
        return signature.getRfc3161Record();
    }

    public Certificate getCertificate(byte[] certificateId) {
        try {
            return publicationsFile.findCertificateById(certificateId);
        } catch (CertificateNotFoundException e) {
            return null;
        }
    }

    public PublicationRecord getPublicationRecord() {
        return getSignature().getPublicationRecord();
    }

    @Deprecated
    public void setPduFactory(PduFactory pduFactory) {
    }

    public PublicationData getUserProvidedPublication() {
        return userPublication;
    }

    public DataHash getDocumentHash() {
        return documentHash;
    }

    public Long getInputHashLevel() {
        return inputHashLevel;
    }

    public boolean isExtendingAllowed() {
        return extendingAllowed;
    }

    public PublicationsFile getPublicationsFile() {
        return publicationsFile;
    }

    private CalendarHashChain extend(Date publicationTime) throws KSIException {
        Future<ExtensionResponse> extenderFuture = extendingService.extend(getSignature().getAggregationTime(), publicationTime);
        ExtensionResponse extensionResponse = extenderFuture.getResult();
        try {
            return signatureComponentFactory.createCalendarHashChain(extensionResponse.getCalendarHashChain());
        } catch (TLVParserException e) {
            throw new KSIProtocolException("Can't parse response message", e);
        }

    }

}
