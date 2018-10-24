package app.kamix.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.kamix.models.Withdrawal;

public class WithdrawalData {
    @SerializedName("wcu_response") @Expose private WCUResponse wcuResponse;
    @SerializedName("withdrawal") @Expose private Withdrawal withdrawal;

    public WCUResponse getWcuResponse() {
        return wcuResponse;
    }

    public void setWcuResponse(WCUResponse wcuResponse) {
        this.wcuResponse = wcuResponse;
    }

    public Withdrawal getWithdrawal() {
        return withdrawal;
    }

    public void setWithdrawal(Withdrawal withdrawal) {
        this.withdrawal = withdrawal;
    }
}
