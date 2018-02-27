/*
 *
 *  Copyright 2013-2018 Guardtime, Inc.
 *
 *  This file is part of the Guardtime client SDK.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License").
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import com.guardtime.ksi.CommonTestUtil;
import com.guardtime.ksi.service.http.simple.SimpleHttpExtenderClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SharedVerificationIntegrationTest extends AbstractCommonIntegrationTest {
    protected static final String INTERNAL_POLICY_SIGNATURES = "INTERNAL_POLICY_SIGNATURES";
    protected static final String INVALID_SIGNATURES = "INVALID_SIGNATURES";
    protected static final String POLICY_VERIFICATION_SIGNATURES = "POLICY_VERIFICATION_SIGNATURES";
    protected static final String VALID_SIGNATURES = "VALID_SIGNATURES";

    private static final Logger logger = LoggerFactory.getLogger(SharedVerificationIntegrationTest.class);

    private static IntegrationTestDataHolder testData = null;

    @AfterMethod
    public void testTearDown() throws IOException {
        if (testData != null) testData.close();
    }

    @Test(groups = TEST_GROUP_INTEGRATION, dataProvider = VALID_SIGNATURES)
    public void testValidSignatures(IntegrationTestDataHolder testData) throws Exception {
        testExecution(testData);
    }

    @Test(groups = TEST_GROUP_INTEGRATION, dataProvider = INVALID_SIGNATURES)
    public void testInvalidSignatures(IntegrationTestDataHolder testData) throws Exception {
        testExecution(testData);
    }

    @Test(groups = TEST_GROUP_INTEGRATION, dataProvider = INTERNAL_POLICY_SIGNATURES)
    public void testInternalPolicySignatures(IntegrationTestDataHolder testData) throws Exception {
        testExecution(testData);
    }

    @Test(groups = TEST_GROUP_INTEGRATION, dataProvider = POLICY_VERIFICATION_SIGNATURES)
    public void testPolicyVerificationSignatures(IntegrationTestDataHolder testData) throws Exception {
        testExecution(testData);
    }

    @DataProvider(name = VALID_SIGNATURES)
    public static Object[][] getTestDataAndResultsForValidSignatures() throws Exception {
        try{
            return getTestFilesAndResults("valid-signatures/", "signature-results.csv");
        } catch (Throwable e){
            return new Object[][] {{}};
        }
    }

    @DataProvider(name = INTERNAL_POLICY_SIGNATURES)
    public static Object[][] getTestDataAndResultsForInternalPolicySignatures() throws Exception {
        try{
            return getTestFilesAndResults("internal-policy-signatures/", "internal-policy-results.csv");
        } catch (Throwable e){
            return new Object[][] {{}};
        }
    }

    @DataProvider(name = INVALID_SIGNATURES)
    public static Object[][] getTestDataAndResultsForInvalidSignatures() throws Exception {
        try{
            return getTestFilesAndResults("invalid-signatures/", "invalid-signature-results.csv");
        } catch (Throwable e){
            return new Object[][] {{}};
        }
    }

    @DataProvider(name = POLICY_VERIFICATION_SIGNATURES)
    public static Object[][] getTestDataAndResultsForPolicyVerificationSignatures() throws Exception {
        try{
            return getTestFilesAndResults("policy-verification-signatures/", "policy-verification-results.csv");
        } catch (Throwable e){
            return new Object[][] {{}};
        }
    }


    private static Object[][] getTestFilesAndResults(String path, String fileName) throws Exception {
        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(CommonTestUtil.loadFile(path + fileName))));
            ArrayList<String> lines = new ArrayList<>();
            String line;
            while ((line = fileReader.readLine()) != null) {
                if (!line.startsWith("#") && line.trim().length() > 17 && !line.contains(IntegrationTestAction.NOT_IMPLEMENTED.getName())) {
                    line = line.replace(";", "; ");
                    lines.add(line);
                }
            }

            int linesCount = lines.size();
            Object[][] data = new Object[linesCount][1];
            SimpleHttpExtenderClient extenderClient = new SimpleHttpExtenderClient(loadExtenderSettings());

            for (int i = 0; i < linesCount; i++) {
                try{
                     testData= new IntegrationTestDataHolder(path, lines.get(i).split(";"), extenderClient);
                    data[i] = new Object[]{testData};
                } catch (Exception e){
                    logger.warn("Error while parsing the following line: '" + lines.get(i) + "' from file: " + fileName);
                    throw e;
                }
            }
            return data;
        } finally {
            if (fileReader != null) {
                fileReader.close();
            }
        }
    }

}
