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

Starting one Spring Boot app that scans all other Spring Boot modules pose a module isolation problem,
for all beans and `AutoConfiguration` clases will start and live in the same `SpringApplicationContext`.

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

## Caveats
