protoc-jar
==========

Protocol Buffers compiler - multi-platform executable JAR and API.
Available on Maven Central: http://central.maven.org/maven2/com/github/os72/protoc-jar/

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.os72/protoc-jar/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.os72/protoc-jar)

---

Simple convenience JAR that embeds protoc compiler binaries for Linux, Mac/OSX, and Windows, providing some portability across the major platforms

See also
* https://github.com/os72/protoc-jar-maven-plugin (maven plugin for protobuf code generation)
* https://github.com/google/protobuf (Protocol Buffers site)

Version support
* protobuf 2.4.1: -v2.4.1, -v241
* protobuf 2.5.0: -v2.5.0, -v250
* protobuf 2.6.1: -v2.6.1, -v261

#### Usage - executable
```
$ java -jar protoc-jar-2.x.5.jar -v2.4.1 --version
protoc-jar: protoc version: 241, detected platform: windows 8/amd64
protoc-jar: executing: [C:\cygwin64\tmp\protoc4075756100699382860.exe, --version]
libprotoc 2.4.1

$ java -jar protoc-jar-2.x.5.jar -v2.5.0 --version
protoc-jar: protoc version: 250, detected platform: windows 8/amd64
protoc-jar: executing: [C:\cygwin64\tmp\protoc7121779431303398811.exe, --version]
libprotoc 2.5.0

$ java -jar protoc-jar-2.x.5.jar -v2.6.1 --version
protoc-jar: protoc version: 261, detected platform: windows 8/amd64
protoc-jar: executing: [C:\cygwin64\tmp\protoc2894421465727929903.exe, --version]
libprotoc 2.6.1
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
  <version>2.x.5</version>
</dependency>
```
