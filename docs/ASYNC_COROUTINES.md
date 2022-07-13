# Coroutines and Spring async
Springboot has a good kotlin integration.  
https://docs.spring.io/spring-framework/docs/5.2.0.RELEASE/spring-framework-reference/languages.html#controllers

 
[This article](https://betterprogramming.pub/how-to-fire-and-forget-kotlin-coroutines-in-spring-boot-40f8204aac86) has some good examples.

# Scopes and structured concurrency
This section will evolve as I learn more on coroutines.

I found [this article very helpful](https://elizarov.medium.com/explicit-concurrency-67a8e8fd9b25)

Parent Coroutine
    -> coroutine 1
    -> coroutine 2
    ...
    -> coroutine X

The couroutines are running async, and one throws an exception.  There are 2 thing that can happen

a)  The Parent coroutine shuts down all the children.  this (i think) is part of structured concurrency.  Note that there can be a default exception handler added to things which could retry a failed coroutine.

b) the other coroutines keep running, even tho the parent is shut down.  I _think_ coroutines executed in `GlobalScope` or `SupervisorScope` behave this way, but I'm not quite clear of the difference yet.  I've seen GlobalScope referred to as daemons.  There is risks in using GlobalScope, ie leaks.  There are reasons to use it, but it seems rare.

https://elizarov.medium.com/the-reason-to-avoid-globalscope-835337445abc

There appear to be several types of Default scopes.
* GlobalScope
* SupervisorScope https://kt.academy/article/cc-constructing-scope (look for 'Constructing a scope for additional calls, suspend scope is intended for if you only intend to use `suspend`)
??

There also is the notion of dispatchers, of which there are several like Dispatchers.DEFAULT, Dispatchers.IO (for IO things), Dispatchers.MAIN (android UI, makes testing easier for android), and others

# Several ways to implement non-blocking io using coroutines
## `suspend` endpoint
Simply making the endpoint function suspendable, by adding the `suspend` keyword gets you a non-blocking (not reactive) endpoint.  I don't know which scope it is using.  When you hit the endpoint you will see the thread switch (after the delay is invoked)

```text
2022-06-30 22:13:44.036  INFO 47956 --- [nio-8088-exec-5] c.d.s.springdemo.async.AsyncController   : in GET coroutine
2022-06-30 22:13:44.549  INFO 47956 --- [DefaultExecutor] c.d.s.springdemo.async.AsyncController   : after delay
```
Hitting it with  ` ab -n 800 -c 100 localhost:8088/suspendable` resulted in about 116 requests per second.  See [more detailed results in ASYNC.md](./ASYNC.md).  I suspect the default-executor has a small (maybe 1 if it is like the `@Async` stuff).

## Suspend using GlobalScope
You can specify a Scope for the coroutines to run in.  Most of the examples show the use of `GlobalScope` which has some implications due to it not supporting Structured Concurrancy (more on that later).  An example of this would be a coroutine/endpoint that makes two conrrurant calls to different services.  One take a long time, the 2nd throws an exception.  The first call will still get processed while the endpoint errors out.

When hitting it with Hitting it with  `ab -n 800 -c 100 localhost:8088/suspendableGlobalScope` I get close to 300 requests per second!

[This is a good basic overview of why not to use GlobalScope](https://betterprogramming.pub/how-to-fire-and-forget-kotlin-coroutines-in-spring-boot-40f8204aac86).  Here are a couple others
* https://elizarov.medium.com/the-reason-to-avoid-globalscope-835337445abc
* https://elizarov.medium.com/structured-concurrency-722d765aa952
* 

##  Custom Scope using Dispatchers.IO
I created a custom context scoped to the Dispatchers.IO, which has a default thread pool of TBD config.  will experiment.  This was very fast and using it gives us structured concurrency.

I don't know the threadpool size assigned to the Dispatchers.IO, but when I ran the same `ab -n 800 -c 100 localhost:8088/suspendableIoScoped`` test it _SMOKED_, processing about 341 requests per second.

## Suspend with a custom scope