protoc-jar release notes
========================

#### 3.5.1.1 (27-Jan-2018)
* Fix regression in shading (due to incorrect version formatting)

#### 3.5.1 (20-Jan-2018)
* Upgrade to protoc 3.5.1
* Supports FreeBSD/x86, Linux/POWER8 (freebsd-x86_64, linux-ppcle_64)
* Supports Linux/ARM, provided by Google (linux-aarch_64)

#### 3.5.0 (28-Nov-2017)
* Upgrade to protoc 3.5.0
* Supports FreeBSD/x86, Linux/POWER8 (freebsd-x86_64, linux-ppcle_64)
* Supports Linux/ARM, provided by Google now (linux-aarch_64)

#### 3.4.0.2 (12-Nov-2017)
* Support for Linux on ARM platform (linux-aarch_64; 2.4.1, 2.6.1, 3.4.0)
* Fix: download would not replace existing file (eg, maven-metadata.xml)

#### 3.4.0.1 (29-Sep-2017)
* Support for Linux on POWER8 platform (linux-ppcle_64)
* Support for FreeBSD on x86 platform (freebsd-x86_64)
* Support unbundled binaries with automatic download from maven central
* Remove 3.x version map to 3.4.0

#### 3.4.0 (29-Aug-2017)
* Upgrade to protoc 3.4.0
* Map previous 3.x versions to 3.4.0 for backward compatibility
* Support POWER8 (ppc64le) platform (protoc 3.4.0 only)

#### 3.3.0.1 (22-Jun-2017)
* Use alternative dir (user.home) if execution in temp dir fails (issue #24)

#### 3.3.0 (26-May-2017)
* Upgrade to protoc 3.3.0
* Map 3.0.0, 3.1.0, 3.2.0 to 3.3.0 for backward compatibility
* Fix for shading when tmp dir and source code are on different filesystems (option `--java_shaded_out`)

#### 3.2.0.1 (2-Apr-2017)
* Separate proto3 and proto2 standard types (option `--include_std_types`)
* Better error message for unsupported versions (issue #19)

#### 3.2.0 (15-Feb-2017)
* Upgrade to protoc 3.2.0
* Map 3.0.0, 3.1.0 to 3.2.0 for backward compatibility
* Implement retry as workaround for text file busy issue #8

#### 3.1.0.3 (17-Jan-2017)
* Fix PlatformDetector NPE on Linux

#### 3.1.0.2 (5-Jan-2017)
* Support downloading protoc from maven central (option `-v:<group>:<artifact>:<version>`)

#### 3.1.0.1 (27-Oct-2016)
* Upgrade Linux 3.1.0 binary to protoc 3.1.0-build2 (issue #13)

#### 3.1.0 (8-Oct-2016)
* Upgrade to protoc 3.1.0
* Map 3.0.0 to 3.1.0 for backward compatibility

#### 3.0.0.1 (28-Aug-2016)
* Package and support google.protobuf standard types out of the box (option `--include_std_types`)

#### 3.0.0 (2-Aug-2016)
* Upgrade to protoc 3.0.0

#### 3.0.0-b4 (26-Jul-2016)
* Upgrade to protoc 3.0.0-beta-4

#### 3.0.0-b3 (18-May-2016)
* Upgrade to protoc 3.0.0-beta-3
* Support shading of generated code for use with `protobuf-java-shaded-[241|250|261]` (option `--java_shaded_out`)

#### 3.0.0-b2.1 (27-Feb-2016)
* Fix for text file busy issue #8 (reported for Ubuntu 14.04)

#### 3.0.0-b2 (11-Jan-2016)
* Upgrade to protoc 3.0.0-beta-2

#### 3.0.0-b1 (15-Oct-2015)
* Upgrade to protoc 3.0.0-beta-1
* All 3.0.0-beta-1 binaries from Google (maven central)

#### 3.0.0-a3 (15-Jun-2015)
* Add support for protoc 3.0.0-alpha-3

#### 2.x.5 (20-Apr-2015)
* Single package to include all versions

#### 2.4.1.4, 2.5.0.4, 2.6.1.4 (26-Mar-2015)
* Minor fixes, no functional changes

#### 2.4.1.3, 2.5.0.3, 2.6.1.3 (31-Dec-2014)
* Rebuilt Linux binaries on older toolchain for better portability

#### 2.4.1.2, 2.5.0.2, 2.6.1.2 (30-Dec-2014)
* Add support for protoc 2.6.1
* Stripped protoc binaries, a bit more compact
* Minor changes for platform detection and reporting

#### 2.4.1.1, 2.5.0.1 (26-Dec-2014)
* Cosmetic release after branch refactoring, no functional changes
* Retired builder pom.xml

#### 2.4.1.0, 2.5.0.0 (7-Jun-2014)
* Initial release, support protoc 2.4.1 and 2.5.0
* Windows binary from Google; Mac/OSX and Linux 64 bit builds
