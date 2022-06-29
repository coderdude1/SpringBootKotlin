# Hmmm
why does the blocking outperform the non-blocking?  worker thread pool size?  That didn't work.
# Async vs non-async endpoint performance tests
I ran the apache benchmark tool several times to warm up the jvm.  I used the same command (different endpoint) to test things

This command does 800 requests with a concurrency of 100

    ab -n 800 -c 100 localhost:8088/async

## Async Endpoint
Hitting a springboot app with @EnableAsync.  This endpoint is @Async.  

Using Jconsole on the service, the max thread count of the server was 134, it was a mix of NIO (request) and worker threads.  The majority by a long shot were request threads, there were roughly 8-10`task-xx` threads, and a ton of `http-nio-8088-blah` threads.  I think that the requests threads block when they can't pass the work to the task threads, but the pool for the requests threads is *FAR* bigger than the task threads.

At the
```
Document Path:          /async
Document Length:        43 bytes
Concurrency Level:      100
Time taken for tests:   51.200 seconds
Complete requests:      800
Failed requests:        0
Total transferred:      140800 bytes
HTML transferred:       34400 bytes
Requests per second:    15.63 [#/sec] (mean)
Time per request:       6399.993 [ms] (mean)
Time per request:       64.000 [ms] (mean, across all concurrent requests)
Transfer rate:          2.69 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    0   0.5      0       2
Processing:   728 5939 1124.1   6065    6830
Waiting:      714 5938 1124.7   6063    6819
Total:        729 5940 1124.1   6066    6830

Percentage of the requests served within a certain time (ms)
  50%   6066
  66%   6503
  75%   6508
  80%   6510
  90%   6536
  95%   6556
  98%   6604
  99%   6614
 100%   6830 (longest request)
```

## non-async
Thread counts maxed out at 123, all http-nio request threads.  This was FAR faster than the async for what I think is the default thread pool having a small max thread cap

```
Document Path:          /not_async
Document Length:        4 bytes

Concurrency Level:      100
Time taken for tests:   6.453 seconds
Complete requests:      800
Failed requests:        0
Total transferred:      108800 bytes
HTML transferred:       3200 bytes
Requests per second:    123.98 [#/sec] (mean)
Time per request:       806.600 [ms] (mean)
Time per request:       8.066 [ms] (mean, across all concurrent requests)
Transfer rate:          16.47 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    0   0.5      0       2
Processing:   701  709  10.4    704     754
Waiting:      701  707   8.2    704     735
Total:        701  710  10.4    705     755

Percentage of the requests served within a certain time (ms)
  50%    705
  66%    706
  75%    714
  80%    717
  90%    731
  95%    734
  98%    736
  99%    736
 100%    755 (longest request)
```

## Attempt to address the poor async perf
### Create specific threadppol with a decent limit for max threads
https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/EnableAsync.html

The default thread pool is pretty simple, it doesn't reuse threads, and the docs claim an unlimited max limit but experiments don't match that.

I added a new thread pool for the @Async (described in the above link).  I named it `dood-thread` so I was able to tell if it was using it.  I set the max threads to 150.  I did see about 8-10 threads prefixed with the `dood-thread`, so it was using it but it was *not* scaling the threads in the pool to a higher number.  Hmmmm

I did notice some of the http-nio-exec threads were in a `TIMED_WAITING` state so I think that confirms my thought that the `task` thread pool is not scaling up enough.

Apache bench output

```
Document Path:          /async
Document Length:        43 bytes

Concurrency Level:      100
Time taken for tests:   58.629 seconds
Complete requests:      800
Failed requests:        0
Total transferred:      140800 bytes
HTML transferred:       34400 bytes
Requests per second:    13.65 [#/sec] (mean)
Time per request:       7328.602 [ms] (mean)
Time per request:       73.286 [ms] (mean, across all concurrent requests)
Transfer rate:          2.35 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    1   0.5      1       2
Processing:   703 6769 1293.1   7027    7795
Waiting:      703 6768 1293.4   7027    7792
Total:        704 6769 1293.1   7028    7795

Percentage of the requests served within a certain time (ms)
  50%   7028
  66%   7044
  75%   7474
  80%   7482
  90%   7485
  95%   7487
  98%   7551
  99%   7581
 100%   7795 (longest request)
```

The max thread count actually observed was the same as the min thread count in the pool.  I bumped the min count to 25 and saw a big improvement.  I don't know why the threadpool isn't trying to scale up.  I saw a LOT of blocked request threads, just less because we had more worker threads to offload the processing to do the work.

```text
Document Path:          /async
Document Length:        43 bytes

Concurrency Level:      100
Time taken for tests:   17.002 seconds
Complete requests:      800
Failed requests:        0
Total transferred:      140800 bytes
HTML transferred:       34400 bytes
Requests per second:    47.05 [#/sec] (mean)
Time per request:       2125.291 [ms] (mean)
Time per request:       21.253 [ms] (mean, across all concurrent requests)
Transfer rate:          8.09 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    0   0.5      0       2
Processing:   702 1934 269.5   2003    2218
Waiting:      702 1933 270.0   2002    2213
Total:        703 1934 269.5   2003    2218

Percentage of the requests served within a certain time (ms)
  50%   2003
  66%   2004
  75%   2005
  80%   2006
  90%   2010
  95%   2019
  98%   2200
  99%   2205
 100%   2218 (longest request)
```
## TODO - Move the @Async from the Controller to a service
to do this I will have to create a service with a method annotated with the @Async, and inject it into the controller.  I don't think this will work as I saw example code where people put the @Async annotation on the endpoint, but you never know.