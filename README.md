# docker-java

Java API client for [Docker](http://docs.docker.io/ "Docker")

Supports a subset of the Docker Client API v1.11, Docker Server version 0.11

Developer forum for [docker-java](https://groups.google.com/forum/?hl=de#!forum/docker-java-dev "docker-java")

## Build with Maven

###### Prerequisites:

* Java 1.6+
* Maven 3.0.5
* Docker daemon running

Maven may run tests during build process but tests are disabled by default. The tests are using a localhost instance of Docker, make sure that you have Docker running for tests to work. To run the tests you have to provide your https://www.docker.io/account/login/ information:

    $ mvn clean install -DskipTests=false -Ddocker.io.username=... -Ddocker.io.password=... -Ddocker.io.email=...

Make sure you have a public repository named "busybox".

By default Docker server is using UNIX sockets for communication with the Docker client, however docker-java
client uses TCP/IP to connect to the Docker server, so you will need to make sure that your Docker server is
listening on TCP port. To allow Docker server to use TCP add the following line to /etc/default/docker

    DOCKER_OPTS="-H tcp://127.0.0.1:2375 -H unix:///var/run/docker.sock"

More details setting up docket server can be found in official documentation: http://docs.docker.io/en/latest/use/basics/

Now make sure that docker is up:

    $ docker -H tcp://127.0.0.1:2375 version

    Client version: 0.8.1
    Go version (client): go1.2
    Git commit (client): a1598d1
    Server version: 0.8.1
    Git commit (server): a1598d1
    Go version (server): go1.2
    Last stable version: 0.8.1

Run build with tests:

    $ mvn clean install

## Docker-Java maven dependency:

    <dependency>
          <groupId>com.kpelykh</groupId>
          <artifactId>docker-java</artifactId>
          <version>0.8.1</version>
    </dependency>


## Example code snippets:

    DockerClient dockerClient = new DockerClient("http://localhost:2375");

###### Get Docker info:

    Info info = dockerClient.info();
    System.out.print(info);

###### Search Docker repository:

    List<SearchItem> dockerSearch = dockerClient.search("busybox");
    System.out.println("Search returned" + dockerSearch.toString());

###### Create new Docker container, wait for its start and stop it:

    ContainerConfig containerConfig = new ContainerConfig();
    containerConfig.setImage("busybox");
    containerConfig.setCmd(new String[] {"touch", "/test"});
    ContainerCreateResponse container = dockerClient.createContainer(containerConfig);

    dockerClient.startContainer(container.id);

    dockerClient.waitContainer(container.id);

    dockerClient.stopContainer(container.id);


##### Support for UNIX sockets:

    Support for UNIX socket should appear in docker-java pretty soon. I'm working on its integration.

##### Docker Builder:

To use Docker Builder, as described on page http://docs.docker.io/en/latest/use/builder/,
user dockerClient.build(baseDir), where baseDir is a path to folder containing Dockerfile.


    File baseDir = new File("~/kpelykh/docker/netcat");

    ClientResponse response = dockerClient.build(baseDir);

    StringWriter logwriter = new StringWriter();

    try {
        LineIterator itr = IOUtils.lineIterator(response.getEntityInputStream(), "UTF-8");
        while (itr.hasNext()) {
            String line = itr.next();
            logwriter.write(line);
            LOG.info(line);
        }
    } finally {
        IOUtils.closeQuietly(response.getEntityInputStream());
    }



For additional examples, please look at [DockerClientTest.java](https://github.com/kpelykh/docker-java/blob/master/src/test/java/com/kpelykh/docker/client/test/DockerClientTest.java "DockerClientTest.java")

## Configuration

There are a couple of configuration items, all of which have sensible defaults:

* `url` The Docker URL, e.g. `http://localhost:2375`.
* `version` The API version, e.g. `1.11`.
* `username` Your repository username (required to push containers).
* `password` Your repository password.
* `email` Your repository email.

There are three ways to configure, in descending order of precedence:

##### Programatic:
In your application, e.g.

    DockerClient docker = new DockerClient("http://localhost:2375");
    docker.setCredentials("dockeruser", "ilovedocker", "dockeruser@github.com");`

##### System Properties:
E.g.

    java -Ddocker.io.username=kpelykh pkg.Main

##### File System  
In `$HOME/.docker.io.properties`, e.g.:

    docker.io.username=dockeruser

##### Class Path
In the class path at `/docker.io.properties`, e.g.:

    docker.io.url=http://localhost:2375
    docker.io.version=1.11

Change Log
---
0.8.15

* Updated to use port 2375 by default.

Contributors
---
* jacobbay
