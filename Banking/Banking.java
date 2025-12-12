public class BankAccount {
    private string accountNumber;
    private double balance;
    private Static string bankName;
    private void setAccountNumber( string accountnumbe){
        this.accountNumber = accountnumbe;
    }
    public string getaccountnumber();{
        return accountnumbe;
    }
    public double getBalance(){
        return balance;
    }
    public void diposit( int money){
        balance+=money;
    }
    public void withdraw ( int amount){
        if( balance>= amount){
            balance=-amount;
        }
        else{
            System.out.println("Insufficiant Balance");

        }
    }
    static void setBankName(string bankName){
        BankAccount.bankName = bankName;
    }
    public void diposit (int money){
        balance+= money;
    }
    public void withdraw(int amount){
        if(balance>=amount){
            balance=-amount;
        }else{
            System.out.println("Insufficiant Balance");
        }
    }
    static void setBankName( string bankName){
        BankAccount.bankName = bankName;
    }
    static string getBankName(){
        return bankName;
    }
    static void setIntrestRate(double IntrestRate){
        BankAccount.intrestRate = intrestRate;
    }
    Static double getIntrestRate(){
        return intrestRate;
    }

}