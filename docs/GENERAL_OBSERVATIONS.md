# Stuff that I've noticed that might end up elsewhere eventually

## Coroutines

***verify this***

Using a blocking endpoint, on app startup seeing lots of `[er @coroutine#2]`, `[-1 @coroutine#3]`, `[-1 @coroutine#4]`, etc which looks like spring recognizes kotlin and uses coroutines where possible.

When running the tests, the threadid for the log statements in the rep were all the same id.  Startup of springboot showed coroutines with the `er` prefix. vs the `-1`