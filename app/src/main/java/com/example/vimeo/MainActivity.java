package com.example.vimeo;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.vimeo.networking.VimeoClient;
import com.vimeo.networking.callbacks.AuthCallback;
import com.vimeo.networking.callbacks.ModelCallback;
import com.vimeo.networking.callbacks.VimeoCallback;
import com.vimeo.networking.model.User;
import com.vimeo.networking.model.Video;
import com.vimeo.networking.model.VideoList;
import com.vimeo.networking.model.error.VimeoError;
import com.vimeo.networking.model.playback.Play;

import okhttp3.CacheControl;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    public static final String TAG = "Vimeo_Networking_Sample";
    private static final int PERMISSION_REQUEST_CODE = 200;

    public static final String STAFF_PICKS_VIDEO_URI = "/channels/927/videos"; // 927 == staffpicks

    private final VimeoClient mApiClient = VimeoClient.getInstance();
    private ProgressDialog mProgressDialog;

    private TextView mRequestOutputTv;

    private TextView mStaffPicksRequestTime;

    Video myVideo = null;

    // <editor-fold desc="Life Cycle">

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkPermission()) {
            openActivity();
        } else {
            if (checkPermission()) {
                requestPermissionAndContinue();
            } else {
                openActivity();
            }
        }

        // ---- Initial UI Setup ----
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("All of your API are belong to us...");

        // ---- Code Grant Check ----
        handleCodeGrantIfNecessary();

        // ---- Client Credentials Auth ----
        if (mApiClient.getVimeoAccount().getAccessToken() == null) {
            // If there is no access token, fetch one on first app open
            authenticateWithClientCredentials();
        }

        // ---- View Binding ----
        mRequestOutputTv = findViewById(R.id.request_output_tv);
        mStaffPicksRequestTime = findViewById(R.id.staff_picks_request_time);
       // findViewById(R.id.fab).setOnClickListener(this);
        findViewById(R.id.code_grant_btn).setOnClickListener(this);
        findViewById(R.id.request_output_tv).setOnClickListener(this);
        findViewById(R.id.staff_picks_gson_btn).setOnClickListener(this);
        findViewById(R.id.staff_picks_moshi_btn).setOnClickListener(this);
        findViewById(R.id.account_type_btn).setOnClickListener(this);
        findViewById(R.id.logout_btn).setOnClickListener(this);
        findViewById(R.id.my_videos_btn).setOnClickListener(this);
        findViewById(R.id.change_activity_btn).setOnClickListener(this);
    }

    private boolean checkPermission() {

        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ;
    }

    private void requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Permission required");
                alertBuilder.setMessage("Permission required");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE
                                , READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
                Log.e("", "permission denied, show dialog");
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        } else {
            openActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (permissions.length > 0 && grantResults.length > 0) {

                boolean flag = true;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        flag = false;
                    }
                }
                if (flag) {
                    openActivity();
                } else {
                    finish();
                }

            } else {
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void openActivity() {
        //add your further process after giving permission or to download images from remote server.
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.staff_picks_gson_btn:
                fetchStaffPicksWithGson();
                break;
            case R.id.staff_picks_moshi_btn:
                fetchStaffPicksWithMoshi();
                break;
            case R.id.account_type_btn:
                fetchAccountType();
                break;
            case R.id.logout_btn:
                logout();
                break;
            case R.id.code_grant_btn:
//            case R.id.fab:
//                toast("Authenticate on Web");
//                goToWebForCodeGrantAuth();
//                break;
            case R.id.my_videos_btn:
                getMyVideos();
                break;

            case R.id.play_videos_btn :
                playMyVideo();
                break;

            case R.id.donload_videos_btn:
                downloadVideo();
                break;

            case R.id.change_activity_btn:
                startActivity(new Intent(MainActivity.this, Main2Activity.class));
        }
    }

    private void downloadVideo() {
        if (myVideo!=null){

        }
    }

    private void playMyVideo() {
        if (myVideo!=null){
            String html = myVideo.embed != null ? myVideo.embed.html : null;
            if(html != null) {
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                intent.putExtra("data", html);
                startActivity(intent);
            }
        }
    }



    private void getMyVideos() {
        mProgressDialog.show();
        String uri = "/me/videos"; // obtained using one of the above methods
        VimeoClient.getInstance().fetchNetworkContent(uri, new ModelCallback<VideoList>(VideoList.class) {
            @Override
            public void success(VideoList videoList) {
                if (videoList != null && videoList.data != null && !videoList.data.isEmpty()) {
                    for (Video video : videoList.data){
                        mRequestOutputTv.setText(video.name+"\n");
                    }
                    myVideo =  videoList.data.get(0);
                }
                toast("My Videos Success ");
                mProgressDialog.hide();
            }

            @Override
            public void failure(VimeoError error) {
                // voice the error
                toast("My videos Failure");
                mRequestOutputTv.setText(error.getDeveloperMessage());
                mProgressDialog.hide();
            }
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    // </editor-fold>

    // <editor-fold desc="Requests">

    private void fetchStaffPicksWithGson() {
        final long initialTime = System.currentTimeMillis();
        mProgressDialog.show();
        mApiClient.fetchContent(STAFF_PICKS_VIDEO_URI, CacheControl.FORCE_NETWORK, new ModelCallback<VideoList>(VideoList.class) {
            @Override
            public void success(VideoList videoList) {
                final long finalTime = System.currentTimeMillis();
                final long totalLoadTime = finalTime - initialTime;

                if (videoList != null && videoList.data != null) {
                    String videoTitlesString = "";
                    boolean addNewLine = false;
                    for (Video video : videoList.data) {
                        if (addNewLine) {
                            videoTitlesString += "\n";
                        }
                        addNewLine = true;
                        videoTitlesString += video.name;
                    }
                    mRequestOutputTv.setText(videoTitlesString);
                }
                mStaffPicksRequestTime.setText(getString(R.string.request_time, "Gson", totalLoadTime));

                toast("Staff Picks Success ");
                mProgressDialog.hide();
            }

            @Override
            public void failure(VimeoError error) {
                toast("Staff Picks Failure");
                mRequestOutputTv.setText(error.getDeveloperMessage());
                mProgressDialog.hide();
            }
        });
    }

    private void fetchStaffPicksWithMoshi() {
        final long initialTime = System.currentTimeMillis();
        mProgressDialog.show();
        mApiClient.fetchContent(STAFF_PICKS_VIDEO_URI, CacheControl.FORCE_NETWORK,  new ModelCallback<VideoList>(VideoList.class) {
            @Override
            public void success(VideoList videoList) {
                final long finalTime = System.currentTimeMillis();
                final long totalLoadTime = finalTime - initialTime;

                if (videoList != null && videoList.data != null) {
                    String videoTitlesString = "";
                    boolean addNewLine = false;
                    for (Video video : videoList.data) {
                        if (addNewLine) {
                            videoTitlesString += "\n";
                        }
                        addNewLine = true;
                        videoTitlesString += video.name;
                    }
                    mRequestOutputTv.setText(videoTitlesString);
                }
                mStaffPicksRequestTime.setText(getString(R.string.request_time, "Moshi", totalLoadTime));
                toast("Staff Picks Success");
                mProgressDialog.hide();
            }

            @Override
            public void failure(VimeoError error) {
                toast("Staff Picks Failure");
                mRequestOutputTv.setText(error.getDeveloperMessage());
                mProgressDialog.hide();
            }
        });
    }

    private void fetchAccountType() {
        mProgressDialog.show();
        mApiClient.fetchCurrentUser(new ModelCallback<User>(User.class) {
            @Override
            public void success(User user) {
                if (user != null) {
                    mRequestOutputTv.setText("Current account type: " + user.account);
                    toast("Account Check Success");
                } else {
                    toast("Account Check Failure");
                }
                mProgressDialog.hide();
            }

            @Override
            public void failure(VimeoError error) {
                toast("Account Check Failure");
                mRequestOutputTv.setText(error.getDeveloperMessage());
                mProgressDialog.hide();
            }
        });
    }

    private void logout() {
        mProgressDialog.show();
        mApiClient.logOut(new VimeoCallback<Object>() {
            @Override
            public void success(Object o) {
                AccountPreferenceManager.removeClientAccount();
                toast("Logout Success");
                mProgressDialog.hide();
            }

            @Override
            public void failure(VimeoError error) {
                AccountPreferenceManager.removeClientAccount();
                toast("Logout Failure");
                mRequestOutputTv.setText(error.getDeveloperMessage());
                mProgressDialog.hide();
            }
        });
    }

    // You can't make any requests to the api without an access token. This will get you a basic
    // "Client Credentials" gran which will allow you to make requests
    private void authenticateWithClientCredentials() {
        mProgressDialog.show();
        mApiClient.authorizeWithClientCredentialsGrant(new AuthCallback() {
            @Override
            public void success() {
                toast("Client Credentials Authorization Success");
                mProgressDialog.hide();
            }

            @Override
            public void failure(VimeoError error) {
                toast("Client Credentials Authorization Failure");
                mRequestOutputTv.setText(error.getDeveloperMessage());
                mProgressDialog.hide();
            }
        });
    }

    private void authenticateWithCodeGrant(Uri uri) {
        mProgressDialog.show();
        if (uri.getQuery() == null || uri.getQuery().isEmpty()) {
            toast("Bad deep link - no query parameters");
            return;
        }
        mApiClient.authenticateWithCodeGrant(uri.toString(), new AuthCallback() {
            @Override
            public void success() {
                toast("Code Grant Success");
                mProgressDialog.hide();
            }

            @Override
            public void failure(VimeoError error) {
                toast("Code Grant Failure");
                mRequestOutputTv.setText(error.getDeveloperMessage());
                mProgressDialog.hide();
            }
        });
    }

    // </editor-fold>

    // <editor-fold desc="Code Grant">

    // We deep link to this activity as specified in the AndroidManifest.
    private void handleCodeGrantIfNecessary() {
        if (getIntent() != null) {
            String action = getIntent().getAction();
            Uri uri = getIntent().getData();
            if (Intent.ACTION_VIEW.equals(action) && uri != null) {
                // This is coming from a deep link
                authenticateWithCodeGrant(uri);
            }
        }
    }

    private void goToWebForCodeGrantAuth() {
        String uri = mApiClient.getCodeGrantAuthorizationURI();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(browserIntent);
    }

    // </editor-fold>

    // <editor-fold desc="Helpers">

    private void toast(String string) {
        Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
    }

    /*
    // Can only fetch quota if you have the upload privilege
    private void fetchAccountType() {
        mProgressDialog.show();
        mApiClient.fetchCurrentUser(new ModelCallback<User>(User.class) {
            @Override
            public void success(User user) {
                long fileSizeBytes = user.getFreeUploadSpace();
                if (user.getFreeUploadSpace() != Vimeo.NOT_FOUND) {
                    String formattedFileSize =
                            Formatter.formatShortFileSize(TestApp.getAppContext(), fileSizeBytes);
                    mRequestOutputTv.setText("Available Space: " + formattedFileSize);
                    toast("Quote Check Success");
                } else {
                    toast("Quote Check Failure");
                }
                mProgressDialog.hide();
            }

            @Override
            public void failure(VimeoError error) {
                mRequestOutputTv.setText(error.getDeveloperMessage());
                mProgressDialog.hide();
            }
        });
    }
    */

    // </editor-fold>
}
