package com.neverworker.oknow.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class FileManager {
	private static String localPath;
	public static void Initial(Activity thisActivity) {
		localPath = thisActivity.getFilesDir().getPath();
		File nomediaFile = new File(localPath + "/.nomedia");
		if(!nomediaFile.exists()) {
			FileOutputStream fout;
			try {
				fout = new FileOutputStream(nomediaFile);
				fout.write(0x00);
				fout.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static boolean Exist(String filename) {
		return new File(localPath + "/" + filename).exists();
	}
	
	public static void SaveImage(String filename, Bitmap bmp) {
		FileOutputStream out = null;
		try {
		    out = new FileOutputStream(localPath + "/" + filename);
		    bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (Exception e) {
		    e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (Throwable ignore) {
			}
		}
	}
	
	public static Bitmap LoadImage(String filename) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap bitmap = BitmapFactory.decodeFile(localPath + "/" + filename, options);
		return bitmap;
	}

    public static String ReadInputStreamToStr(InputStream fileIs, String encoding) {
        StringBuilder dataStr = new StringBuilder();

        try {
            BufferedReader bw = new BufferedReader(new InputStreamReader(fileIs, encoding));
            String lineData;
            while ((lineData = bw.readLine()) != null) {
                dataStr.append(lineData + "\n");
            }
            bw.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }

        return dataStr.toString();
    }
}
