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
package com.guardtime.ksi.service.client.http;

import java.net.URL;

/**
 * Contains low level connection parameters for HTTP service.
 */
public class HTTPConnectionParameters {

    private static final int UNDEFINED_TIMEOUT = -1;

    private URL proxyUrl;
    private String proxyUser;
    private String proxyPassword;

    private int connectionTimeout = UNDEFINED_TIMEOUT;
    private int readTimeout = UNDEFINED_TIMEOUT;

    /**
     * Creates new set of HTTP connection parameters.
     */
    public HTTPConnectionParameters() {}

    /**
     * Creates new set of HTTP connection parameters.
     *
     * @param connectionTimeout timeout in milliseconds until a connection is established.
     * @param readTimeout socket read timeout in milliseconds.
     */
    public HTTPConnectionParameters(int connectionTimeout, int readTimeout) {
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * Determines the timeout in milliseconds until a connection is established.
     * <p>
     * A timeout value of zero is interpreted as an infinite timeout. A negative value is interpreted as undefined
     * (system default).
     * </p><p>
     * Default value is -1.
     * </p>
     *
     * @return Connection timeout in milliseconds as int.
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Defines the socket read timeout in milliseconds, which is the timeout for waiting for data or a maximum period
     * inactivity between two consecutive data packets.
     * <p>
     * A timeout value of zero is interpreted as an infinite timeout. A negative value is interpreted as undefined
     * (system default).
     * </p><p>
     * Default value is -1.
     * </p>
     *
     * @return Read timeout in milliseconds as int.
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * @return Proxy password.
     */
    public String getProxyPassword() {
        return proxyPassword;
    }

    /**
     * Set proxy password.
     *
     * @param proxyPassword proxy password.
     */
    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    /**
     * @return proxy URL.
     */
    public URL getProxyUrl() {
        return proxyUrl;
    }

    /**
     * Set proxy URL.
     *
     * @param proxyUrl proxy URL.
     */
    public void setProxyUrl(URL proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

    /**     
     * @return Proxy username.
     */
    public String getProxyUser() {
        return proxyUser;
    }

    /**
     * Set proxy username.
     *
     * @param proxyUser proxy username.
     */
    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

}
