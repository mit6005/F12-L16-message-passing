package beforeclass;
import java.util.ArrayList;
import java.util.List;

// WARNING: This class suffers from race conditions.
public class Bank {
    private List<Account> accounts;
    
    class Account {
        // the amount of money in the account, in dollars
        private int balance = 0;
    }
    
    public Bank(int n) {
        accounts = new ArrayList<Account>();
        for (int i = 0; i < n; ++i) {
            accounts.add(new Account());
        }
    }
    
    
    /**
     * @param from     account to transfer from
     * @param to       account to transfer to; requires from != to.
     * Modifies from and to by withdrawing 1 dollar from from and depositing it into to.
     */
    public void transfer(Account from, Account to) {
        from.balance = from.balance - 1;
        to.balance = to.balance + 1;
    }
    
    /**
     * @return total balance of the bank
     */
    public int audit() {
        int sum = 0;
        System.out.print("account balances: ");
        for (Account account : accounts) {
            System.out.print(account.balance + " ");
            sum += account.balance;
        }
        System.out.println();
        
        return sum;
    }
    
    /**
     * @return a randomly-chosen account from this bank
     */
    public Account randomAccount() {
        int i = (int) (Math.random() * accounts.size());
        return accounts.get(i);
    }
    

    

    // simulate a network of cash machines, handling customer transactions concurrently
    private static final int NUM_ACCOUNTS = 5;
    private static final int NUM_CASH_MACHINES = 2;
    private static final int TRANSACTIONS_PER_MACHINE = 1000;
        
    // each ATM does a bunch of transfers between accounts, which should
    // leave the overall balance of the bank unchanged
    private static void cashMachine(Bank bank) {
        for (int i = 0; i < TRANSACTIONS_PER_MACHINE; ++i) {
            Account from = bank.accounts.get(0);
            Account to = bank.randomAccount();
            if (from != to) {
                bank.transfer(from, to);                
            }
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        final Bank bank = new Bank(NUM_ACCOUNTS);
        
        System.out.println ("starting audit is " + bank.audit());

        System.out.println("...then " 
                            + NUM_CASH_MACHINES 
                            + " cash machines do "
                            + TRANSACTIONS_PER_MACHINE
                            + " transfers...");

        // simulate each cash machine with a thread
        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < NUM_CASH_MACHINES; ++i) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    Thread.yield();  // give the other threads a chance to start too, so it's a fair race
                    cashMachine(bank);   // do the transactions for this cash machine
                }
            });
            t.start(); // don't forget to start the thread!
            threads.add(t);
        }
        
        // wait for all the threads to finish
        for (Thread t: threads) t.join(); 
    
        System.out.println("final audit is " + bank.audit()); 
    }
}
