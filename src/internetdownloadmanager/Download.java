package internetdownloadmanager;

import java.io.*;
import java.net.*;
import java.util.*;
/**
 *
 * @author Ken
 */

//Download file from URL.
class Download extends Observable implements Runnable {
    
    //Max size of download buffer.
    private static final int MAX_BUFFER_SIZE = 1024;
    
    //Download status names.
    public static final String STATUSES[] = {"Downloading","Paused","Complete","Cancelled", "Error"};

    //Status codes.
    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETE = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;
    
    private URL url; //Download URL.
    private int size; //Download size in bytes.
    private int downloaded; //Downloaded size in bytes.
    private int status; //Current download status.
    
    //Constructor.
    public Download(URL url){
        this.url = url;
        size = -1;
        downloaded = 0;
        status = DOWNLOADING;
        
        //Begin downloading.
        download();
    }
    //Get the download URL.
    public String getUrl() {
        return url.toString();
    }
    //Get download size.
    public int getSize() {
        return size;
    }
    //Get download status.
    public int getStatus() {
        return status;
    }
    
    public float getProgress(){
        return ((float)downloaded / size) * 100;
    }
    
    //Pause download.
    public void pause(){
        status = PAUSED;
        stateChanged();
    }
    
    //Resume download.
    public void resume(){
        status = DOWNLOADING;
        stateChanged();
    }
    
    //Cancel download.
    public void cancel(){
        status = CANCELLED; 
        stateChanged();
    }
    
    //Mark download as having an error.
    private void error(){
        status = ERROR;
        stateChanged();
    }
    
    //Start or resume download.
    private void download(){
        Thread thread = new Thread(this);
        thread.start();
    }
    
    //Get file name portion of the URL.
    private String getFileName(URL url){
        String fileName = url.getFile();
        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }
    
    //Download the file.
    public void run(){
        RandomAccessFile file = null;
        InputStream stream = null;
        
        try{
            //Open a connection to the URL.
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            //Specify the file portion to be downloaded.
            connection.setRequestProperty("Range", "bytes = " + downloaded + " - ");
            
            //Connect to server.
            connection.connect();
            
            //Make sure the response code is in the 200 range.
            if (connection.getResponseCode()/100 != 2){
                error();
            }
            
            //Check for valid content length.
            int contentLength = connection.getContentLength();
            if (contentLength < 1){
                error();
            }
            
            //Change status to complete if downloading finished.
            if (status == DOWNLOADING){
                status = COMPLETE;
                stateChanged();
            }
        }catch (Exception e){
            error();
        }finally {
            //Close the file.
            if (file != null){
                try{
                    file.close();
                }catch (Exception e){}
            }
            //Close connection to the server.
            if (stream != null){
                try{
                    stream.close();
                }catch (Exception e){}
            }
        }
    }
    
    //Notify observers that this download's status has changed
    private void stateChanged() {
        setChanged();
        notifyObservers();
    }
    
}
