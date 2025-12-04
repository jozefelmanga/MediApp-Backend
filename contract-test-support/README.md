# MediApp Contract Test Support

Reusable Spring Cloud Contract base classes and dependencies to keep microservice contract tests consistent.

## Usage

1. Add the dependency to a service module (test scope recommended):

```xml
<dependency>
    <groupId>com.mediapp</groupId>
    <artifactId>contract-test-support</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

2. Extend `com.mediapp.contracts.BaseMvcContractTest` (for MVC) or `BaseWebFluxContractTest` (for WebFlux) in the generated base class configured by Spring Cloud Contract.

3. Place Groovy/YAML contract definitions under `src/test/resources/contracts` within each service. Generated tests will automatically leverage the shared setup.

4. Use the shared `contract-test` Spring profile to load lightweight configurations during verification.
