package app.kamix.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import app.kamix.models.Funding;

public class FundingData implements Serializable{
    @SerializedName("funding") @Expose private Funding funding;
    @SerializedName("wcu_response") @Expose private WCUResponse wcuResponse;

    public Funding getFunding() {
        return funding;
    }

    public void setFunding(Funding funding) {
        this.funding = funding;
    }

    public WCUResponse getWcuResponse() {
        return wcuResponse;
    }

    public void setWcuResponse(WCUResponse wcuResponse) {
        this.wcuResponse = wcuResponse;
    }
}
