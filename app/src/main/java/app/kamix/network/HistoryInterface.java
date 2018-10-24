package app.kamix.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.kamix.models.Funding;
import app.kamix.models.Payment;
import app.kamix.models.Transfert;
import app.kamix.models.Withdrawal;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface HistoryInterface {

    @GET("history/{userid}")
    Call<HistoryResponse> history(@Path("userid") String userid);

    public class HistoryResponse extends BaseResponse{
        @SerializedName("transferts") @Expose List<Transfert> transferts;
        @SerializedName("payments") @Expose List<Payment> payments;
        @SerializedName("fundings") @Expose List<Funding> fundings;
        @SerializedName("withdrawals") @Expose List<Withdrawal> withdrawals;

        public List<Transfert> getTransferts() {
            return transferts;
        }

        public void setTransferts(List<Transfert> transferts) {
            this.transferts = transferts;
        }

        public List<Payment> getPayments() {
            return payments;
        }

        public void setPayments(List<Payment> payments) {
            this.payments = payments;
        }

        public List<Funding> getFundings() {
            return fundings;
        }

        public void setFundings(List<Funding> fundings) {
            this.fundings = fundings;
        }

        public List<Withdrawal> getWithdrawals() {
            return withdrawals;
        }

        public void setWithdrawals(List<Withdrawal> withdrawals) {
            this.withdrawals = withdrawals;
        }
    }
}
