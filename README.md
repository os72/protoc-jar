protoc-jar
==========

Protocol Buffers compiler - executable JAR and API

Simple convenience JAR that embeds protoc compiler binaries for Linux, Mac/OSX, and Windows. See the Protocol Buffers site for details: https://code.google.com/p/protobuf/

Maven code generation plugin based on this: https://github.com/os72/protoc-jar-maven-plugin

#### Usage - executable
```
$ java -jar protoc-jar-2.4.1.0.jar --version
protoc-jar: executing: [C:\cygwin64\tmp\protoc8420137375795222513.exe, --version]
libprotoc 2.4.1

$ java -jar protoc-jar-2.5.0.0.jar --version
protoc-jar: executing: [C:\cygwin64\tmp\protoc4520968910109233475.exe, --version]
libprotoc 2.5.0
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
  <version>2.4.1.0</version>
</dependency>
```

For protobuf 2.5.0
```xml
<dependency>
  <groupId>com.github.os72</groupId>
  <artifactId>protoc-jar</artifactId>
  <version>2.5.0.0</version>
</dependency>
```
