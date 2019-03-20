/*
 * Copyright 2014 protoc-jar developers
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.os72.protocjar;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

public class URLSpec
{
	public URLSpec(String url) throws MalformedURLException {
		mUrl = new URL(url);
	}
	public URLSpec(URL url) {
		mUrl = url;
	}

	public URLSpec(String url, String host, int port) throws MalformedURLException {
		mUrl = new URL(url);
		mProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
	}
	public URLSpec(String url, Proxy proxy) throws MalformedURLException {
		mUrl = new URL(url);
		mProxy = proxy;
	}
	public URLSpec(URL url, Proxy proxy) {
		mUrl = url;
		mProxy = proxy;
	}

	public URLConnection openConnection() throws IOException {
		if (mProxy == null) return mUrl.openConnection();
		return mUrl.openConnection(mProxy);
	}

	@Override
	public String toString() {
		if (mProxy == null) return mUrl.toString();
		return mUrl.toString() + " [" + mProxy + "]";
	}

	private URL mUrl;
	private Proxy mProxy;
}
