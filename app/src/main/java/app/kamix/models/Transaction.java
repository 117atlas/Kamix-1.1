package app.kamix.models;

public class Transaction {
    protected long tdate;

    public long getTdate() {
        return tdate;
    }

    public void setTdate() {
        if (this instanceof Funding) this.tdate = ((Funding)this).getDate();
        else if (this instanceof Withdrawal) this.tdate = ((Withdrawal)this).getDate();
        else if (this instanceof Transfert) this.tdate = ((Transfert)this).getDate();
        else if (this instanceof Payment) this.tdate = ((Payment)this).getDate();
    }
}
