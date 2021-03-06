package org.egovernments.egoverp.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.egovernments.egoverp.R;
import org.egovernments.egoverp.models.TaxDetail;

import java.lang.ref.WeakReference;
import java.util.List;

public class TaxAdapter extends BaseAdapter {

    private WeakReference<Context> contextWeakReference;
    private List<TaxDetail> taxDetails;

    public TaxAdapter(List<TaxDetail> taxDetails, Context context) {
        this.taxDetails = taxDetails;
        this.contextWeakReference = new WeakReference<>(context);
    }

    @Override
    public int getCount() {
        return taxDetails.size();
    }

    @Override
    public Object getItem(int position) {
        return taxDetails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TaxViewHolder taxViewHolder = null;
        View view = convertView;
        if (convertView == null) {

            view = LayoutInflater.from(contextWeakReference.get()).inflate(R.layout.item_tax, parent, false);

            taxViewHolder = new TaxViewHolder();
            taxViewHolder.taxAmount = (TextView) view.findViewById(R.id.propertytax_taxamount);
            taxViewHolder.taxChequePenalty = (TextView) view.findViewById(R.id.propertytax_chequepenalty);
            taxViewHolder.taxPenalty = (TextView) view.findViewById(R.id.propertytax_penalty);
            taxViewHolder.taxInstallment = (TextView) view.findViewById(R.id.propertytax_installment);
            taxViewHolder.taxRebate = (TextView) view.findViewById(R.id.propertytax_rebate);
            taxViewHolder.taxTotal = (TextView) view.findViewById(R.id.propertytax_total);

            view.setTag(taxViewHolder);
        }

        if (taxViewHolder == null) {
            taxViewHolder = (TaxViewHolder) view.getTag();
        }
        TaxDetail taxDetail = (TaxDetail) getItem(position);

        taxViewHolder.taxInstallment.setText("Installment: " + taxDetail.getInstallment());
        taxViewHolder.taxAmount.setText("Amount: Rs. " + taxDetail.getTaxAmount());
        taxViewHolder.taxChequePenalty.setText("Cheque Bounce Penalty: Rs. " + taxDetail.getChqBouncePenalty());
        taxViewHolder.taxPenalty.setText("Penalty: Rs. " + taxDetail.getPenalty());
        taxViewHolder.taxRebate.setText("Rebate: Rs. " + taxDetail.getRebate());
        taxViewHolder.taxTotal.setText("Total: Rs. " + taxDetail.getTotalAmount());

        return view;
    }

    public static class TaxViewHolder {

        private TextView taxAmount;
        private TextView taxChequePenalty;
        private TextView taxPenalty;
        private TextView taxInstallment;
        private TextView taxRebate;
        private TextView taxTotal;
    }
}
