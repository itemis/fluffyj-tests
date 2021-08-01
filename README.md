# Fluffy J Tests
Helper code that aims at making test code easier to comprehend. Also extra fluffy ❤

This is not a replacement for the popular [AssertJ](https://assertj.github.io/doc/) or any of the other popular test helper libraries. However, I like my (unit) test code to feature a certain style and sometimes, it is hard to express things with the usual libraries without cluttering the test code with boilerplate. Thus, I developed a couple of helpers over the years which I think may also be useful for other people so I decided to provide this little library.

# Build
`mvn clean install`

# FluffyTestHelper
Collection of recurring asserts that are cumbersome to implement and thus are often skipped. Shall help not to avoid 'easy' tests that are 'not worth' to implement.

## Asserting on Implementation Basics
Everyone needs them, no-one likes to implement them, tests on them are always missing.
  
* Is class declared final? -> assertFinal
* Does class have a [serialVersionUid](https://stackoverflow.com/q/285793)? -> assertSerialVersionUid
* Does a method properly handle null args? -> assertNullArgNotAccepted
* I want my test to sleep but always mix up milliseconds and do not want to care about any exception -> sleep
* Make sure a class is not instantiatable -> assertNotInstantiatable

### Note on sleep
First of all, sleeping in tests is always a smell. It indicates that implementors may have failed on providing proper means of synchronization (e. g. callbacks) or test implementors didn't know how to use them (or didn't care). The result is often a test that sleeps for too long (i. e. increases round trip times beyond reasonable levels) or sleeps for too short a time which results in flickering tests (pass most of the time but fail if the tested code happens to take just a little more time).
  
However, especially when dealing with legacy code, it may still be useful to let the test sleep. Two problems with this:
* Thread.sleep requires handling of InterruptedException (as well does TimeUnit.sleep).
* Thread.sleep requires a value specified in milliseconds, which either is misinterpreted by implementors (e. g. 'I want it to sleep 5s -> Thread.sleep(5)) and is also often a [Magic Number](https://stackoverflow.com/q/47882).

`FluffyTestHelper.sleep` does not require exception handling (but still takes proper care of interrupts) and makes the magic number situation a bit better by using `Duration` instead of a `long` value. So instead of doing this:
  
```
@Test
public void testSomething() {
  try {
      Thread.sleep(5000);
  } catch (InterruptedException e) {
      // doing nothing here makes things even worse
  }  
}
```
Or this

```
// Oh c'mon it still is ugly to be required to specify throws here
@Test
public void testSomething() throws InterruptedException {
  Thread.sleep(5000);
}
```
You may just do this:

```
@Test
public void testSomething() {
    FluffyTestHelper.sleep(Duration.ofSeconds(5));
}
```
  
# Proper Handling of SystemProperties During Tests
When testing code that relies on SystemProperties, it is often a pain to restore the properties to the values they had before the test was started. This is very important, because otherwise, subsequent tests may be influenced by left overs of an earlier test giving false positives or negatives. Cleanup becomes even harder in situations where test code throws exceptions, i. e. not reaching normal cleanup procedure at the end of tests.
  
With `FluffyTestSystemProperties` this cleanup is properly taken care of. It is a JUnit5 extension that works like this:

```
public class SomeTest {
    // This makes sure, SystemProperties are recorded before each test and restored after each test.
    @RegisterExtension
    FluffyTestSystemProperties fluffyProps = new FluffyTestSystemProperties();
    
    @Test
    public void testSomething() {
        System.setProperty("myProp", "myPropValue");
        someApi.someCode();
        assert..
    }
}
```

# Chaining Mockito Answers
Sometimes when writing test code, it may be required to write a rather large amount of mock behavior code. In the world of the very popular [Mockito](https://github.com/mockito/mockito) framework, this is done with objects called `Answers`.  
  
Unfortunately when working with answers (and mocking that is) the test code tends to become more complicated thus obfuscating the actual test scenario. Chained answers where created to make the code easier to comprehend, so that the scenario (especially the `given` and `when` parts) becomes more clear to the reader.  
  
It works like this:

```
//instead of writing this
when(mock.method()).thenAnswer(
    invocation -> {
        argumentStore = invocation.getArgumentAt(0, Type.class);
        someSideEffect.run();
        throw Exception("Expected");
        return null;
    }
);

// you could write
when (mock.method()).thenAnswer(
    execute(() -> someSideEffect.run())
    .andThen(exceptionalAnswer(new Exception("Expected")))
    .andThen(somethingElse())
);
```

Think of it as the Mockito kind of function composition. This is done by the help of the interface `com.itemis.fluffyj.tests.ChainedMockitoAnswer` which is a Mockito `Answer` but also provides `andThen`.  
  
The more implementations of `ChainedMockitoAnswer` exist, the stronger the effect becomes. There could also be answers for common mock behavior like sleeping, storing current time, counting down latches etc.

# Concurrency Boons  
## Asserting on Thread Safety
One of the hardest things to test is that a piece of code is thread safe. Usually a test would start multiple threads which concurrently access the test subject, e. g.:

```
// Pseudo code
@Test
public void testThreadSafety() {
    var threadRunner = initRunner
    
    var threads = listOfThreads
    for (1 to 10) {
        listOfThreads.add(threadRunner.run(() -> underTest.lazyInitThing()));
    }
   
    makeSureAllThreadsStartAtSameTime();
    
    for (listOfThreads) {
        assertThat(currentThread.result).isSameAsThose(ofOtherThreads);
    }
}
```

Usually if the test subject is not thread safe, one of the threads encounters a different result than the others. The more threads, the more likely it becomes to find those problems but naturally this cannot be made 100% sure. 
  
Implementing such a scenario in a language like Java tends to become cumbersome and obfuscates the test scenario which is especially true for making sure that all threads start at the same time and when it comes to wait for all threads to finish. Luckily, FluffyJ provides a JUnit5 based extension to abstract away the thread management boiler plate:  

```
private List things = new CopyOnWriteArrayList();

@AssertThreadSafety(threadCount = 100)
@Test
public void testThreadSafety() {
    
    var currentThing = underTest.lazyInitThing();
    things.add(currentThing);
    
    assertTrue(things).allMatch(thing -> thing == currentThing);
}
```

Please note that you still need to take care of storing the individual thread results and asserting that they do match.
  
## FluffyTestFutures
Things that may come in handy when testing code that deals with `Futures` itself or when mocking code that returns `Futures` with special behavior. Sometimes, it is very inconvenient to mock `Futures` in order to force a certain behavior, e. g. throwing an exception. In those situations you may want to use one of the following:
  
* `scheduleInterruptibleFuture` - A `Future` that is guaranteed to be interruptible by thread interrupts and is also already running as soon as it has been constructed.
* `scheduleExceptionalFuture` - A `Future` that is guaranteed to throw `ExecutionException` when client code calls `get`.
* `scheduleNeverendingFuture` - A `Future` that is guaranteed to not be interruptible by thread interrupts.
  
The `NeverEndingFuture` may be used to assert proper cleanu code behavior, i. e. does it recognize that a `Future` won't shut down and how does it behave in such a situation?

## FluffyTestLatches
Aims at making Assertions on `CountDownLatch` easy. Sometimes a test needs to wait on a latch, e. g. if legacy code does not provide proper means of synchronization. Consider the following example:

```
@Test
public void test() throws InterruptedException {
    var latch = CountDownLatch(1);
    runInSeparateThread(() -> {
        causeSomeSideEffect();
        latch.countdown();
    );
    
    // Wait until we are sure, the sideEffect happened.
    // This kind of 'manual' synchronization prevents us from using polling.
    latch.await();

    assertOnSideEffect();
}
```
  
Problems:
* We need to either take care of `InterruptedException` or declare it to be thrown in which case it is not obvious as to why the test failed.
* In case of `InterruptedException` it is a good idea to restore the thread interrupt flag in order for subsequent code to properly recognize that the thread is being interrupted.
* `await()` blocks until the latch is zero. If the latch isn't counted down, the test will block forever.
* If we where to use `await(long timeout, TimeUnit unit)` instead, we'd need to check the return value in order to make sure, the latch actually reached zero. It is also possible to mess up the time out, e. g. `await(5000, TimeUnit.SECONDS) //used to be MILLIS`
  
With `FluffyTestLatches` these points are all taken care of in the background:  

```
@Test
public void test() {
    var latch = CountDownLatch(1);
    runInSeparateThread(() -> {
        causeSomeSideEffect();
        latch.countdown();
    );
    
    // Wait until we are sure, the sideEffect happened.
    // This kind of 'manual' synchronization prevents us from using polling.
    FluffyTestLatches.assertLatch(latch, Duration.ofSeconds(2));
    
    assertOnSideEffect();
}
```
  
By doing so, the test will only continue if the latch reached zero, will wait for a maximum of 2 seconds, does respect thread interrupts, restores thread interrupt flag and will throw a meaningful exception in case waiting is interrupted.

# Asserting on Log Messages
In some situations it is important to make sure that a message is logged. When using [SLF4J](http://www.slf4j.org) you may use the `FluffyTestAppender` to assert on log messages like this:

```
public class TestSomething {
    @RegisterExtension
    FluffyTestAppender logAssert = new FluffyTestAppender();
    
    @Test
    public void testSomething() {
        someApi.call();
        
        logAssert.assertLogContains(Level.WARN,"this is a warning");
    }
}
```
  
The current implementation requires [http://logback.qos.ch](http://logback.qos.ch), i. e. you may want to include

```
<!-- https://mvnrepository.com/artifact/ch.qos.logback/logback-classic -->
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>${logback.version}</version>
    <scope>test</scope>
</dependency>
```
  
into your build setup.

# Expected Exceptions
When mocking code to throw an exception often two problems arise:
* Instantiating expected exceptions in every test method or class becomes tedious.
* Developers tend to skip giving those exceptions a meaningful message, cluttering the test log with stacktraces that are not clearly identifiable as being intentional.
  
To improve this situation, `ExpectedExceptions` provides instances of these `Exceptions´ (and `Throwables` and `Errors` respectively) so that developers may not need to come up with their own ones.