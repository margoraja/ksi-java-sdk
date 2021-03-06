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

import com.guardtime.ksi.InconsistentCalendarHashChainException;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.TestUtil;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.publication.PublicationRecord;
import com.guardtime.ksi.publication.PublicationsFile;
import com.guardtime.ksi.publication.inmemory.PublicationsFilePublicationRecord;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.SignaturePublicationRecord;
import com.guardtime.ksi.unisignature.inmemory.InvalidSignatureContentException;
import com.guardtime.ksi.unisignature.verifier.VerificationErrorCode;
import com.guardtime.ksi.unisignature.verifier.VerificationResult;
import com.guardtime.ksi.unisignature.verifier.policies.PublicationsFileBasedVerificationPolicy;
import com.guardtime.ksi.unisignature.verifier.policies.UserProvidedPublicationBasedVerificationPolicy;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;

import static com.guardtime.ksi.Resources.EXTENDED_SIGNATURE_2017_03_14;
import static com.guardtime.ksi.Resources.PUBLICATIONS_FILE;
import static com.guardtime.ksi.Resources.SIGNATURE_2017_03_14;
import static com.guardtime.ksi.Resources.SIGNATURE_CALENDAR_CHAIN_FIRST_LINK_CHANGED;
import static com.guardtime.ksi.Resources.SIGNATURE_CALENDAR_CHAIN_WITH_EXTRA_RIGHT_LINK;
import static com.guardtime.ksi.Resources.SIGNATURE_ONLY_AGGREGATION_HASH_CHAINS;
import static com.guardtime.ksi.TestUtil.loadSignature;

public class ExtendingIntegrationTest extends AbstractCommonIntegrationTest {

    @Test(dataProvider = KSI_DATA_GROUP_NAME, groups = TEST_GROUP_INTEGRATION)
    public void testExtendToNearest_OK(KSI ksi) throws Exception {
        KSISignature extendedSignature = ksi.extend(loadSignature(SIGNATURE_2017_03_14));
        Assert.assertTrue(extendedSignature.isExtended(), "Signature extension failed.");
    }

    @Test(dataProvider = KSI_DATA_GROUP_NAME, groups = TEST_GROUP_INTEGRATION)
    public void testVerifyExtendedSignature_OK(KSI ksi) throws Exception {
        KSISignature signature = loadSignature(SIGNATURE_2017_03_14);
        signature = ksi.extend(signature);
        Assert.assertTrue(signature.isExtended(), "Signature extension failed.");

        VerificationResult verificationResult = ksi.verify(signature, new PublicationsFileBasedVerificationPolicy());
        Assert.assertTrue(verificationResult.isOk(), "Verification of extended signature failed with " + verificationResult.getErrorCode());
    }

    @Test(dataProvider = KSI_DATA_GROUP_NAME, groups = TEST_GROUP_INTEGRATION)
    public void testExtendWithPublicationsFile_OK(KSI ksi) throws Exception {
        KSISignature signature = loadSignature(SIGNATURE_2017_03_14);
        PublicationsFile publicationsFile = TestUtil.loadPublicationsFile(PUBLICATIONS_FILE);
        PublicationRecord publicationRecord = publicationsFile.getPublicationRecord(signature.getPublicationTime());
        KSISignature extendedSignature = ksi.extend(signature, publicationRecord);
        Assert.assertTrue(extendedSignature.isExtended());
    }

    @Test(dataProvider = KSI_DATA_GROUP_NAME, groups = TEST_GROUP_INTEGRATION)
    public void testExtendToUserPublicationString_OK(KSI ksi) throws Exception {
        SignaturePublicationRecord publicationRecord = loadSignature(EXTENDED_SIGNATURE_2017_03_14).getPublicationRecord();
        KSISignature extendedSignature = ksi.extend(loadSignature(SIGNATURE_2017_03_14), publicationRecord);
        Assert.assertTrue(extendedSignature.isExtended(), "Signature extension failed");
        VerificationResult result = ksi.verify(extendedSignature, new UserProvidedPublicationBasedVerificationPolicy(), publicationRecord.getPublicationData());
        Assert.assertTrue(result.isOk());
    }

    @Test(dataProvider = KSI_DATA_GROUP_NAME, groups = TEST_GROUP_INTEGRATION)
    public void testVerifyExtendedSignatureAfterWrithingToAndReadingFromStream_OK(KSI ksi) throws Exception {
        KSISignature signature = loadSignature(SIGNATURE_2017_03_14);
        signature = ksi.extend(signature);
        Assert.assertTrue(signature.isExtended(), "Signature extension failed.");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        signature.writeTo(baos);
        signature = ksi.read(new ByteArrayInputStream(baos.toByteArray()));

        VerificationResult verificationResult = ksi.verify(signature, new PublicationsFileBasedVerificationPolicy());
        Assert.assertTrue(verificationResult.isOk(), "Verification of extended signature failed with " + verificationResult.getErrorCode());
    }

    @Test(groups = TEST_GROUP_INTEGRATION,
            expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Publication is before signature")
    public void testExtendPublicationBeforeSignature_NOK() throws Exception {
        KSISignature signature = loadSignature(SIGNATURE_2017_03_14);
        PublicationRecord publicationRecord = new PublicationsFilePublicationRecord(new PublicationData(
                new Date(signature.getAggregationTime().getTime() - 1000000L),
                new DataHash(HashAlgorithm.SHA2_256, new byte[32])
        ));
        ksi.extend(signature, publicationRecord);
    }

    @Test(dataProvider = KSI_DATA_GROUP_NAME, groups = TEST_GROUP_INTEGRATION)
    public void testExtendSignatureFromAnotherCore_NOK(KSI ksi) throws Exception {
        KSISignature signature = loadSignature(SIGNATURE_2017_03_14);
        PublicationRecord record = new PublicationsFilePublicationRecord(new PublicationData(
                new Date(signature.getPublicationTime().getTime() + 100000L),
                new DataHash(HashAlgorithm.SHA2_256, new byte[32])
        ));
        try {
            ksi.extend(signature, record);
            Assert.assertTrue(false, "Extended signature internal verification had to fail.");
        } catch (InvalidSignatureContentException e) {
            Assert.assertFalse(e.getVerificationResult().isOk());
            Assert.assertEquals(e.getVerificationResult().getErrorCode(), VerificationErrorCode.INT_09);
            Assert.assertTrue(e.getSignature().isExtended());
        }
    }

    @Test(dataProvider = KSI_DATA_GROUP_NAME, groups = TEST_GROUP_INTEGRATION,
            expectedExceptions = InconsistentCalendarHashChainException.class,
            expectedExceptionsMessageRegExp = "Right links of signature calendar hash chain and extended calendar hash chain do not match")
    public void testExtendSignatureWithMissingRightLinkInCalendarChain_throwsKsiException(KSI ksi) throws Exception {
        ksi.extend(loadSignature(SIGNATURE_CALENDAR_CHAIN_WITH_EXTRA_RIGHT_LINK));
    }

    @Test(dataProvider = KSI_DATA_GROUP_NAME, groups = TEST_GROUP_INTEGRATION,
            expectedExceptions = InconsistentCalendarHashChainException.class,
            expectedExceptionsMessageRegExp = "Right links of signature calendar hash chain and extended calendar hash chain do not match")
    public void testExtendSignatureWithCalendarChain_throwsKsiException(KSI ksi) throws Exception {
        ksi.extend(loadSignature(SIGNATURE_CALENDAR_CHAIN_FIRST_LINK_CHANGED));
    }

    @Test(dataProvider = KSI_DATA_GROUP_NAME, groups = TEST_GROUP_INTEGRATION)
    public void testExtendSignatureWithCalendarChain_Ok(KSI ksi) throws Exception {
        KSISignature extendedSignature = ksi.extend(loadSignature(SIGNATURE_ONLY_AGGREGATION_HASH_CHAINS));
        Assert.assertTrue(extendedSignature.isExtended(), "Signature extension failed.");
    }
}
