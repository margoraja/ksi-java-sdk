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

package com.guardtime.ksi.unisignature.verifier.rules;

import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.unisignature.verifier.VerificationContext;
import com.guardtime.ksi.unisignature.verifier.VerificationErrorCode;
import com.guardtime.ksi.unisignature.verifier.VerificationResultCode;
import com.guardtime.ksi.unisignature.CalendarHashChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verifies that calendar authentication record publication hash equals to calendar hash chain
 * publication hash. If calendar authentication record is missing then status {@link VerificationResultCode#OK} is
 * returned.
 */
public class CalendarAuthenticationRecordAggregationHashRule extends BaseRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarAuthenticationRecordAggregationHashRule.class);

    public VerificationResultCode verifySignature(VerificationContext context) throws KSIException {
        if (context.getCalendarAuthenticationRecord() == null) {
            return VerificationResultCode.OK;
        }

        CalendarHashChain calendarHashChain = context.getCalendarHashChain();
        PublicationData calendarAuthenticationPublicationData = context.getCalendarAuthenticationRecord().getPublicationData();
        if (calendarHashChain.getOutputHash().equals(calendarAuthenticationPublicationData.getPublicationDataHash())) {
            return VerificationResultCode.OK;
        }
        LOGGER.info("Invalid calendar authentication record publication data publication hash. Expected '{}', found '{}'", calendarHashChain.getOutputHash(), calendarAuthenticationPublicationData.getPublicationDataHash());
        return VerificationResultCode.FAIL;
    }

    public VerificationErrorCode getErrorCode() {
        return VerificationErrorCode.INT_08;
    }

}
