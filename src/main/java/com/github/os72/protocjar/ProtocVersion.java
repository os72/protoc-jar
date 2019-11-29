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

public class ProtocVersion
{
	public static final ProtocVersion PROTOC_VERSION = new ProtocVersion(null, null, "3.10.1");

	public static ProtocVersion getVersion(String spec) {
		if (!spec.startsWith("-v")) return null;
		ProtocVersion version = null;
		String[] as = spec.split(":");
		if (as.length == 4 && as[0].equals("-v")) version = new ProtocVersion(as[1], as[2], as[3]);
		else version = new ProtocVersion(null, null, spec.substring(2));
		if (version.mVersion.length() == 3) { // "123" -> "1.2.3"
			String dotVersion = version.mVersion.charAt(0) + "." + version.mVersion.charAt(1) + "." + version.mVersion.charAt(2);
			version = new ProtocVersion(version.mGroup, version.mArtifact, dotVersion);
		}
		return version;
	}

	public ProtocVersion(String group, String artifact, String version) {
		mGroup = group;
		mArtifact = artifact;
		mVersion = version;
	}

	@Override
	public String toString() {
		if (mArtifact == null) return mVersion;
		return mGroup+":"+mArtifact+":"+mVersion;
	}

	public final String mGroup;
	public final String mArtifact;
	public final String mVersion;
}
