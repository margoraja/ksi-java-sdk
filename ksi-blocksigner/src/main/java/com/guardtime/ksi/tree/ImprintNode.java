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

import static com.guardtime.ksi.util.Util.notNull;

/**
 * Represents a hash tree node. Every non-leaf node is labelled with the hash of the labels or values
 * (in case of leaves) of its child nodes.
 */
public class ImprintNode implements TreeNode {

    private final DataHash value;
    private final long level;

    private TreeNode parent;
    private TreeNode leftChild;
    private TreeNode rightChild;

    private boolean left = false;

    /**
     * Creates a copy of a node.
     *
     * @param node node to be copied.
     */
    public ImprintNode(ImprintNode node) {
        notNull(node, "ImprintNode");
        this.value = node.value;
        this.level = node.level;
        this.parent = node.parent;
        this.leftChild = node.leftChild;
        this.rightChild = node.rightChild;
        this.left = node.left;
    }

    /**
     * Creates a new leaf node with given hash and level 0.
     *
     * @param value hash of the new node.
     */
    public ImprintNode(DataHash value) {
        this(value, 0L);
    }

    /**
     * Creates a leaf node with given hash and level.
     *
     * @param value hash of the new node.
     * @param level level of the new node.
     */
    public ImprintNode(DataHash value, long level) {
        notNull(value, "InputHash");
        this.value = value;
        this.level = level;
    }

    /**
     * Creates a non-leaf node.
     *
     * @param leftChild left child node of the new node.
     * @param rightChild right child node of the new node.
     * @param value hash of the new node.
     * @param level level of the new node.
     */
    public ImprintNode(ImprintNode leftChild, ImprintNode rightChild, DataHash value, long level) {
        this(value, level);
        notNull(leftChild, "LeftChild");
        notNull(rightChild, "RightChild");
        leftChild.parent = this;
        leftChild.left = true;
        rightChild.parent = this;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    /**
     * Creates a non-leaf node.
     *
     * @param leftChild  left child node of the new node.
     * @param rightChild right child node of the new node.
     * @param value      hash of the new node.
     * @param level      level of the new node.
     */
    ImprintNode(ImprintNode leftChild, MetadataNode rightChild, DataHash value, long level) {
        this(value, level);
        notNull(leftChild, "LeftChild");
        notNull(rightChild, "RightChild");
        leftChild.parent = this;
        leftChild.left = true;
        rightChild.parent = this;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    public byte[] getValue() {
        return value.getImprint();
    }

    public TreeNode getParent() {
        return parent;
    }

    public TreeNode getLeftChildNode() {
        return leftChild;
    }

    public TreeNode getRightChildNode() {
        return rightChild;
    }

    public boolean isLeft() {
        return left;
    }

    public long getLevel() {
        return level;
    }

    public boolean isRoot() {
        return getParent() == null;
    }

    public boolean isLeaf() {
        return getLeftChildNode() == null && getRightChildNode() == null;
    }

    public boolean hasMetadata() {
        return parent != null && parent.getRightChildNode() instanceof MetadataNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImprintNode that = (ImprintNode) o;

        if (level != that.level) return false;
        return !(value != null ? !value.equals(that.value) : that.value != null);

    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (int) (level ^ (level >>> 32));
        return result;
    }

}
