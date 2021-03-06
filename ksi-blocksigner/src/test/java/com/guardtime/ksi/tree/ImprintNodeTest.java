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

package com.guardtime.ksi.tree;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertTrue;

public class ImprintNodeTest {

    private static final byte[] INPUT_HASH_VALUE = new byte[32];

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "InputHash can not be null")
    public void testCreateImprintNodeWithoutDataHash() {
        DataHash dataHash = null;
        new ImprintNode(dataHash);
    }

    @Test
    public void testCreateImprintNode() {
        ImprintNode node = new ImprintNode(new DataHash(HashAlgorithm.SHA2_256, INPUT_HASH_VALUE));
        assertTrue(node.isLeaf());
        assertTrue(node.isRoot());
    }

    @Test
    public void testCreateHashTree() {
        ImprintNode left = new ImprintNode(new DataHash(HashAlgorithm.SHA2_256, INPUT_HASH_VALUE));
        ImprintNode right = new ImprintNode(new DataHash(HashAlgorithm.SHA2_256, INPUT_HASH_VALUE));
        ImprintNode root = new ImprintNode(left, right, new DataHash(HashAlgorithm.SHA1, new byte[20]), 2);

        assertTrue(root.isRoot());
        assertFalse(root.isLeft());

        assertTrue(left.isLeaf());
        assertTrue(right.isLeaf());

        assertTrue(left.isLeft());
        assertFalse(right.isLeft());
        assertEquals(root.getLeftChildNode(), left);
        assertEquals(root.getRightChildNode(), right);
    }

    @Test
    public void testCopyImprintNode() {
        DataHash dataHash = new DataHash(HashAlgorithm.SHA2_256, INPUT_HASH_VALUE);
        ImprintNode leftChildNode = new ImprintNode(dataHash);
        ImprintNode rightChildNode = new ImprintNode(dataHash);
        ImprintNode root = new ImprintNode(leftChildNode, rightChildNode, dataHash, 2);

        ImprintNode copy = new ImprintNode(root);

        assertEquals(copy.getLeftChildNode(), leftChildNode);
        assertEquals(copy.getRightChildNode(), rightChildNode);
        assertEquals(copy.getLevel(), root.getLevel());
        assertEquals(copy.getValue(), root.getValue());
        assertNotSame(copy, root);

        ImprintNode leftChildNodeCopy = new ImprintNode(leftChildNode);
        assertTrue(leftChildNodeCopy.isLeft());
    }

}
