# Project Creation Using initializer observations

* I used latest stable spring boot (2.7.0).  There is a m3 release of 3.0.0
* I couldn't select java and kotlin.  I'm pretty sure I will still be able to get both tho.
* I selected a bunch of options that I may or may not need.
* There is an option for okta support!
* There is no option (afaics) of dynamodb support.  I selected mongo/reactive mongo for now
* There are a lot of monitoring built in options, including datadog
* The kotlin version defaulted to kotlin 1.6 (1.7 just went stable).  Sticking with this atm.
* There was no option that I saw to select `kotest` or even `spock`. The default testing appears to be junit 5.
* No OOB support for AWS.  There is support for GCP and azure.
* I skipped all the cloud support options for now.  This is things for hystrix, external configuration management, service discovery, routing, etc
* some options were not selectable, due to using spring boot 2.7 (several options require < 2.7m1)
* I didn't see an option for swagger/swaggerUI/openAPI

## Some neat things
1. I selected the option for 'test-containers'
2. I selected the [spring rest docs](https://spring.io/projects/spring-restdocs), which allows creating some documentation from comments in code.  It shows up as a test option I want to see what it is 
3. Using mongo and reactive mongo for now as that is supported OOB.  Will look into ddb at a later time.
4. There is a new (to me) gradle dependency scope called `developmentOnly`

## Issues
### Dependency order
I juggled the dependency order in the [gradle build file](../build.gradle.kts) to be alpha sorted due to it being my weird ocd thing.  I also added some white space breaks.

### Initial project issues after creation of project via initilizer
These appear to be related to each other, and are an inssue since I selected the `spring rest docs` testing option in the initilizer.  There is a [github issue](https://github.com/spring-io/initializr/issues/922) for this.  Note there are a couple of recommendations on how to fix it in that issue

#### Unresolved reference snippetsDir
The new project had a snippets dir defined in the build.gradle.kts.  It broke the initial build.  Apparently this is a known issue dating back to 2019, not sure why it isn't fixed.

I manually created the dir that it was specifying, it didn't fix it.  Changed it according to the github issue

#### Unresolved task
After fixing that I had to change a line in the tasks.asciiDoctor from

    dependsOn(test)

to

    dependsOn(tasks.test)

## Testing
There was no apparent option to select `spock` or `kotest` (or any other testing framework).  The only testing options were things like the `rest docs` and the `test-containers`.  The initilizer added some junit 5 stuff, but I don't know if that is the default behavior or if it was due to me adding those test dependencies I mentioned previously.

I found a couple of links for adding and using kotest, including  [kotest docs](https://kotest.io/docs/extensions/spring.html) and [this](https://dev.to/kotest/testing-a-spring-boot-application-with-kotlintest-pgd)
