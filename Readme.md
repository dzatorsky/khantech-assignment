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
   - If the amount is negative, the transaction should be rejected.
   - If the user has insufficient balance, the transaction should be rejected.
   - If the transaction is larger than a configurable threshold (e.g., $1000), it must be manually approved before
   proceeding.
3. Service Layer:

   Implement a service that handles the transaction processing logic, including:
   - Validating if the transaction is legitimate (amount, balance, etc.).
   - Automatically approving transactions under a certain threshold.
   - Logging rejected transactions for reference.
   - Handling manual approval for transactions over the threshold.
4. Repository:

    Use an abstraction layer for data access. The system should be flexible enough to easily swap database technologies. For
   testing purposes, you can implement the repository using an in-memory database like H2, but ensure that the architecture
   allows for changing the database without significant code changes.
   -  Demonstrate good design principles by defining appropriate interfaces and abstractions for the repository layer.
   -  The deliverable should be structured in a way that supports switching from H2 to another database (e.g., PostgreSQL,
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

## Comments from Danylo
- Prerequisites to run the solution:
   1. Java 23
   2. Docker (for integration tests using test containers with postgres DB)
### Running automated tests (with Postgres or H2 as a DB)
- All of the tests use DB under the hood instead of mockito mocks. The database implementation chosen based on profile, therefore in order to run all the tests you need to do:
    - `./gradlew test` #This will run all tests with H2
    - `./gradlew integrationTest` #This will run all tests with Postgres DB running as docker test container. This will spawn docker container automatically.

### Running manual tests
- To start the app I recommend using [TestKhantechApplication.java](src/test/java/com/khantech/assignment/TestKhantechApplication.java). It will automatically start docker container with Postgres
- Test container postgres DB Port is 15432. Login/Password/DBName is khantech/khantech/khantech
- In order to submit transaction the user and the wallet must already exist in the system. The user can have multiple wallets in different currencies. Each transaction is associated with specific wallet.

- So in order to submit transactions you need to create user and wallet first:
  - Example request to create user. Remember the userId, it will be needed in the next step. User id "67fa8ff5-01fb-4e64-8521-0f2c8d929f91" returned in my specific case.
  ```
   curl -X POST http://localhost:8888/api/users -H "Content-Type: application/json" -d '{"name": "John Doe"}'
  ```
  - Example request to create wallet in a given currency for a user created on a previous step. Remember the "id" field returned by server which is id of a newly created wallet, you will need it for submitting transactions on the next step (In my case it returned 9f36f5b4-0d4d-4301-9e9d-3469cc21e551):
  ```
   curl -X POST http://localhost:8888/api/wallets -H "Content-Type: application/json" -d '{"userId": "67fa8ff5-01fb-4e64-8521-0f2c8d929f91","currency": "USD"}'
  ```
  - Example request to submit the transaction. Make sure you generate unique requestId on each request. It's used for removing duplicates. The walletId should be used from the response to previous request. You are good to go to submit as many transactions as you want, just remember that requestId should be unique on each request:
  ```
  curl -X POST http://localhost:8888/api/wallets/9f36f5b4-0d4d-4301-9e9d-3469cc21e551/transactions -H "Content-Type: application/json" -d '{"requestId": "edf2b545-0480-4d30-8816-4f2c171b0263", "amount": 120.01, "type": "CREDIT"}'
  ```
  ```
  curl -X POST http://localhost:8888/api/wallets/9f36f5b4-0d4d-4301-9e9d-3469cc21e551/transactions -H "Content-Type: application/json" -d '{"requestId": "e5124993-5abc-4754-bc6e-9d32fefc8a5f", "amount": 200000.55, "type": "CREDIT"}'
  ```
  ```
  curl -X POST http://localhost:8888/api/wallets/9f36f5b4-0d4d-4301-9e9d-3469cc21e551/transactions -H "Content-Type: application/json" -d '{"requestId": "e5124993-5abc-4754-bc6e-9d32fefc8a5f", "amount": 500.77, "type": "DEBIT"}'
  ```
  -  Example request to approve the transaction. In my case the transaction in AWAITING_APPROVAL status is 50e73494-b195-439d-960e-797381ca245d:
    ```
  curl -X POST http://localhost:8888/api/admin/transactions/50e73494-b195-439d-960e-797381ca245d/approve"
  ```
- Swagger for facilitated testing is available at: http://localhost:8888/docs

### Other
- Processing transaction synchronously is more reliable way of workiing with transactions, therefore state PENDING has been removed.
    So transactions go through the validation steps and transit into either APPROVED or WAITING_APPROVAL states immediately on submission.
- Chain of responsibility used for transaction approval and submission with idea that there might be much more business rules to be added in the future