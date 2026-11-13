# CO-1 Case Study 5: User Accounts, Email Verification and Contact Details

Requires JDK 17 or newer (uses sealed interfaces, records, pattern matching).

## Run in VS Code
1. Install the "Extension Pack for Java" (Microsoft).
2. File > Open Folder > select this `CaseStudy5` folder.
3. Open `src/useraccounts/Main.java` and click "Run" above `main`.

## Run from a terminal
    javac -d out src/useraccounts/*.java
    java -cp out useraccounts.Main

## Files (all in src/useraccounts)
- Value objects: UserId, EmailAddress, PhoneNumber, PostalAddress, VerificationToken
- State types: EmailVerification (sealed: Unverified | Verified), AccountStatus
- Entity / aggregate root: UserAccount
- Cross-aggregate rule: UserRepository, InMemoryUserRepository (email uniqueness)
- Workflow: RegistrationService
- Demo: Main
