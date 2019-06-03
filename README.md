# **Maven Pipeline**

Shared library that provides a basic `Maven-Docker` pipeline.

## **What will offer the shared library?**

* Build `Maven` projects that would be package it into `Docker Image`
* Support backing services (mysql, rabbitMQ, etc.) in order to compile your `Maven` project

## **How can I use the shared library?

* Add the shared library to `Jenkins` as `maven-docker-pipeline`

* Your `Maven` project must follow the following structure.


```sh
.
├── Jenkinsfile
├── docker
│   └── Dockerfile
├── docker-compose-backing-services.yml
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   └── resources
│   └── test
└── waitforit.yml
```

**Brief Description**

* **Backing Services Support.** In order to compile with required backing services, you will need to provide a `docker-compose` file with the services that you will need in order to get a successful build. i.e.

```sh
version: '3.7'

services:

  database:
    image: mysql:5.7.24
    ports:
      - 3306:3306
    environment:
      MYSQL_ROOT_PASSWORD: r00t
```

Also, you must provide a `waitforit.yml` file in order to wait until the backing service is ready to use to avoid failed build. **Note. You Jenkins should provide the [waitfor](https://github.com/maxcnunes/waitforit) utility that is used in the shared library**

```sh
backingServices:
  - name: mysql
    host: localhost --> always must be `localhost`
    port: 3306
    retries: 10
    timeout: 10
```

* **`Dockerfile.** You must provide a `Dockerfile` in the directory `docker/Dockerfile` in order to build the `docker image`

* **Jenkinsfile.** In order to use the shared library

```sh
@Library('maven-docker-pipeline')_

mavenDockerPipeline {

    setupBackingServices = [
        'docker-compose -f docker-compose-backing-services.yml up -d' // docker-compose defined before with the backing services
    ]

    build = [
        'mvn clean install' // commands to use in the build stage
    ]

    cleanup = [
        'docker-compose -f docker-compose-backing-services.yml down' // remove all backing service docker containers created before
    ]

}
``` 
