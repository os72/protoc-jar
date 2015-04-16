protoc-jar
==========

Protocol Buffers compiler - multi-platform executable JAR and API.
Available on Maven Central: http://central.maven.org/maven2/com/github/os72/protoc-jar/

Simple convenience JAR that embeds protoc compiler binaries for Linux, Mac/OSX, and Windows, providing some portability across the major platforms.
See the Protocol Buffers site for details: https://github.com/google/protobuf

Maven plugin for protobuf code generation based on this: https://github.com/os72/protoc-jar-maven-plugin

Branches
* https://github.com/os72/protoc-jar/tree/protobuf_241 (protobuf 2.4.1)
* https://github.com/os72/protoc-jar/tree/protobuf_250 (protobuf 2.5.0)
* https://github.com/os72/protoc-jar/tree/protobuf_261 (protobuf 2.6.1)

#### Usage - executable
```
$ java -jar protoc-jar-2.4.1.4.jar --version
protoc-jar: protoc version: 241, detected platform: windows 8/amd64
protoc-jar: executing: [C:\cygwin64\tmp\protoc8662657713800064679.exe, --version]
libprotoc 2.4.1

$ java -jar protoc-jar-2.5.0.4.jar --version
protoc-jar: protoc version: 250, detected platform: windows 8/amd64
protoc-jar: executing: [C:\cygwin64\tmp\protoc1183088349500847030.exe, --version]
libprotoc 2.5.0

$ java -jar protoc-jar-2.6.1.4.jar --version
protoc-jar: protoc version: 261, detected platform: windows 8/amd64
protoc-jar: executing: [C:\cygwin64\tmp\protoc512014267302891752.exe, --version]
libprotoc 2.6.1
```

#### Usage - API
```java
import com.github.os72.protocjar.Protoc;
...
String[] args = {"--help"};
Protoc.runProtoc(args);
```

#### Maven dependency

For protobuf 2.4.1
```xml
<dependency>
  <groupId>com.github.os72</groupId>
  <artifactId>protoc-jar</artifactId>
  <version>2.4.1.4</version>
</dependency>
```

For protobuf 2.5.0
```xml
<dependency>
  <groupId>com.github.os72</groupId>
  <artifactId>protoc-jar</artifactId>
  <version>2.5.0.4</version>
</dependency>
```

For protobuf 2.6.1
```xml
<dependency>
  <groupId>com.github.os72</groupId>
  <artifactId>protoc-jar</artifactId>
  <version>2.6.1.4</version>
</dependency>
```
