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

package com.guardtime.ksi.pdu.v1;

import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.pdu.KSIRequestContext;
import com.guardtime.ksi.service.KSIProtocolException;
import com.guardtime.ksi.service.client.ServiceCredentials;
import com.guardtime.ksi.tlv.GlobalTlvTypes;
import com.guardtime.ksi.tlv.TLVElement;

class ExtensionResponseV1 extends AbstractKSIResponse<ExtensionResponsePayloadV1>  {

    /**
     * This constructor is used to parse response messages. Also does the basic validation.
     *
     * @param rootElement
     *         instance of{@link TLVElement}
     * @param context
     *         instance of {@link KSIRequestContext}. may not be null
     * @param credentials
     *         instance of {@link ServiceCredentials}. may not be null
     * @throws KSIProtocolException
     *         - will be thrown when TLV message parsing fails
     */
    public ExtensionResponseV1(TLVElement rootElement, KSIRequestContext context, ServiceCredentials credentials) throws KSIException {
        super(rootElement, context, credentials);
    }

    @Override
    protected final ExtensionResponsePayloadV1 parse(TLVElement element) throws KSIException {
        return new ExtensionResponsePayloadV1(element);
    }

    @Override
    public int getElementType() {
        return GlobalTlvTypes.ELEMENT_TYPE_EXTENSION_PDU_V1;
    }

}
