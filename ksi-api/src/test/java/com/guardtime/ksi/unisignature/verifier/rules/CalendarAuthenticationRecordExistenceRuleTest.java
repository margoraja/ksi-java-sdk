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

package com.guardtime.ksi.unisignature.verifier.rules;

import com.guardtime.ksi.TestUtil;
import com.guardtime.ksi.unisignature.verifier.RuleResult;
import com.guardtime.ksi.unisignature.verifier.VerificationErrorCode;
import com.guardtime.ksi.unisignature.verifier.VerificationResultCode;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.guardtime.ksi.Resources.SIGNATURE_2017_03_14;
import static com.guardtime.ksi.Resources.SIGNATURE_NO_CALENDAR_AUTH_RECORD;

public class CalendarAuthenticationRecordExistenceRuleTest extends AbstractRuleTest {

    private CalendarAuthenticationRecordExistenceRule rule = new CalendarAuthenticationRecordExistenceRule();

    @Test
    public void testSignatureWithoutCalendarAuthenticationRecordReturnsNaStatus_Ok() throws Exception {
        RuleResult result = rule.verify(build(TestUtil.loadSignature(SIGNATURE_NO_CALENDAR_AUTH_RECORD)));
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getResultCode(), VerificationResultCode.NA);
        Assert.assertEquals(result.getErrorCode(), VerificationErrorCode.GEN_02);
    }

    @Test
    public void testSignatureWithCalendarAuthenticationRecordReturnsOkStatus_Ok() throws Exception {
        RuleResult result = rule.verify(build(TestUtil.loadSignature(SIGNATURE_2017_03_14)));
        Assert.assertEquals(result.getResultCode(), VerificationResultCode.OK);
    }

}