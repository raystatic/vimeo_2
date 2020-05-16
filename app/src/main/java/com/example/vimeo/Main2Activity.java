package com.example.vimeo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Main2Activity extends AppCompatActivity {

    private Button downloadBtn;
    private ProgressBar progressBar;
    private TextView progress_tv;

    String path = "";

    SecretKey key;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        KeyGenerator kgen = null;
        try {
            kgen = KeyGenerator.getInstance("AES");
            key = kgen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progress_tv = (TextView) findViewById(R.id.progress_tv);
        downloadBtn = findViewById(R.id.download);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWork();
            }
        });
        findViewById(R.id.encrypt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                encryptFile();
            }
        });

        findViewById(R.id.decrypt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main2Activity.this, VideoActivity.class);
                intent.putExtra("path", path);
                startActivity(intent);
//                decryptFile();
            }
        });

    }

    private void decryptFile() {
        if (!TextUtils.isEmpty(path)){
            try{
                File outfile = new File("/data/user/0/com.example.vimeo/app_files/sample_video.mp4");
                int read;
                if(!outfile.exists())
                    outfile.createNewFile();
                File decfile = new File("/data/user/0/com.example.vimeo/app_files/sample_video.mp4");
                if(!decfile.exists())
                    decfile.createNewFile();
                FileInputStream encfis = new FileInputStream(outfile);
                FileOutputStream decfos = new FileOutputStream(decfile);
                Cipher decipher = Cipher.getInstance("AES");
                //Lgo
                decipher.init(Cipher.DECRYPT_MODE, key);
                CipherOutputStream cos = new CipherOutputStream(decfos,decipher);
                while((read=encfis.read())!=-1)
                {
                    cos.write(read);
                    cos.flush();
                }
                cos.close();
                Log.d("videopath", "doWork: decrypted "+decfile.getAbsolutePath()+"\n"+outfile.getAbsolutePath());

            }catch (FileNotFoundException e){

            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
        }
    }

    private void encryptFile() {

        if (!TextUtils.isEmpty(path)){
            try{
                FileInputStream fis = new FileInputStream(new File(path));
                File outfile = new File(path);
                int read;
                if(!outfile.exists())
                    outfile.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(outfile);
                Cipher encipher = Cipher.getInstance("AES");
                encipher.init(Cipher.ENCRYPT_MODE, key);
                CipherInputStream cis = new CipherInputStream(fis, encipher);
                while((read = cis.read())!=-1)
                {
                    fileOutputStream.write((char)read);
                    fileOutputStream.flush();
                }
                fileOutputStream.close();
                Log.d("videopath", "doWork: encrypted "+outfile.getAbsolutePath());

            }catch (FileNotFoundException e){

            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
        }
    }

    private void startWork(){
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresStorageNotLow(true)
                .build();
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(DownloadWorker.class)
                .setConstraints(constraints).build();;
        WorkManager.getInstance(this).enqueue(oneTimeWorkRequest);
        LiveDataHelper.getInstance().observePercentage().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                progressBar.setProgress(integer);
                progress_tv.setText(integer+"/100");
            }
        });

        LiveDataHelper.getInstance().observeFilePath().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (!TextUtils.isEmpty(s)){
                    path = s;
                }
            }
        });
    }
}
