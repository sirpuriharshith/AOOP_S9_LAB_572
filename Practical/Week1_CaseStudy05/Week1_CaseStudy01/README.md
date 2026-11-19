# CO-1 Case Study 1: Money and Pricing in an E-Commerce Catalog

Requires JDK 17 or newer (uses records, exact minor-unit math).

## Run in VS Code
1. Install the "Extension Pack for Java" (Microsoft).
2. File > Open Folder > select this `Week1_CaseStudy01` folder.
3. Open `src/casestudy1/Main.java` and click "Run" above `main`.

## Run from a terminal
    javac -d out src/casestudy1/*.java
    java -cp out casestudy1.Main

## Files (all in src/casestudy1)
- Value objects: Money, Currency, Percentage, Quantity, Refund, SignedMoney
- Entity / aggregate root: Cart, Product
- Domain service: ExchangeService (cross-currency explicit rate conversions)
- Exception: DomainException
- Demo: Main (10 scenarios, 45 automated checks)
