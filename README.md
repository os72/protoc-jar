protoc-jar
==========

Protocol Buffers protobuf compiler - multi-platform executable protoc JAR and API.
Available on Maven Central: https://repo.maven.apache.org/maven2/com/github/os72/protoc-jar/3.11.4/

[![Maven Central](https://img.shields.io/badge/maven%20central-3.11.4-brightgreen.svg)](http://search.maven.org/#artifactdetails|com.github.os72|protoc-jar|3.11.4|)
[![Join the chat at https://gitter.im/os72/community](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/os72/community)

---

Simple convenience JAR that embeds protoc compiler binaries for Linux, Mac/OSX, and Windows, providing some portability across the major platforms. At runtime the library detects the platform and executes the corresponding protoc binary.
Supports embedded protoc versions 2.4.1, 2.5.0, 2.6.1, 3.11.4, and any protoc version available for download from maven central

* Support for FreeBSD on x86 platform (freebsd-x86_64), thanks [kjopek](https://github.com/kjopek)
* Support for Solaris on x86 platform (sunos-x86_64), thanks [siepkes](https://github.com/siepkes)
* Support for Linux on POWER8 platform (linux-ppcle_64), now from Google
  * Older versions (up to 3.6.0), thanks to [Apache SystemML](https://github.com/apache/systemml) folks ([nakul02](https://github.com/nakul02))
* Support for Linux on ARM platform (linux-aarch_64), now from Google
  * Older versions (2.4.1, 2.6.1, 3.4.0), thanks [garciagorka](https://github.com/garciagorka)

See also
* https://github.com/os72/protoc-jar-maven-plugin
* https://github.com/os72/protobuf-java-shaded-241
* https://github.com/os72/protobuf-java-shaded-250
* https://github.com/os72/protobuf-java-shaded-261
* https://github.com/os72/protobuf-java-shaded-351
* https://github.com/os72/protobuf-java-shaded-360
* https://github.com/os72/protobuf-java-shaded-3-11-1
* https://github.com/google/protobuf

Binaries
* https://repo.maven.apache.org/maven2/com/google/protobuf/protoc/
* https://repo.maven.apache.org/maven2/com/github/os72/protoc/
* https://oss.sonatype.org/content/repositories/snapshots/com/github/os72/protoc/

Version support
* protobuf 2.4.1: `-v2.4.1`
* protobuf 2.5.0: `-v2.5.0`
* protobuf 2.6.1: `-v2.6.1`
* protobuf 3.11.4: `-v3.11.4`
* Other versions: will attempt download from maven central
* Download by maven artifact id: `-v:<group>:<artifact>:<version>` (eg, `-v:com.google.protobuf:protoc:3.0.0`)

#### Usage - executable
```
$ java -jar protoc-jar-3.11.4.jar -v2.4.1 --version
protoc-jar: protoc version: 2.4.1, detected platform: windows-x86_64 (windows 8.1/amd64)
protoc-jar: embedded: bin/2.4.1/protoc-2.4.1-windows-x86_64.exe
protoc-jar: executing: [C:\cygwin64\tmp\protocjar4043021753132417014\bin\protoc.exe, --version]
libprotoc 2.4.1

$ java -jar protoc-jar-3.11.4.jar -v2.5.0 --version
protoc-jar: protoc version: 2.5.0, detected platform: windows-x86_64 (windows 8.1/amd64)
protoc-jar: embedded: bin/2.5.0/protoc-2.5.0-windows-x86_64.exe
protoc-jar: executing: [C:\cygwin64\tmp\protocjar1249962506895049512\bin\protoc.exe, --version]
libprotoc 2.5.0

$ java -jar protoc-jar-3.11.4.jar -v2.6.1 --version
protoc-jar: protoc version: 2.6.1, detected platform: windows-x86_64 (windows 8.1/amd64)
protoc-jar: embedded: bin/2.6.1/protoc-2.6.1-windows-x86_64.exe
protoc-jar: executing: [C:\cygwin64\tmp\protocjar6776927966028536935\bin\protoc.exe, --version]
libprotoc 2.6.1

$ java -jar protoc-jar-3.11.4.jar -v3.11.4 --version
protoc-jar: protoc version: 3.11.4, detected platform: windows-x86_64 (windows 8.1/amd64)
protoc-jar: embedded: bin/3.11.4/protoc-3.11.4-windows-x86_64.exe
protoc-jar: executing: [C:\cygwin64\tmp\protocjar6721276946617095290\bin\protoc.exe, --version]
libprotoc 3.11.4

$ java -jar protoc-jar-3.11.4.jar -v3.1.0 --version
protoc-jar: protoc version: 3.1.0, detected platform: windows-x86_64 (windows 8.1/amd64)
protoc-jar: downloading: http://central.maven.org/maven2/com/google/protobuf/protoc/maven-metadata.xml
protoc-jar: saved: C:\cygwin64\tmp\protocjar.webcache\com\google\protobuf\protoc\maven-metadata.xml
protoc-jar: downloading: http://central.maven.org/maven2/com/github/os72/protoc/maven-metadata.xml
protoc-jar: saved: C:\cygwin64\tmp\protocjar.webcache\com\github\os72\protoc\maven-metadata.xml
protoc-jar: cached: C:\cygwin64\tmp\protocjar.webcache\com\google\protobuf\protoc\maven-metadata.xml
protoc-jar: downloading: http://central.maven.org/maven2/com/google/protobuf/protoc/3.1.0-build2/protoc-3.1.0-build2-windows-x86_64.exe
protoc-jar: saved: C:\cygwin64\tmp\protocjar.webcache\com\google\protobuf\protoc\3.1.0-build2\protoc-3.1.0-build2-windows-x86_64.exe
protoc-jar: executing: [C:\cygwin64\tmp\protocjar1755669222845599671\bin\protoc.exe, --version]
libprotoc 3.1.0
```

#### Usage - executable, include google.protobuf standard types (option --include_std_types)
```
$ java -jar protoc-jar-3.11.4.jar --include_std_types -I. --java_out=out StdTypeExample.proto
protoc-jar: protoc version: 3.11.4, detected platform: windows-x86_64 (windows 8.1/amd64)
protoc-jar: embedded: bin/3.11.4/protoc-3.11.4-windows-x86_64.exe
protoc-jar: executing: [C:\cygwin64\tmp\protocjar4162580735258750520\bin\protoc.exe, -IC:\cygwin64\tmp\protocjar4162580735258750520\include, -I., --java_out=out, StdTypeExample.proto]
```

#### Usage - executable, apply shading for use with protobuf-java-shaded-241 (option --java_shaded_out)
```
$ java -jar protoc-jar-3.11.4.jar -v2.4.1 --java_shaded_out=out PersonSchema.proto
protoc-jar: protoc version: 2.4.1, detected platform: windows-x86_64 (windows 8.1/amd64)
protoc-jar: embedded: bin/2.4.1/protoc-2.4.1-windows-x86_64.exe
protoc-jar: executing: [C:\cygwin64\tmp\protocjar139806143399660474\bin\protoc.exe, --java_out=out, PersonSchema.proto]
protoc-jar: shading (version 2.4.1): out
```

#### Usage - run platform detector (option -pp, print platform, usually for debugging)
```
$ java -jar protoc-jar-3.11.4.jar -pp
------------------------------------------------------------------------
Detecting the operating system and CPU architecture
------------------------------------------------------------------------
os.detected.name: windows
os.detected.arch: x86_64
os.detected.version: 6.3
os.detected.version.major: 6
os.detected.version.minor: 3
os.detected.classifier: windows-x86_64
Detected platform: windows-x86_64 (windows 8.1/amd64)
```

#### Usage - API
```java
import com.github.os72.protocjar.Protoc;
...
String[] args = {"-v2.4.1", "--help"};
Protoc.runProtoc(args);
```

#### Maven dependency

```xml
<dependency>
  <groupId>com.github.os72</groupId>
  <artifactId>protoc-jar</artifactId>
  <version>3.11.4</version>
</dependency>
```
