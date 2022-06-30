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

# Some future experiments
1. annotate a service method with `@Async` to see if I have to have the caller block? - NOPE.

# Outstanding Questions
1. Since NIO is managing the incoming connections, and a blocking client calling a non-blocking endpoint, how does this impact the endpoint server?  The call is 'shelved' while the event loop is doing other things, but there is a limit on socket connections and such.
2. Configuring and tuning a thread pool.  I found if the `ThreadPoolTaskExecutor` is configured with a queue size close to (TODO figure out percentage, I did an experiment with the queue size 1/3 of the max pool and it was slow) or greater than the max pool size, requests get shuttled to the queue and no new task/worker threads get spun up.  Setting the queue size much lower causes it to spin new threads up.  What are the implications?
3. How do I find the values of a running springboot instance for the threadpools?  According to the spring API docs for `ThreadPoolTaskExecutor` I should be able to see it (and tweek values) in JMX.  I did not find it in JConsole.
4. Coroutines.  I think these will be far more performant than threads based on reading and dabbling.