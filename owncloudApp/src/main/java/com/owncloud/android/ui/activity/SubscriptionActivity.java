package com.owncloud.android.ui.activity;

import android.accounts.Account;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.Purchase.PurchasesResult;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.gson.JsonObject;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.datamodel.Plan;
import com.owncloud.android.datamodel.UserProfile;
import com.owncloud.android.datamodel.UserProfilesRepository;
import com.owncloud.android.ui.adapter.PlanListAdapter;
import com.owncloud.android.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

import co.spacium.cloud.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class SubscriptionActivity extends ToolbarActivity implements PurchasesUpdatedListener {

    PlanListAdapter mPlanListAdapter;

    RecyclerView mRecyclerView;
    List<Plan> plans = new ArrayList<Plan>();
    private ProgressBar mProgressBar = null;
    private BillingClient mBillingClient;
    private List<SkuDetails> mSkuDetailsList;
    CircularProgressBar circularProgressBar;

    TextView usedStorage;
    TextView usedStoragePercent;
    TextView totalStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);
        setupToolbar();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(getText(R.string.actionbar_subscription));
        mRecyclerView = findViewById(R.id.plans_recycler_view);

        circularProgressBar = findViewById(R.id.circularProgressBar);

        usedStorage = findViewById(R.id.used_storage);
        usedStoragePercent = findViewById(R.id.used_storage_percent);
        totalStorage = findViewById(R.id.total_storage);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mPlanListAdapter = new PlanListAdapter(plans, this);
        mRecyclerView.setAdapter(mPlanListAdapter);
        mProgressBar = findViewById(R.id.syncProgressBar);
        mProgressBar.setIndeterminate(true);
        loadPlans();
        updateQuota();
    }


    private void loadPlans() {
        getApi().getPlans().enqueue(new Callback<List<Plan>>() {
            @Override
            public void onResponse(Call<List<Plan>> call, retrofit2.Response<List<Plan>> response) {
                if (response.isSuccessful()) {
                    plans.clear();
                    plans.addAll(response.body());
                    mPlanListAdapter.notifyDataSetChanged();
                    mProgressBar.setIndeterminate(false);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    setupBillingClient();

                }
            }

            @Override
            public void onFailure(Call<List<Plan>> call, Throwable t) {

            }
        });
    }


    private void setupBillingClient() {
        mBillingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(this)
                .build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                loadPurchases();
                loadAllSKUs();
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });
    }

    private void loadPurchases(){
        PurchasesResult purchasesResult = mBillingClient.queryPurchases("subs");
        for (Purchase purchase:purchasesResult.getPurchasesList()){
            Timber.d(purchase.getSku());
        }
    }

    /**
     * Updates the quota in the drawer
     */
    private void updateQuota() {
        Account account = AccountUtils.getCurrentOwnCloudAccount(this);

        if (account == null) {
            return;
        }

        UserProfile.UserQuota userQuota = UserProfilesRepository.getUserProfilesRepository().getQuota(account.name);

        if (userQuota == null) {
            return;
        }


        // Update progress bar rounding up to next int. Example: quota is 0.54 => 1
        usedStorage.setText(DisplayUtils.bytesToHumanReadable(userQuota.getUsed(), this));
        totalStorage.setText(DisplayUtils.bytesToHumanReadable(userQuota.getTotal(), this));
        usedStoragePercent.setText(String.format("%s%% Used", String.valueOf(userQuota.getRelative())));
        circularProgressBar.setProgress((int) Math.ceil(userQuota.getRelative()));
    }

    private void loadAllSKUs() {
        if (!mBillingClient.isReady()) {
            return;
        }

        List<String> skuList = getSkuList();
        SkuDetailsParams params = SkuDetailsParams.newBuilder().setSkusList(skuList).setType(BillingClient.SkuType.SUBS).build();

        mBillingClient.querySkuDetailsAsync(params, (billingResult, list) -> {
            String message = billingResult.getDebugMessage();
            Timber.d(message);
            mSkuDetailsList = list;
        });

    }

    private List<String> getSkuList() {
        List<String> skuList = new ArrayList<>();
        for (Plan plan : plans) {
            skuList.add(plan.getAndroidProductId());
        }
        return skuList;
    }


    public void subscribe(Plan plan) {
        for (SkuDetails skuDetails : mSkuDetailsList) {
            if (plan.getAndroidProductId().equals(skuDetails.getSku())) {

                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build();

                mBillingClient.launchBillingFlow(this, billingFlowParams);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean retval = true;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                retval = super.onOptionsItemSelected(item);
        }
        return retval;
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
        if (list != null) {
            for (Purchase purchase : list) {
                getApi().processSubscription(purchase.getOriginalJson(), AccountUtils.getUsernameOfAccount(AccountUtils.getCurrentOwnCloudAccount(SubscriptionActivity.this).name)).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        Account account = AccountUtils.getCurrentOwnCloudAccount(SubscriptionActivity.this);
                        Timber.d(account.name);
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                });
            }
        }
    }
}

