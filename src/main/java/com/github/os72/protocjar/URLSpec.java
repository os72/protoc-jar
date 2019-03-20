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
