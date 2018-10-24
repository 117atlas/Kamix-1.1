package app.kamix.kamixui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

import app.kamix.R;
import app.kamix.kamixui.utils.Country;

public class CountrySpinnerAdapter extends BaseAdapter{
    public ArrayList<Country> cArr = new ArrayList<Country>();
    public Context ct;

    public CountrySpinnerAdapter(Context ct, ArrayList<Country> arr) {
        this.ct = ct;
        this.cArr = arr;
    }

    @Override
    public int getCount() {
        return cArr.size();
    }

    @Override
    public Country getItem(int i) {
        return cArr.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    Holder holder;

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(ct).inflate(R.layout.custom_countryspinner_row, null);
            holder = new Holder();
            holder.imgFLag = (ImageView) view.findViewById(R.id.imgFLag);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        holder.imgFLag.setImageResource(cArr.get(i).getFlag());
        return view;
    }

    class Holder {
        ImageView imgFLag;
    }
}
