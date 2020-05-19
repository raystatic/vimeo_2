package com.example.vimeo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.truecaller.android.sdk.ITrueCallback;
import com.truecaller.android.sdk.TrueError;
import com.truecaller.android.sdk.TrueException;
import com.truecaller.android.sdk.TrueProfile;
import com.truecaller.android.sdk.TruecallerSDK;
import com.truecaller.android.sdk.TruecallerSdkScope;
import com.truecaller.android.sdk.clients.VerificationCallback;
import com.truecaller.android.sdk.clients.VerificationDataBundle;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TruecallerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_truecaller);

        TruecallerSdkScope trueScope = new TruecallerSdkScope.Builder(this, sdkCallback)
                .consentMode(TruecallerSdkScope.CONSENT_MODE_FULLSCREEN )
                .consentTitleOption( TruecallerSdkScope.SDK_CONSENT_TITLE_VERIFY )
                .sdkOptions( TruecallerSdkScope.SDK_OPTION_WITH_OTP )
                .build();

        TruecallerSDK.init(trueScope);

        TruecallerSDK.getInstance().getUserProfile(TruecallerActivity.this);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        TruecallerSDK.getInstance().onActivityResultObtained(this, resultCode, data);

    }

    private final ITrueCallback sdkCallback = new ITrueCallback() {

        @Override
        public void onSuccessProfileShared(@NonNull final TrueProfile trueProfile) {
            Log.d("truecaller_request", "onSuccessProfileShared: "+trueProfile.phoneNumber);
            toast(trueProfile.accessToken);
        }

        @Override
        public void onFailureProfileShared(@NonNull final TrueError trueError) {

            Log.d("truecaller_request", "onFailureProfileShared: "+trueError.getErrorType()+"\n"+trueError.describeContents());


            switch (trueError.getErrorType()){
                case 1 : toast("Network Failure");
                        break;
                case 2 : toast("User pressed back");
                    break;
                case 3 : toast("Incorrect Partner Key");
                    break;
                case 4 :
                case 10 :
                    toast("User not Verified on Truecaller");
                    break;
                case 5 : toast("Truecaller App Internal Error");
                    break;
                case 13 : toast("User pressed back while verification in process");
                    break;
                case 14 : toast("User pressed \"SKIP / USE ANOTHER NUMBER\"");
                    break;
            }

        }

        @Override
        public void onVerificationRequired() {

            TruecallerSDK.getInstance().requestVerification("IN", "9650124756", apiCallback, TruecallerActivity.this);

        }

    };

     private final VerificationCallback apiCallback = new VerificationCallback() {

        @Override
        public void onRequestSuccess(int requestCode, @Nullable VerificationDataBundle extras) {

            if (requestCode == VerificationCallback.TYPE_MISSED_CALL_INITIATED) {
                Log.d("truecaller_request", "onRequestSuccess: TYPE_MISSED_CALL_INITIATED");

            }
            if (requestCode == VerificationCallback.TYPE_MISSED_CALL_RECEIVED) {
                Log.d("truecaller_request", "onRequestSuccess: TYPE_MISSED_CALL_RECEIVED");
                TrueProfile profile = new TrueProfile.Builder("raystatic", "testing").build();
                TruecallerSDK.getInstance().verifyMissedCall(profile, apiCallback);

            }
            if (requestCode == VerificationCallback.TYPE_OTP_INITIATED) {
                Log.d("truecaller_request", "onRequestSuccess: TYPE_OTP_INITIATED");

            }
            if (requestCode == VerificationCallback.TYPE_OTP_RECEIVED) {
                Log.d("truecaller_request", "onRequestSuccess: TYPE_OTP_RECEIVED");
                TrueProfile profile = new TrueProfile.Builder("raystatic", "testing").build();

                String otp = "";
                if (extras!=null && extras.getString(VerificationDataBundle.KEY_OTP)!=null){
                    otp = extras.getString(VerificationDataBundle.KEY_OTP);
                }

                if (!TextUtils.isEmpty(otp))
                    TruecallerSDK.getInstance().verifyOtp(profile, otp ,apiCallback);
            }
            if (requestCode == VerificationCallback.TYPE_VERIFICATION_COMPLETE) {
                Log.d("truecaller_request", "onRequestSuccess: TYPE_VERIFICATION_COMPLETE");

                if (extras!=null){
                    String accessToken = extras.getString(VerificationDataBundle.KEY_ACCESS_TOKEN);
                    ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                    apiInterface.verifyUser(accessToken).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            String res = response.body();
                            Log.d("truecaller_verify", "onResponse: "+res + "\n" + response.code());
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Log.d("truecaller_verify", "onFailure: "+t.getLocalizedMessage());
                        }
                    });
                }

            }
            if (requestCode == VerificationCallback.TYPE_PROFILE_VERIFIED_BEFORE) {
                Log.d("truecaller_request", "onRequestSuccess: TYPE_PROFILE_VERIFIED_BEFORE");
            }

        }

        @Override
        public void onRequestFailure(final int requestCode, @NonNull final TrueException e) {
            Log.d("truecaller_error", "onRequestFailure: "+e.getExceptionMessage());
            toast(e.getExceptionMessage());
        }

    };

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void toast(String msg){
        Toast.makeText(TruecallerActivity.this, msg, Toast.LENGTH_SHORT).show();
    }


}
