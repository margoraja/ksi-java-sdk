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

package com.guardtime.ksi.unisignature.verifier.policies;

import com.guardtime.ksi.unisignature.verifier.rules.AggregationChainInputHashVerificationRule;
import com.guardtime.ksi.unisignature.verifier.rules.AggregationHashChainAlgorithmDeprecatedRule;
import com.guardtime.ksi.unisignature.verifier.rules.AggregationHashChainConsistencyRule;
import com.guardtime.ksi.unisignature.verifier.rules.AggregationHashChainIndexConsistencyRule;
import com.guardtime.ksi.unisignature.verifier.rules.AggregationHashChainIndexSuccessorRule;
import com.guardtime.ksi.unisignature.verifier.rules.AggregationHashChainLinkMetadataRule;
import com.guardtime.ksi.unisignature.verifier.rules.AggregationHashChainTimeConsistencyRule;
import com.guardtime.ksi.unisignature.verifier.rules.CalendarAuthenticationRecordAggregationHashRule;
import com.guardtime.ksi.unisignature.verifier.rules.CalendarAuthenticationRecordAggregationTimeRule;
import com.guardtime.ksi.unisignature.verifier.rules.CalendarHashChainAggregationAlgorithmObsoleteRule;
import com.guardtime.ksi.unisignature.verifier.rules.CalendarHashChainAggregationTimeRule;
import com.guardtime.ksi.unisignature.verifier.rules.CalendarHashChainInputHashVerificationRule;
import com.guardtime.ksi.unisignature.verifier.rules.CalendarHashChainRegistrationTimeRule;
import com.guardtime.ksi.unisignature.verifier.rules.DocumentHashAlgorithmVerificationRule;
import com.guardtime.ksi.unisignature.verifier.rules.DocumentHashVerificationRule;
import com.guardtime.ksi.unisignature.verifier.rules.InputHashLevelVerificationRule;
import com.guardtime.ksi.unisignature.verifier.rules.Rfc3161InternalHashAlgorithmsDeprecatedRule;
import com.guardtime.ksi.unisignature.verifier.rules.Rfc3161OutputHashAlgorithmDeprecatedRule;
import com.guardtime.ksi.unisignature.verifier.rules.Rfc3161RecordIndexRule;
import com.guardtime.ksi.unisignature.verifier.rules.Rfc3161RecordTimeRule;
import com.guardtime.ksi.unisignature.verifier.rules.Rule;
import com.guardtime.ksi.unisignature.verifier.rules.SignatureInputHashAlgorithmDeprecatedRule;
import com.guardtime.ksi.unisignature.verifier.rules.SignaturePublicationRecordPublicationHashRule;
import com.guardtime.ksi.unisignature.verifier.rules.SignaturePublicationRecordPublicationTimeRule;

import java.util.LinkedList;
import java.util.List;

/**
 * Checks KSI signature's internal consistency. The verification described in this policy
 * assumes, the signature being verified is syntactically correct - it parses correctly and contains all the mandatory
 * elements. Parsing of the signature must be completed before the verification process.
 */
public class InternalVerificationPolicy implements Policy {

    private static final String TYPE_INTERNAL_VERIFICATION_POLICY = "INTERNAL_VERIFICATION_POLICY";

    private final List<Rule> rules = new LinkedList<>();
    private Policy fallbackPolicy;

    public InternalVerificationPolicy() {
        rules.add(new DocumentHashAlgorithmVerificationRule());
        rules.add(new DocumentHashVerificationRule());
        rules.add(new InputHashLevelVerificationRule());
        rules.add(new SignatureInputHashAlgorithmDeprecatedRule());

        rules.add(new Rfc3161InternalHashAlgorithmsDeprecatedRule());
        rules.add(new Rfc3161OutputHashAlgorithmDeprecatedRule());
        rules.add(new AggregationChainInputHashVerificationRule());
        rules.add(new Rfc3161RecordIndexRule());
        rules.add(new Rfc3161RecordTimeRule());
        rules.add(new AggregationHashChainIndexSuccessorRule());

        rules.add(new AggregationHashChainLinkMetadataRule());
        rules.add(new AggregationHashChainAlgorithmDeprecatedRule());

        rules.add(new AggregationHashChainConsistencyRule());
        rules.add(new AggregationHashChainTimeConsistencyRule());
        rules.add(new AggregationHashChainIndexConsistencyRule());

        // verify calendar hash chain (if present)
        rules.add(new CalendarHashChainInputHashVerificationRule());
        rules.add(new CalendarHashChainAggregationTimeRule());
        rules.add(new CalendarHashChainRegistrationTimeRule());
        rules.add(new CalendarHashChainAggregationAlgorithmObsoleteRule());

        // verify publication record (if present)
        rules.add(new SignaturePublicationRecordPublicationTimeRule());
        rules.add(new SignaturePublicationRecordPublicationHashRule());

        // verify calendar authentication record (if present)
        rules.add(new CalendarAuthenticationRecordAggregationTimeRule());
        rules.add(new CalendarAuthenticationRecordAggregationHashRule());
    }

    /**
     * Used to add a new rule to the list of rules to be used to verify signature.
     *
     * @param rule
     *         rule to be added. not null.
     */
    protected final void addRule(Rule rule) {
        rules.add(rule);
    }

    /**
     * Returns the list of rules to be used verify the KSI signature.
     *
     * @return list of rules. always present.
     */
    public List<Rule> getRules() {
        return rules;
    }

    public String getName() {
        return "Internal verification policy";
    }

    public String getType() {
        return TYPE_INTERNAL_VERIFICATION_POLICY;
    }

    public void setFallbackPolicy(Policy policy) {
        this.fallbackPolicy = policy;
    }

    public Policy getFallbackPolicy() {
        return fallbackPolicy;
    }

}
