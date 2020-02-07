package com.owncloud.android.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.owncloud.android.ui.dialog.LoadingDialog;

import co.spacium.cloud.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends ToolbarActivity {

    EditText name;
    EditText email;
    EditText password;
    EditText confirmPassword;
    TextView errorTextView;

    private static final String WAIT_DIALOG_TAG = "WAIT_DIALOG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = findViewById(R.id.account_name);
        email = findViewById(R.id.account_email);
        password = findViewById(R.id.account_password);
        confirmPassword = findViewById(R.id.account_confirm_password);

        errorTextView = findViewById(R.id.auth_status_text);

    }

    public void onLoginClick(View view) {
        finish();
    }

    String validate(){
        if(name.getText().toString().length() == 0){
            return  "Name is required";
        }
        if(email.getText().toString().length() == 0){
            return "Email is required";
        }
        if(password.getText().toString().length() == 0){
            return "Password is required";
        }
        if(!confirmPassword.getText().toString().equals(password.getText().toString())){
            return "Passwords do not match";
        }
        return null;
    }

    public void onRegisterClick(View view) {
        String error = validate();
        showError(error);
        if(error != null){
            return;
        }

        LoadingDialog dialog = LoadingDialog.newInstance(R.string.auth_trying_to_register, false);
        dialog.show(getSupportFragmentManager(), WAIT_DIALOG_TAG);

        getApi().register(name.getText().toString(),email.getText().toString(),password.getText().toString()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        dialog.dismiss();
                        showSuccess();
                    }else {
                        try {
                            JsonObject errorResponse = new JsonParser().parse(response.errorBody().string()).getAsJsonObject();
                            showError(errorResponse.get("message").getAsString());
                        }catch (Exception exception){
                            showError("An error has occurred. Please try again later");
                        }

                    }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }

    private void showError(String error){
        if(error == null){
            errorTextView.setVisibility(View.GONE);
        }else {
            errorTextView.setVisibility(View.VISIBLE);
        }
        errorTextView.setText(error);
    }

    private void showSuccess(){
        findViewById(R.id.success_container).setVisibility(View.VISIBLE);
        findViewById(R.id.form_container).setVisibility(View.GONE);
    }
}
