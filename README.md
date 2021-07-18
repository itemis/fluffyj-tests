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