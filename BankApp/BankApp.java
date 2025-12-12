import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Single-file Java OOP console application: BankApp
 * - Put this code into BankApp.java
 * - Compile: javac BankApp.java
 * - Run:     java BankApp
 *
 * Designed to demonstrate OOP: abstraction, inheritance, encapsulation, composition.
 */

public class BankApp {
    public static void main(String[] args) {
        Bank bank = new Bank("Simple Bank");
        bank.seedDemoData(); // optional demo accounts
        bank.runConsole();
    }
}

/* -----------------------------
   Domain classes (OOP design)
   ----------------------------- */

abstract class Account {
    private static long nextId = 1001;
    private final long id;
    private final String ownerName;
    protected double balance;
    private final List<Transaction> transactions = new ArrayList<>();

    public Account(String ownerName, double initialDeposit) {
        this.id = nextId++;
        this.ownerName = ownerName;
        this.balance = Math.max(0.0, initialDeposit);
        if (initialDeposit > 0) {
            transactions.add(new Transaction("INITIAL_DEPOSIT", initialDeposit, "Initial deposit"));
        }
    }

    public long getId() { return id; }
    public String getOwnerName() { return ownerName; }
    public double getBalance() { return balance; }

    // deposit common behavior
    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        balance += amount;
        transactions.add(new Transaction("DEPOSIT", amount, "Deposit to account"));
    }

    // withdraw: concrete classes may override to add fees or rules
    public void withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive.");
        if (amount > balance) throw new IllegalArgumentException("Insufficient funds.");
        balance -= amount;
        transactions.add(new Transaction("WITHDRAW", -amount, "Withdrawal from account"));
    }

    public void addTransaction(String type, double amount, String note) {
        transactions.add(new Transaction(type, amount, note));
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    // Each account can have its own monthly update (interest, fees...)
    public abstract void monthlyUpdate();

    @Override
    public String toString() {
        return String.format("[%d] %s - Owner: %s, Balance: %.2f", getId(), getAccountType(), getOwnerName(), getBalance());
    }

    public abstract String getAccountType();
}

class SavingsAccount extends Account {
    private double annualInterestRate; // e.g., 0.03 for 3%

    public SavingsAccount(String ownerName, double initialDeposit, double annualInterestRate) {
        super(ownerName, initialDeposit);
        this.annualInterestRate = Math.max(0.0, annualInterestRate);
    }

    @Override
    public void monthlyUpdate() {
        // monthly interest
        double monthlyRate = annualInterestRate / 12.0;
        double interest = getBalance() * monthlyRate;
        if (interest > 0) {
            balanceAdd(interest);
            addTransaction("INTEREST", interest, "Monthly interest");
        }
    }

    // helper to update balance even though balance is protected
    private void balanceAdd(double amount) {
        // directly modify protected field
        this.balance += amount;
    }

    @Override
    public String getAccountType() { return "Savings"; }
}

class CheckingAccount extends Account {
    private int freeWithdrawalsPerMonth;
    private double withdrawalFee; // fee after free withdrawals
    private int withdrawalsThisMonth = 0;

    public CheckingAccount(String ownerName, double initialDeposit, int freeWithdrawalsPerMonth, double withdrawalFee) {
        super(ownerName, initialDeposit);
        this.freeWithdrawalsPerMonth = Math.max(0, freeWithdrawalsPerMonth);
        this.withdrawalFee = Math.max(0.0, withdrawalFee);
    }

    @Override
    public void withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive.");
        double total = amount;
        if (withdrawalsThisMonth >= freeWithdrawalsPerMonth) {
            total += withdrawalFee;
        }
        if (total > getBalance()) throw new IllegalArgumentException("Insufficient funds (including fees).");
        // apply
        this.balance -= total;
        withdrawalsThisMonth++;
        addTransaction("WITHDRAW", -amount, "Checking withdrawal");
        if (total != amount) {
            addTransaction("FEE", -withdrawalFee, "Withdrawal fee");
        }
    }

    @Override
    public void monthlyUpdate() {
        // reset counters monthly
        withdrawalsThisMonth = 0;
    }

    @Override
    public String getAccountType() { return "Checking"; }
}

/* Simple transaction log class */
class Transaction {
    private final LocalDateTime timestamp;
    private final String type;
    private final double amount;
    private final String note;

    public Transaction(String type, double amount, String note) {
        this.timestamp = LocalDateTime.now();
        this.type = type;
        this.amount = amount;
        this.note = note;
    }

    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("%s | %-12s | %8.2f | %s", timestamp.format(fmt), type, amount, note);
    }
}

/* -----------------------------
   Bank: manages accounts & console UI
   ----------------------------- */

class Bank {
    private final String name;
    private final Map<Long, Account> accounts = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);

    public Bank(String name) {
        this.name = name;
    }

    // demo seed
    public void seedDemoData() {
        createAccountInternal(new SavingsAccount("Alice", 1000.0, 0.03));
        createAccountInternal(new CheckingAccount("Bob", 500.0, 2, 1.0));
    }

    // internal helper to skip printing
    private void createAccountInternal(Account acc) {
        accounts.put(acc.getId(), acc);
    }

    public void runConsole() {
        System.out.println("Welcome to " + name + " (Console)");
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Choose option: ");
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1": actionCreateAccount(); break;
                    case "2": actionDeposit(); break;
                    case "3": actionWithdraw(); break;
                    case "4": actionTransfer(); break;
                    case "5": actionShowAccounts(); break;
                    case "6": actionShowTransactions(); break;
                    case "7": actionMonthlyUpdate(); break;
                    case "0": running = false; break;
                    default: System.out.println("Invalid option, try again."); break;
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
            System.out.println();
        }
        System.out.println("Goodbye!");
    }

    private void printMenu() {
        System.out.println("=== Menu ===");
        System.out.println("1) Create account");
        System.out.println("2) Deposit");
        System.out.println("3) Withdraw");
        System.out.println("4) Transfer");
        System.out.println("5) Show accounts");
        System.out.println("6) Show transactions for an account");
        System.out.println("7) Monthly update (interest/fees reset)");
        System.out.println("0) Exit");
    }

    private void actionCreateAccount() {
        System.out.print("Owner name: ");
        String owner = scanner.nextLine().trim();
        if (owner.isEmpty()) { System.out.println("Owner name cannot be empty."); return; }

        System.out.println("Account type: 1) Savings  2) Checking");
        String type = scanner.nextLine().trim();
        System.out.print("Initial deposit: ");
        double initial = parseDoubleInput(scanner.nextLine());

        Account acc;
        if ("1".equals(type)) {
            System.out.print("Annual interest rate (e.g., 0.03 for 3%): ");
            double rate = parseDoubleInput(scanner.nextLine());
            acc = new SavingsAccount(owner, initial, rate);
        } else {
            System.out.print("Free withdrawals per month (e.g., 2): ");
            int free = parseIntInput(scanner.nextLine());
            System.out.print("Withdrawal fee after free withdrawals (e.g., 1.0): ");
            double fee = parseDoubleInput(scanner.nextLine());
            acc = new CheckingAccount(owner, initial, free, fee);
        }
        accounts.put(acc.getId(), acc);
        System.out.println("Account created: " + acc);
    }

    private void actionDeposit() {
        Account acc = askForAccount("Enter account id to deposit into: ");
        if (acc == null) return;
        System.out.print("Amount to deposit: ");
        double amt = parseDoubleInput(scanner.nextLine());
        acc.deposit(amt);
        System.out.printf("Deposited %.2f into account %d. New balance: %.2f\n", amt, acc.getId(), acc.getBalance());
    }

    private void actionWithdraw() {
        Account acc = askForAccount("Enter account id to withdraw from: ");
        if (acc == null) return;
        System.out.print("Amount to withdraw: ");
        double amt = parseDoubleInput(scanner.nextLine());
        acc.withdraw(amt);
        System.out.printf("Withdrawn %.2f from account %d. New balance: %.2f\n", amt, acc.getId(), acc.getBalance());
    }

    private void actionTransfer() {
        Account from = askForAccount("From account id: ");
        if (from == null) return;
        Account to = askForAccount("To account id: ");
        if (to == null) return;
        System.out.print("Amount to transfer: ");
        double amt = parseDoubleInput(scanner.nextLine());
        // basic transfer with simple validation
        if (amt <= 0) { System.out.println("Amount must be positive."); return; }
        if (amt > from.getBalance()) { System.out.println("Insufficient funds."); return; }

        from.withdraw(amt); // this will apply fees if checking
        to.deposit(amt);
        from.addTransaction("TRANSFER_OUT", -amt, "Transfer to account " + to.getId());
        to.addTransaction("TRANSFER_IN", amt, "Transfer from account " + from.getId());
        System.out.printf("Transferred %.2f from %d to %d.\n", amt, from.getId(), to.getId());
    }

    private void actionShowAccounts() {
        if (accounts.isEmpty()) {
            System.out.println("No accounts available.");
            return;
        }
        System.out.println("Accounts:");
        for (Account a : accounts.values()) {
            System.out.println(a);
        }
    }

    private void actionShowTransactions() {
        Account acc = askForAccount("Enter account id to view transactions: ");
        if (acc == null) return;
        System.out.println("Transactions for account " + acc.getId() + ":");
        for (Transaction t : acc.getTransactions()) {
            System.out.println(t);
        }
        System.out.printf("Current balance: %.2f\n", acc.getBalance());
    }

    private void actionMonthlyUpdate() {
        for (Account a : accounts.values()) {
            a.monthlyUpdate();
        }
        System.out.println("Monthly update applied to all accounts (interest/fee resets).");
    }

    private Account askForAccount(String prompt) {
        System.out.print(prompt);
        String s = scanner.nextLine().trim();
        long id;
        try {
            id = Long.parseLong(s);
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid id.");
            return null;
        }
        Account acc = accounts.get(id);
        if (acc == null) System.out.println("Account not found.");
        return acc;
    }

    private double parseDoubleInput(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private int parseIntInput(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
