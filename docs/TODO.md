#
## Async but not reactive

DONE - figure out why in the [Async vs non-async perf test](./ASYNC_VS_NONASYNC.md) that the thread pool is not scaling up when I hammer the async endpoint.  

Added a custom thread pool and it scales, with some notes.

Do the same tests in micronaut.

I 