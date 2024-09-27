# KhanTech - Assignment

## Transaction Management System
The goal of this assignment is to create a simple system to manage financial transactions. The main focus will be on
business logic validation, transaction handling, and exception management, rather than just creating multiple endpoints.
The challenge lies in processing the transactions according to specific business rules.

### Requirements
1. Single Entity:
   The system will manage transactions. Each transaction will have an amount and a status.
2. Complex Business Logic:
   Transactions must go through several validations before being saved:
   • If the amount is negative, the transaction should be rejected.
   • If the user has insufficient balance, the transaction should be rejected.
   • If the transaction is larger than a configurable threshold (e.g., $1000), it must be manually approved before
   proceeding.
3. Service Layer:
   Implement a service that handles the transaction processing logic, including:
   • Validating if the transaction is legitimate (amount, balance, etc.).
   • Automatically approving transactions under a certain threshold.
   • Logging rejected transactions for reference.
   • Handling manual approval for transactions over the threshold.
4. Repository:
   Use an abstraction layer for data access. The system should be flexible enough to easily swap database technologies. For
   testing purposes, you can implement the repository using an in-memory database like H2, but ensure that the architecture
   allows for changing the database without significant code changes.
   • Demonstrate good design principles by defining appropriate interfaces and abstractions for the repository layer.
   • The deliverable should be structured in a way that supports switching from H2 to another database (e.g., PostgreSQL,
   MySQL) with minimal changes.
5. Endpoint:
   Implement a single POST endpoint /api/v1/transactions that allows submission of a transaction.
6. Bonus:
   • Add scheduled job logic for automatically processing pending transactions after a certain period (e.g., 24 hours).
   • Implement custom exception handling and validation to improve error management in the API.
   
### Model

   ```
   Transaction Class:
   • id (auto-generated)
   • userId
   • amount
   • status (PENDING, APPROVED, REJECTED, AWAITING_APPROVAL)
   • createdAt
   ```

   ```
   User Class:
   • id (auto-generated)
   • name
   • balance
   ```

### Bonus: 
   Create a wallet for the user to manage their balance more comprehensively.
   Deliverables:

   ```
   • Source code for the transaction management system with:
      • A working POST endpoint for submitting transactions.
      • A service layer that implements business logic.
      • A repository layer using H2 in-memory for storing data.
   • Tests demonstrating that the business logic and API work as expected.
   ```

### Evaluation Criteria:
   ```
   • Correct implementation of business logic.
   • Proper separation of concerns between controller, service, and repository layers.
   • Correct handling of exceptions and errors.
   • Clean code and appropriate use of Spring features.
   • Bonus points for implementing scheduled jobs and more advanced validation logic.
   ```

# Comments from Danylo
Prerequisites to run the solution:
   1. Java 23
   2. Docker (for integration tests using test containers)

JPA relations (@OneToMany, @OneToOne, etc) between entities are not used intentionally. Today it's more an antipattern than pattern
