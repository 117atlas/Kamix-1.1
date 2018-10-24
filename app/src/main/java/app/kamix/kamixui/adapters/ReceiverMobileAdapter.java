package app.kamix.kamixui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;

import app.kamix.R;
import app.kamix.app.Utils;
import app.kamix.kamixui.utils.FormatUtils;
import app.kamix.kamixui.utils.UiUtils;
import app.kamix.models.Contact;

public class ReceiverMobileAdapter extends ArrayAdapter<Contact> {
    private static String DEBUG = ReceiverMobileAdapter.class.getSimpleName();
    private ArrayList<Contact> allContacts;
    private ArrayList<Contact> contacts;
    private ArrayList<Contact> suggestions;
    private int viewResourceId;

    public ArrayList<Contact> getSuggestions() {
        return suggestions;
    }

    public ReceiverMobileAdapter(Context context, ArrayList<Contact> contacts) {
        super(context, R.layout.receiver_mobile_itemview, contacts);
        this.contacts = contacts;
        this.allContacts = contacts!=null?(ArrayList<Contact>) contacts.clone():null;
        this.suggestions = new ArrayList<Contact>();
        this.viewResourceId = R.layout.receiver_mobile_itemview;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(viewResourceId, null);
        }
        Contact contact = contacts.get(position);
        if (contact != null) {
            TextView contactName = (TextView) v.findViewById(R.id.contact_name);
            TextView contactMobile = v.findViewById(R.id.contact_mobile);
            TextView contactIndicator = v.findViewById(R.id.indicator);
            if (contactName != null) {
                contactName.setText(contact.getName());
            }
            if (contactMobile != null) {
                contactIndicator.setText(FormatUtils.getIndicator(contact.getMobile()));
                contactMobile.setText(FormatUtils.removeIndicator(contact.getMobile()));
            }
        }
        return v;
    }

    @Override
    public Filter getFilter() {
        return contactsFilter;
    }

    Filter contactsFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue) {
            String str = FormatUtils.removeIndicator(((Contact)(resultValue)).getMobile());
            return str;
        }
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if(constraint != null && allContacts!=null) {
                suggestions.clear();
                for (Contact contact : allContacts) {
                    if(contact.getMobile().toLowerCase().contains(constraint.toString().toLowerCase())){
                        suggestions.add(contact);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<Contact> filteredList = (ArrayList<Contact>) results.values;
            if(results != null && results.count > 0) {
                clear();
                for (Contact c : filteredList) {
                    add(c);
                }
                notifyDataSetChanged();
            }
        }
    };
}
