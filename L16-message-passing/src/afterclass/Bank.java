package afterclass;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// WARNING: This class suffers from deadlocks.
public class Bank {
    private List<Account> accounts;
    
    class Account {
        // the amount of money in the account, in dollars
        private int balance = 0;
    
        // a lock that protects this bank account from concurrent access
        private Lock lock = new ReentrantLock();
        // DON'T IMITATE THIS CODE.  Instead you should use the synchronized
        // keyword, which uses the lock built into every Java object.
        // This example uses a Lock object so that we can show the explicit
        // lock() and unlock() operations.
    }
    
    public Bank(int n) {
        accounts = new ArrayList<Account>();
        for (int i = 0; i < n; ++i) {
            accounts.add(new Account());
        }
    }
    
    
    /**
     * @param from     account to transfer from
     * @param to       account to transfer to.
     * Modifies from and to by withdrawing 1 dollar from from and depositing it into to.
     */
    public void transfer(Account from, Account to) {
        from.lock.lock();
        to.lock.lock();

        from.balance = from.balance - 1;
        to.balance = to.balance + 1;

        from.lock.unlock();
        to.lock.unlock();
    }
    
    /**
     * @return total balance of the bank
     */
    public int audit() {
        // acquire the locks on all the accounts
        for (Account account : accounts) { 
            account.lock.lock(); 
        }
        
        // operations on the whole bank is now suspended
        // while we sum up the books
        int sum = 0;
        System.out.print("account balances: ");
        for (Account account : accounts) {
            System.out.print(account.balance + " ");
            sum += account.balance;
        }
        System.out.println();
        
        // release all the locks
        for (Account account : accounts) {
            account.lock.unlock(); 
        }
        
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
            Account from = bank.randomAccount();
            Account to = bank.randomAccount();
            bank.transfer(from, to);                
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        final Bank bank = new Bank(NUM_ACCOUNTS);
        
        System.out.println ("starting audit is " + bank.audit());

        System.out.println("...then " 
                            + NUM_CASH_MACHINES 
                            + " cash machines do "
                            + TRANSACTIONS_PER_MACHINE
                            + " transfers between random accounts...");

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
