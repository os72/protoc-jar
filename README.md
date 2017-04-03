protoc-jar
==========

Protocol Buffers protobuf compiler - multi-platform executable protoc JAR and API.
Available on Maven Central: http://central.maven.org/maven2/com/github/os72/protoc-jar/3.2.0.1/

[![Maven Central](https://img.shields.io/badge/maven%20central-3.2.0.1-brightgreen.svg)](http://search.maven.org/#artifactdetails|com.github.os72|protoc-jar|3.2.0.1|)

---

Simple convenience JAR that embeds protoc compiler binaries for Linux, Mac/OSX, and Windows, providing some portability across the major platforms. At runtime the library detects the platform and executes the corresponding protoc binary.
Supports protoc versions 2.4.1, 2.5.0, 2.6.1, 3.2.0. Also supports downloading protoc from maven central

See also
* https://github.com/os72/protoc-jar-maven-plugin
* https://github.com/os72/protobuf-java-shaded-241
* https://github.com/os72/protobuf-java-shaded-250
* https://github.com/os72/protobuf-java-shaded-261
* https://github.com/google/protobuf

Version support
* protobuf 2.4.1: `-v2.4.1`, `-v241`
* protobuf 2.5.0: `-v2.5.0`, `-v250`
* protobuf 2.6.1: `-v2.6.1`, `-v261`
* protobuf 3.2.0: `-v3.2.0`, `-v320`
* download from maven central: `-v:<group>:<artifact>:<version>` (eg, `-v:com.google.protobuf:protoc:3.2.0`)

#### Usage - executable
```
$ java -jar protoc-jar-3.2.0.1.jar -v2.4.1 --version
protoc-jar: protoc version: 241, detected platform: windows 8/amd64
protoc-jar: executing: [C:\cygwin64\tmp\protocjar4075756100699382860\bin\protoc.exe, --version]
libprotoc 2.4.1

$ java -jar protoc-jar-3.2.0.1.jar -v2.5.0 --version
protoc-jar: protoc version: 250, detected platform: windows 8/amd64
protoc-jar: executing: [C:\cygwin64\tmp\protocjar7121779431303398811\bin\protoc.exe, --version]
libprotoc 2.5.0

$ java -jar protoc-jar-3.2.0.1.jar -v2.6.1 --version
protoc-jar: protoc version: 261, detected platform: windows 8/amd64
protoc-jar: executing: [C:\cygwin64\tmp\protocjar2894421465727929903\bin\protoc.exe, --version]
libprotoc 2.6.1

$ java -jar protoc-jar-3.2.0.1.jar -v3.2.0 --version
protoc-jar: protoc version: 320, detected platform: windows 8/amd64
protoc-jar: executing: [C:\cygwin64\tmp\protocjar4836429907662708747\bin\protoc.exe, --version]
libprotoc 3.2.0

$ java -jar protoc-jar-3.2.0.1.jar -v:com.google.protobuf:protoc:3.2.0 --version
protoc-jar: protoc version: com.google.protobuf:protoc:3.2.0, detected platform: windows 8/amd64
protoc-jar: downloading: http://central.maven.org/maven2/com/google/protobuf/protoc/3.2.0/protoc-3.2.0-windows-x86_64.exe
protoc-jar: cached: C:\cygwin64\tmp\protocjar.webcache\com\google\protobuf\protoc\3.2.0\protoc-3.2.0-windows-x86_64.exe
protoc-jar: executing: [C:\cygwin64\tmp\protocjar2416601643482368618\bin\protoc.exe, --version]
libprotoc 3.2.0
```

#### Usage - executable, include google.protobuf standard types (option --include_std_types)
```
$ java -jar protoc-jar-3.2.0.1.jar --include_std_types -I. --java_out=out StdTypeExample.proto
protoc-jar: protoc version: 320, detected platform: windows 8/amd64
protoc-jar: executing: [C:\cygwin64\tmp\protocjar25170607065138750\bin\protoc.exe, -IC:\cygwin64\tmp\protocjar25170607065138750\include, -I., --java_out=out, StdTypeExample.proto]
```

#### Usage - executable, apply shading for use with protobuf-java-shaded-241 (option --java_shaded_out)
```
$ java -jar protoc-jar-3.2.0.1.jar -v2.4.1 --java_shaded_out=out PersonSchema.proto
protoc-jar: protoc version: 241, detected platform: windows 8/amd64
protoc-jar: executing: [C:\cygwin64\tmp\protocjar3155568375885699334\bin\protoc.exe, --java_out=out, PersonSchema.proto]
protoc-jar: shading (version 241): out
```

#### Usage - API
```java
import com.github.os72.protocjar.Protoc;
...
String[] args = {"-v241", "--help"};
Protoc.runProtoc(args);
```

#### Maven dependency

```xml
<dependency>
  <groupId>com.github.os72</groupId>
  <artifactId>protoc-jar</artifactId>
  <version>3.2.0.1</version>
</dependency>
```
