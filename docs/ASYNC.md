# Async/Non Blocking IO (Not Reactive)

Enabling async/nonblocking io is pretty straight forward in springboot.  In the springboot app class add the `@EnableAsync` annotation.  This does some magic (like create a default worker threadpool) but doesn't make anything async.

You can add a `@Async` annotation to an endpoint or probably a service method and it will magically get moved to the worker thread pool.  One thing I noticed is I needed to have the method/function I annotate with the `@Async` return a CompletableFuture<T>, otherwise the call to the endpoint never receives a response, which makes sense because the work is being allocated to the worker thread.  I ***think*** that the `@Async` deals with the block.  

I added a filter to confirm something I read about it being on the request thread, and it is.  When I ran it calling an async endpoint, I see:

```
[nio-8088-exec-1] c.d.s.springdemo.async.FilterLogger      : start of servlet filter
[nio-8088-exec-1] c.d.s.springdemo.async.FilterLogger      : filter awake now
[         task-1] c.d.s.springdemo.async.AsyncController   : in GET async
[         task-1] c.d.s.springdemo.async.AsyncController   : after sleep
```
I will look into micronaut to see if it uses servlet filters, and if so how they behave.

When I call the blocking endpoint without the @async.  Note everything is handled by the requestThread

```
[nio-8088-exec-7] c.d.s.springdemo.async.FilterLogger      : start of servlet filter
[nio-8088-exec-7] c.d.s.springdemo.async.FilterLogger      : filter awake now
[nio-8088-exec-7] c.d.s.springdemo.async.AsyncController   : in GET NotAsync
[nio-8088-exec-7] c.d.s.springdemo.async.AsyncController   : after sleep
```
# Async vs Non Async
Using default settings (ie whatever threadpools that spring will used by default) for the spring boot config, I created a spring boot application that has `@EnableAsycn` and two endpoints, one of which is `@Async` and the other is normal blocking IO.

I used the apache server bench tool `ab` to simulate 100 concurrant clients, making a total of 800 requests.

    ab -n 800 -c 100 localhost:8088/async

[Check here for the results and thoughts](./ASYNC_VS_NONASYNC.md)

# Coroutines
`/suspendable` endpoint implements this

I was able to do a suspendable endpoint by adding `suspend` to the controller endpoint and put a delay in it.  this appears to offload it to a coroutine.  I'm not sure which scope is used, I need to experiment more with this.  The endpoint

I did this experiment before finding the [spring doc on coroutines](https://docs.spring.io/spring-framework/docs/5.2.0.RELEASE/spring-framework-reference/languages.html#how-reactive-translates-to-coroutines).  Note the first log statement is on the request thread, after the delay it is on a different thread.

```text
2022-06-30 22:13:43.835  INFO 47956 --- [nio-8088-exec-5] c.d.s.springdemo.async.FilterLogger      : start of servlet filter
2022-06-30 22:13:44.036  INFO 47956 --- [nio-8088-exec-5] c.d.s.springdemo.async.FilterLogger      : filter awake now
2022-06-30 22:13:44.036  INFO 47956 --- [nio-8088-exec-5] c.d.s.springdemo.async.AsyncController   : in GET coroutine
2022-06-30 22:13:44.549  INFO 47956 --- [DefaultExecutor] c.d.s.springdemo.async.AsyncController   : after delay
```
Note the 'DefaultExecutor' thread which is the suspendable function _after_ the delay in the controller

I need to understand scopes better, as there are rules on if a child suspendable function breaks/throws an exception and how it affects parents/etc, ie structured concurrency

## Specifying  GlobalScope on an end point
`suspendableScoped` endpoint specifies a suspendable function that uses the GlobalScope scope.  Holy crap this bencmarks _**FAST**_ handling 363 requests per second!

Notice the calls _start_ in a differnt thread pool vs just using the `susppend` on a function (the first log was the request thread, the 2nd after the delay was on a different thread)

From what I read using the GlobalScope is a limited use case as it has some risks [see the async coroutines doc](ASYNC_COROUTINES.md)

```text
2022-07-04 23:01:04.811  INFO 42160 --- [atcher-worker-1] c.d.s.springdemo.async.AsyncController   : in GlobalScope.async handler
2022-07-04 23:01:04.839  INFO 42160 --- [atcher-worker-1] c.d.s.springdemo.async.AsyncController   : after delay
```

## Using a custom scope
I created a custom scope using the Dispatchers.IO pool, default configs, and had very similar performances to the GlobalScoped endpoint, which implies they have similar configs.  The advantage of using the custom scope is we now can use structured concurrency.   Here's a log entry

```text
2022-07-12 22:23:40.129  INFO 35564 --- [tcher-worker-57] c.d.s.springdemo.async.AsyncController   : after delay
```
# Some future experiments
1. annotate a service method with `@Async` to see if I have to have the caller block? - **NOPE**.

# Outstanding Questions
1. Since NIO is managing the incoming connections, and a blocking client calling a non-blocking endpoint, how does this impact the endpoint server?  The call is 'shelved' while the event loop is doing other things, but there is a limit on socket connections and such.
2. Configuring and tuning a thread pool.  I found if the `ThreadPoolTaskExecutor` is configured with a queue size close to (TODO figure out percentage, I did an experiment with the queue size 1/3 of the max pool and it was slow) or greater than the max pool size, requests get shuttled to the queue and no new task/worker threads get spun up.  Setting the queue size much lower causes it to spin new threads up.  What are the implications?
3. How do I find the values of a running springboot instance for the threadpools?  According to the spring API docs for `ThreadPoolTaskExecutor` I should be able to see it (and tweek values) in JMX.  I did not find it in JConsole.
4. Coroutines.  I think these will be far more performant than threads based on reading and dabbling.

# Reading Material
https://www.quinbay.com/blog/working-with-threadpooltaskexecutor-of-spring#:~:text=One%20of%20the%20added%20Advantage,is%20roughly%20equivalent%20to%20Executors.

There is some talk about the when using the TaskExecutor manually your task is added to the queue and it's internal rules determine when it gets a thread.  #33.2
https://docs.spring.io/spring-framework/docs/4.2.x/spring-framework-reference/html/scheduling.html

Async with kotlin/coroutines.  also has examples of using a `router` vs a `@Controller`.  Looks like it assumes reactive tho, not just non-blocking/async
https://foojay.io/today/build-and-test-non-blocking-web-applications-with-spring-webflux-kotlin-and-coroutines/

This one may be worth looking at more.  It seems to have a lot of extra coroutine (ie continuations) but worth looking at
https://resources.jetbrains.com/storage/products/kotlinconf2017/slides/AsynchronousProgramming.pdf
