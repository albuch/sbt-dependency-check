# dependency-check-sbt
[![Build Status](https://travis-ci.org/albuch/dependency-check-sbt.svg)](https://travis-ci.org/albuch/dependency-check-sbt) [![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
# Usage
## Configuration
`dependency-check-sbt` uses the default configuration of OWASP [DependencyCheck](https://github.com/jeremylong/DependencyCheck). You can override all settings with sbt task settings.
## Tasks
### Check
### Aggregate

# Known issues
* Check task runs forever, CVE database file size increases drastically
  * SBT projects that have a H2 database version 1.4.x in the classpath (e.g. Play Framework projects) will have issues with the dependency check maintenance task that is run after fetching NVD data sets und inserting them in the database. For the initial data import the database file size will increase up to several GB and the task will run for more than 1 hour depending on the hardware resources.
  * Current proposed workaround is to override the dependency to the latest 1.3.x release of H2 database in `project/plugins.sbt`.

    ```sbt
    dependencyOverrides += "com.h2database" % "h2" % "1.3.176"
    ```
# License
Copyright 2016 Alexander v. Buchholtz

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
