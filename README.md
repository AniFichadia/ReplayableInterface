[![Build Status](https://travis-ci.org/AniFichadia/ReplayableInterface.svg?branch=develop)](https://travis-ci.org/AniFichadia/ReplayableInterface)

# ReplayableInterface
ReplayableInterface takes an interface and uses code generation and annotation processing to generate a proxy/delegate/commander/insertSomeOtherRelevantPattern implementation.

The generated code uses a kind of smart/priorty queue that saves and/or proxies calls to the interface when the real implementation is disconnected. It then allows either all, or some events to be 'replayed' against the real implementation (hence the naming).

This allows the consumption of interface implementations without worrying about null checks.

More information coming soon!
