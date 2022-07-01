#
## Async but not reactive

DONE - figure out why in the [Async vs non-async perf test](./ASYNC_VS_NONASYNC.md) that the thread pool is not scaling up when I hammer the async endpoint.  

Added a custom thread pool and it scales, with some notes.

Do the same tests in micronaut.

## Using Coroutines with springboot
I added a `suspend` to an endpoint def and it seems to work (look at the [Async doc in the coroutines section](./ASYNC.md))
1. Define scopes for endpoints that are suspendable vs default (what is it?)
2. structured concurrency?