# Follow-up Question — Answer

> **For larger and more complex projects, why is it discouraged to use web-exposed objects
> (e.g., `? extends GenericRequest`, `? extends GenericResponse`) within the Service layer?**

---

The Service layer must be transport-independent. If the Service depends on `GenericRequest`:

1. **Coupling**: changing the API format (REST → gRPC, adding a field) forces changes in the
   Service, violating the Single Responsibility Principle.

2. **Non-reusability**: the same Service cannot be called from non-web contexts
   (scheduled jobs, JMS messages) without artificial adapters.

3. **Testability**: testing business logic requires constructing web objects,
   introducing irrelevant dependencies.

4. **Security**: web objects may carry unsanitized input that the Service should never receive.

**Solution already adopted in this project**:
Controller → `Assembler.toCriteria()` → Service receives `Criteria` objects (pure POJOs).