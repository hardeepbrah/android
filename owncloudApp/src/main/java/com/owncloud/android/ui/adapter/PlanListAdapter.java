package com.owncloud.android.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.owncloud.android.datamodel.Plan;
import com.owncloud.android.ui.activity.SubscriptionActivity;

import java.util.ArrayList;
import java.util.List;

import co.spacium.cloud.R;

public class PlanListAdapter extends RecyclerView.Adapter<PlanListAdapter.PlanViewHolder> {

    private List<Plan> plans;
    SubscriptionActivity subscriptionActivity;

    public PlanListAdapter(List<Plan> plans,SubscriptionActivity subscriptionActivity) {
        this.plans = plans;
        this.subscriptionActivity = subscriptionActivity;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plan_list, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        holder.bind(plans.get(position));
        holder.subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscriptionActivity.subscribe(plans.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    class PlanViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView currentPlan;
        TextView cost;
        TextView days;
        Button subscribeButton;

        PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.plan_name);
            currentPlan = itemView.findViewById(R.id.button_current_plan);
            subscribeButton = itemView.findViewById(R.id.button_subscribe);
        }


        public void bind(Plan plan) {
            name.setText(plan.getName());
            if(plan.getActive()){
                subscribeButton.setVisibility(View.GONE);
                currentPlan.setVisibility(View.VISIBLE);
            }else {
                subscribeButton.setVisibility(View.VISIBLE);
                currentPlan.setVisibility(View.GONE);
                subscribeButton.setText(plan.getCtaText());
            }
        }
    }
}

