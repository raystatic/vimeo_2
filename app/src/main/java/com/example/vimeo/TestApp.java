package com.example.vimeo;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.example.vimeo.vimeonetworking.NetworkingLogger;
import com.example.vimeo.vimeonetworking.TestAccountStore;
import com.vimeo.networking.Configuration;
import com.vimeo.networking.Vimeo.LogLevel;
import com.vimeo.networking.VimeoClient;

/**
 * Created by kylevenn on 1/27/16.
 */
public class TestApp extends Application {

    private static final String SCOPE = "private public create edit delete interact";

    private static final boolean IS_DEBUG_BUILD = false;
    // Switch to true to see how access token auth works.
    private static final boolean ACCESS_TOKEN_PROVIDED = true;

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();

     //   Licensing.allow(getApplicationContext());

        sContext = this;
        AccountPreferenceManager.initializeInstance(sContext);

        // <editor-fold desc="Vimeo API Library Initialization">
        Configuration.Builder configBuilder;
        // This check is just as for the example. In practice, you'd use one technique or the other.
        if (ACCESS_TOKEN_PROVIDED) {
            configBuilder = getAccessTokenBuilder();
        } else {
            configBuilder = getClientIdAndClientSecretBuilder();
        }
        if (IS_DEBUG_BUILD) {
            // Disable cert pinning if debugging (so we can intercept packets)
            configBuilder.enableCertPinning(false);
            configBuilder.setLogLevel(LogLevel.VERBOSE);
        }
        configBuilder
                .setCacheDirectory(this.getCacheDir())
                .setUserAgentString(getUserAgentString(this))
                .setDebugLogger(new NetworkingLogger());
        VimeoClient.initialize(configBuilder.build());
        // </editor-fold>
    }

    public Configuration.Builder getAccessTokenBuilder() {
        // The values file is left out of git, so you'll have to provide your own access token
        String accessToken = "711ca0860eb7db747be23868e301d4a0";
        return new Configuration.Builder(accessToken);
    }

    public Configuration.Builder getClientIdAndClientSecretBuilder() {
        // The values file is left out of git, so you'll have to provide your own id and secret
        String clientId = "313de3b6f43a898cb12914c12f8d77cb38217e70";
        String clientSecret = "cPpLQ4QC+GMkPVm391eBr/zM8kRt9thgiiT0U1CKiv8UixJfZuKzcXWpN8HeDw7g5c3CLExpshd/6PTChafbwZeHNbJuh/9yYW20T5TiRCcJXOEnoOlL758nPz9ZMsTG";
        String codeGrantRedirectUri = getString(R.string.deeplink_redirect_scheme) + "://" +
                                      getString(R.string.deeplink_redirect_host);
        TestAccountStore testAccountStore = new TestAccountStore(this.getApplicationContext());
        Configuration.Builder configBuilder =
                new Configuration.Builder(clientId, clientSecret, SCOPE);
        configBuilder
                // Used for oauth flow
                .setCodeGrantRedirectUri(codeGrantRedirectUri);

        return configBuilder;
    }

    public static String getUserAgentString(Context context) {
        String packageName = context.getPackageName();

        String version = "unknown";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            System.out.println("Unable to get packageInfo: " + e.getMessage());
        }

        String deviceManufacturer = Build.MANUFACTURER;
        String deviceModel = Build.MODEL;
        String deviceBrand = Build.BRAND;

        String versionString = Build.VERSION.RELEASE;
        String versionSDKString = String.valueOf(Build.VERSION.SDK_INT);

        return packageName + " (" + deviceManufacturer + ", " + deviceModel + ", " + deviceBrand +
               ", " + "Android " + versionString + "/" + versionSDKString + " Version " + version +
               ")";
    }
}
