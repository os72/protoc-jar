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

import java.util.HashMap;
import java.util.Map;

public class ProtocVersion
{
	public static final ProtocVersion PROTOC_VERSION = new ProtocVersion(null, null, "320");

	public static ProtocVersion getVersion(String spec) {
		String v = sVersionMap.get(spec.replace(".", ""));
		if (v != null) return new ProtocVersion(null, null, v);		
		String[] as = spec.split(":");
		if (as.length == 4 && as[0].equals("-v")) return new ProtocVersion(as[1], as[2], as[3]);
		return null;
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

	private static Map<String,String> sVersionMap = new HashMap<String,String>();
	static {
		sVersionMap.put("-v320", "320");
		sVersionMap.put("-v310", "320");
		sVersionMap.put("-v300", "320");
		sVersionMap.put("-v261", "261");
		sVersionMap.put("-v250", "250");
		sVersionMap.put("-v241", "241");
	}
}
