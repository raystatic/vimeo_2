package com.example.vimeo;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static android.content.Context.MODE_PRIVATE;

public class DownloadWorker extends Worker {

    private LiveDataHelper liveDataHelper;

    public DownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        liveDataHelper = LiveDataHelper.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        try{

//            try {
//                File path= new File(getApplicationContext().getFilesDir(), "VimeoApp" + File.separator + "Videos");
//                if(!path.exists()){
//                    path.mkdirs();
//                }
//                File outFile = new File(path, "samplefile" + ".txt");
//                //now we can create FileOutputStream and write something to file
//                FileOutputStream fos = new FileOutputStream(outFile);
//                String data = "test data";
//                fos.write(data.getBytes());
//                fos.close();
//                Log.e("file_saving", "Saving received message path"+outFile.getAbsolutePath());
//            } catch (FileNotFoundException e) {
//                Log.e("file_saving", "Saving received message failed with", e);
//            } catch (IOException e) {
//                Log.e("file_saving", "Saving received message failed with", e);
//            }

//            ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
//            File directory = contextWrapper.getDir(getApplicationContext().getFilesDir().getName(), Context.MODE_PRIVATE);
//            File file =  new File(directory ,"samplefile");
//            String data = "TEST DATA";
//            FileOutputStream fileout=openFileOutput("mytextfile.txt", MODE_PRIVATE);
//            //FileOutputStream fos = openFileOutput("samplefile.txt", MODE_PRIVATE); // save
//            fos.write(data.getBytes());
//            fos.close();

            ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
            File directory = contextWrapper.getDir(getApplicationContext().getFilesDir().getName(), Context.MODE_APPEND);
            if (!directory.exists()){
                directory.mkdir();
            }
            File outputFile = new File(directory, "sample_video.mp4");
            URL url = new URL("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
            //URL url = new URL("https://inducesmile.com/wp-content/uploads/2019/01/inducesmilelog.png");
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            int fileLength = urlConnection.getContentLength();
            FileOutputStream fos = new FileOutputStream(outputFile);
            InputStream inputStream = urlConnection.getInputStream();
            byte[] buffer = new byte[1024];
            int len1;
            long total = 0;
            while ((len1 = inputStream.read(buffer))>0){
                total += len1;
                int percentage = (int) ((total * 100)/ fileLength);
                liveDataHelper.updateDownloadPer(percentage);
                fos.write(buffer, 0, len1);
            }
            fos.close();
            inputStream.close();
            Log.d("videopath", "doWork: "+outputFile.getAbsolutePath());
            liveDataHelper.updateFilePath(outputFile.getAbsolutePath());

//            final byte[] buf = new byte[8192];
//            final Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
//            c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec("1234567890123456".getBytes(), "AES"), new IvParameterSpec(new byte[16]));
//            final InputStream is = new FileInputStream(outputFile.getAbsolutePath());
//            final OutputStream os = new CipherOutputStream(new FileOutputStream(outputFile.getAbsolutePath()), c);
//            while (true) {
//                int n = is.read(buf);
//                if (n == -1) break;
//                os.write(buf, 0, n);
//            }
//            os.close(); is.close();






            Log.d("videopath", "doWork: encrypted "+outputFile.getAbsolutePath());

        }catch (Exception e){
            e.printStackTrace();
            return Result.failure();
        }
        return Result.success();
    }

}
