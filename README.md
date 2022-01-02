# Demo on how to integrate diverse Spring Boot apps with OSGi and Docker

# Intro

I've been developing a **Maven** _multi-module_ **Spring Boot** app for a while now.

Trying keep these modules independent posed some challenges,
which led me to search for a new way to approach the current architecture.

This README shows **problems** with the current scenario, a plausible **solution**, and its **caveats**.

# Current scenario: Advantages and Problems

Each module has its own set of `@Controller`, `@Service`, and JPA `Repository` classes, 
and access its own database _schema_.

Also, each _Module_ has its own `@SpringBootApplication`.

It strives to be a kind of **Modulith** scenario.
However, as we'll see, it presents some old style **Monolith** problems.

## Advantages

The main advantage is **simplicity**. This _multi-module_ can be thought of 
just a bunch of Spring Boot apps.

Any of these can be started with a single click on "Run As -> Java Application".

- Each app exposes HTTP endpoints via its own `@Controllers`.
- Each app takes care of it own `application.properties`.
- Each app manages its own _databse access_ and _database schema_.

Simple, right?

Problems begin when some of these modules depend on others.

## Problems

Let's take `Module_A` _depends on_ `Module_B`. 

More presicely:

```java
@Service
class Service_A {

  @Autowired
  Service_B service;
  
}
```

In order for this to work, both `Service_A` and `Service_B` need to be in the classpath.
Also, when starting the Spring app, **all** _Spring beans_ from both modules need to be scanned.

This raises the following problems:

## 1. Lack of Isolation

Starting one Spring Boot app that scans _all_ other Spring Boot modules pose a module isolation problem,
for all beans and `AutoConfiguration` classes will start and live in the same `SpringApplicationContext`.

Isolation problems manifest as:

- The module that starts the application (`Module_A`) needs to `@ComponentScan` its own beans and all other modules it depends upon.
Moreover, it needs to scan beans in all transitive modules (A -> B -> C).
- Spring bean names will eventually collide and prevent Spring from starting up.
- `application.properties` from `Module_B` will be overriden by `application.properties` in `Module_A`, which means copying properties to the outer-most module.
- If using Flyway to manage _database schemas_, some magic is needed for each module to only manage its own schema.

## 2. Slow startup time

As an obvious consequence, starting all modules at once takes longer, like a 50 seconds for the whole app, instead of 5-10 seconds for each module.

## 3. Spring Boot version interdependency

You cannot mix Spring Boot versions between modules. 
If you need to bump your Spring Boot version, you have to do it for all modules.

If you're unfamiliar with this problem, here's a taste what it could be like:

https://blog.codecentric.de/en/2021/12/migrating-spring-boot-java-17/

# (Plausible) Solution

I've been thinking about all this for a while. I've known about OSGi for many years now, but never had the chance to play around with it.
But somehow, I kept thinking it would be a good match for the aforementioned problems.

The solution I'm about to unfold really took shape when I read this excellent article by [@amussarra](https://github.com/amusarra):

[What are OSGi Remote µServices](https://techblog.smc.it/en/2020-07-31/cosa-sono-osgi-remote-services)

That's the spark I needed.

Although Antonio's solution focuses on **Remote Services**, his article plus [GitHub repository](https://docs.github.com/es/get-started/using-github/keyboard-shortcuts) provides  examples of the following:

- How to config and run Karaf on Docker
- How to code Declarative Services
- How to package an OSGi bundle

## Claims

These are this solution's claims:

1. You can run each Spring Application module separately.
2. Each Maven module can inherint from a different version of Spring Boot and still work.
3. `Service_A` can use `Service_B` seamlessly, i.e. as if it were just another lib in the classpath.

## Development environment

You're gonna need:

- Docker: https://docs.docker.com/
- Docker Compose: https://docs.docker.com/compose/
- Eclipse (if you want to make changes): https://www.eclipse.org/
- Maven: https://maven.apache.org/


## Moduliths

This is this Demo's folder structure:

`tree -L 2 -P 'pom.xml' docker-osgi-spring-boot-demo`

```
docker-osgi-spring-boot-demo
├── pom.xml
├── spring.boot.demo.one
│   ├── pom.xml
│   ├── src
│   └── target
├── spring.boot.demo.one.service.api
│   ├── pom.xml
│   ├── src
│   └── target
└── spring.boot.demo.two
    ├── pom.xml
    ├── src
    └── target
```

There are 3 Maven modules / OSGi bundles:

- `spring.boot.demo.one.service.api`: It contains just a Java interface, `DemoOneService`.
- `spring.boot.demo.one`: A Spring Boot **2.5.8** app which _implements_ `DemoOneService` and accesses a H2 database via a JPA `Repository`.
- `spring.boot.demo.two`: Another Spring Boot **2.6.2** app, which _uses_ `DemoOneService` from module `spring.boot.demo.one`.

## Compile

1. Head to root folder and exec:

```
mvn install
```

Should yield:

```console
[INFO] Reactor Summary for spring.boot.demo.parent 0.0.1-SNAPSHOT:
[INFO] 
[INFO] spring.boot.demo.one.service.api ................... SUCCESS [  0.786 s]
[INFO] spring.boot.demo.one ............................... SUCCESS [ 13.236 s]
[INFO] spring.boot.demo.two ............................... SUCCESS [  8.548 s]
[INFO] spring.boot.demo.parent ............................ SUCCESS [  0.039 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  23.220 s
[INFO] Finished at: 2022-01-02T16:34:45+01:00
[INFO] ------------------------------------------------------------------------
```


## Run

1. Head to `compose` folder and exec: 

    ```
    docker-compose up -d
    ```

2. Connect to karaf (password is "karaf"):

    ```
    ssh -p 8101 karaf@localhost
    ```
    
    ```console
        __ __                  ____      
       / //_/____ __________ _/ __/      
      / ,<  / __ `/ ___/ __ `/ /_        
     / /| |/ /_/ / /  / /_/ / __/        
    /_/ |_|\__,_/_/   \__,_/_/         

    Apache Karaf (4.3.1)

    Hit '<tab>' for a list of available commands
    and '[cmd] --help' for help on a specific command.
    Hit 'system:shutdown' to shutdown Karaf.
    Hit '<ctrl-d>' or type 'logout' to disconnect shell from current session.

    karaf@root()>                                                                                                                     
    ```
  
3. Install `bundles`:

    ```
    karaf@root()> install mvn:org.example.osgi/spring.boot.demo.one.service.api/0.0.1-SNAPSHOT
    Bundle ID: 64
    karaf@root()> install mvn:org.example.osgi/spring.boot.demo.one/0.0.1-SNAPSHOT                                        
    Bundle ID: 65
    karaf@root()> install mvn:org.example.osgi/spring.boot.demo.two/0.0.1-SNAPSHOT                                                    
    Bundle ID: 66
    ```
    
4. List all `bundles`:

    ```
    karaf@root()> list
    START LEVEL 100 , List Threshold: 50
    ID │ State     │ Lvl │ Version        │ Name
    ───┼───────────┼─────┼────────────────┼──────────────────────────────────────────────────────────────────────────────────────────
    24 │ Active    │  80 │ 1.14.0         │ Aries Remote Service Admin Core
    25 │ Active    │  80 │ 1.14.0         │ Aries Remote Service Admin Discovery Gogo Commands
    26 │ Active    │  80 │ 1.14.0         │ Aries Remote Service Admin Discovery Local
    27 │ Active    │  80 │ 1.14.0         │ Aries Remote Service Admin Discovery Zookeeper
    28 │ Active    │  80 │ 1.14.0         │ Aries Remote Service Admin Event Publisher
    29 │ Active    │  80 │ 1.14.0         │ Aries Remote Service Admin provider TCP
    30 │ Active    │  80 │ 1.14.0         │ Aries Remote Service Admin SPI
    31 │ Active    │  80 │ 1.14.0         │ Aries Remote Service Admin Topology Manager
    34 │ Active    │  80 │ 3.4.14         │ ZooKeeper Bundle
    40 │ Active    │  80 │ 4.3.1          │ Apache Karaf :: OSGi Services :: Event
    64 │ Installed │  80 │ 0.0.1.SNAPSHOT │ spring.boot.demo.one.service.api
    65 │ Installed │  80 │ 0.0.1.SNAPSHOT │ spring.boot.demo.one
    66 │ Installed │  80 │ 0.0.1.SNAPSHOT │ spring.boot.demo.two
    ```
    
    Notice bundles 64, 65, and 66. Those are our bundles!
    
4. Start `bundles` 64, 65, and 66 (⚠️ Use whichever number was printed in previous list):

    ```
    start 64 
    start 65
    start 66
    ```
    
    You should be watching Spring Boot log unfold.
    
5. Watch for changes in `-SNAPSHOT` `bundles`:

    ```
    bundle:watch *
    ```
    
6. Exit Karaf: Hit `CTRL+D`

## Test

1. Find out `karaf-instance-1`'s IP address:

    `docker inspect osgi_karaf-instance-1_1 | grep IPA`
    
    Should print:
    
    ```
    "SecondaryIPAddresses": null,
    "IPAddress": "",
            "IPAMConfig": null,
            "IPAddress": "192.168.128.3",
    ```

2. Try http://192.168.128.3:8080/service. That's DemoOneController calling `count()` on DemoOneService implemented on this very module.
3. Try http://192.168.128.3:8081/service. That's DemoTwoController calling `count()` on DemoOneService as well!

## Caveats

As usual, this solution is not free from inconveniences.

Hare are some:

- Feedback cycle: You need to `mvn install` for every change, instead of just hitting `CTRL+S` on the editor and `F5` on the browser.
- Debug: You can't do "Debug As -> Java application" just as easily. You need to setup `JAVA_TOOL_OPTS` and do "Remote Debug".
- Gateway: All-in-one Spring Boot app gives you one port 8080 to access all endpoints. With this approach (as is right now), you get 8080, 8081, etc. You need a gateway/router.
