package com.android.factory;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
/* zouguanbo 20150715 add for Jria LM-49 when sdswap is disable need change storage path */
import com.mediatek.storage.StorageManagerEx;

public class SDCardTesting extends Activity{
    TextView textView;
    private StorageManager mStorageManager ;
    private String mStoragePath[] = new String[]{"/storage/sdcard0","/storage/sdcard1"};
    private int mPhoneIndex = 0;
    private int mSDCardIndex = 1;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    setContentView(R.layout.sdcard_testing);
	    
		/* zouguanbo 20150715 add begin for Jria LM-49 when sdswap is disable need change storage path */
		if(StorageManagerEx.getSdSwapState()){
	    		mStoragePath[0] = "/storage/sdcard0";
				mStoragePath[1] = "/storage/sdcard1";
	  	}else{
	    		mStoragePath[0] = "/storage/sdcard1";
				mStoragePath[1] = "/storage/sdcard0";
	  	}
		/* zouguanbo 20150715 add end for Jria LM-49 when sdswap is disable need change storage path */
		/* zouguanbo 20150727 add begin for Jria TCOLOMBIA-17 when don't have sd change mStoragePath */
		if(!isSDMounted()){
	    		mStoragePath[0] = "/storage/sdcard0";
				mStoragePath[1] = "/storage/sdcard1";
		}
		/* zouguanbo 20150727 add end for Jria TCOLOMBIA-17 when don't have sd change mStoragePath */
	    initStoragePath();
		textView = (TextView)findViewById(R.id.comm_info);
	    String str = getInfo("cat /proc/driver/nand");
	    textView.setText(str);
    }

	//starmen add for SDCard begin
	public static boolean isSDCardExist(){
		/*hetian modify start for OE-24 catch exception 20160114*/
		try {
			StatFs sdStatFs = new StatFs("/storage/sdcard1"); 
			Log.d("Starmen", "sdStatFs.getBlockCount()*sdStatFs.getBlockSize() = " + sdStatFs.getBlockCount()*sdStatFs.getBlockSize());
			if (0==(sdStatFs.getBlockCount()*sdStatFs.getBlockSize())){
				return false;
			}else{
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		/*hetian modify end for OE-24 catch exception 20160114*/
	}
	//starmen add for SDCard end
    /**
     * This method checks whether SDcard is mounted or not
     * 
     * @param mountPoint the mount point that should be checked
     * @return true if SDcard is mounted, false otherwise
     */
    private boolean isMounted(String mountPoint) {
        Log.d("shihaijun", "isMounted, mountPoint = " + mountPoint);
        if (TextUtils.isEmpty(mountPoint)) {
            return false;
        }
        String state = null;
        
        state = mStorageManager.getVolumeState(mountPoint);
        Log.d("shihaijun", "state = " + state);
        return Environment.MEDIA_MOUNTED.equals(state);
    }
    /* zouguanbo 20150727 add begin for Jria TCOLOMBIA-17 when don't have sd change mStoragePath */
    private boolean isSDMounted() {
    	/*hetian modify start for OE-24 catch exception 20160114*/
			try {
        StatFs localStatFs = new StatFs("/storage/sdcard1");
        long l1 = localStatFs.getBlockCount();
        long l2 = localStatFs.getBlockSize();
        long l4 = l1 * l2 / 1024L / 1024L;
        if(l4 == 0){
        		return false;
        }else{
        		return true;
        }
      } catch (Exception e) {
				return false;
			}
			/*hetian modify end for OE-24 catch exception 20160114*/
    }
    /* zouguanbo 20150727 add end for Jria TCOLOMBIA-17 when don't have sd change mStoragePath */
    
    private void initStoragePath(){
    	 mStorageManager = (StorageManager)getSystemService(Context.STORAGE_SERVICE);
    	
    	StorageVolume[] sv = mStorageManager.getVolumeList();
    	for(int i=0;i<sv.length;i++){
    		
    		StorageVolume vol = sv[i];
    		if(isMounted(vol.getPath())){
    			mPhoneIndex = i;
    		}
    	}
    	
    	mSDCardIndex = 1-mPhoneIndex;
    	
    }
  private String getInfo(String paramString)
  {
      StringBuilder sb = new StringBuilder();
      long blockSize = 0;
      long blockCount = 0;
      long availCount = 0;
      long totalSize = 0;
      long availSize = 0;
      double totalSize1;
      double availSize1;
      int a;
    //Tim add for TI-15
    try{
    //Tim add end
      
     StatFs rootsf = new StatFs(mStoragePath[mPhoneIndex]); 
     
      blockSize = rootsf.getBlockSize();  
      blockCount = rootsf.getBlockCount();  
      availCount = rootsf.getAvailableBlocks(); 
      totalSize = blockCount * blockSize / 1024L;
      availSize = availCount * blockSize / 1024L;
totalSize1=(double)(totalSize/1024.0);
a=(int)(totalSize1*100.0);
totalSize1=(double)(a/100.0);
availSize1=(double)(availSize/1024.0);
a=(int)(availSize1*100.0);
availSize1=(double)(a/100.0);
//DecimalFormat df = new DecimalFormat("0.00");
//totalSize1 = df.format(totalSize1);
//availSize1 = df.format(availSize1);
      sb.append(this.getString(R.string.internal_memory)).append("\n\n");
      sb.append(this.getString(R.string.sdcard_totalsize)).append(totalSize1).append("MB").append("\n\n");
      sb.append(this.getString(R.string.sdcard_freesize)).append(availSize1).append("MB").append("\n\n");



//	        StringBuilder localStringBuilder = new StringBuilder();

//          StatFs localStatFs = new StatFs("/storage/sdcard1");
          /* zouguanbo 20160330 add for when don't have sd card , it will be exception */
          try{
          StatFs localStatFs = new StatFs(mStoragePath[mSDCardIndex]);
          long l1 = localStatFs.getBlockCount();
              long l2 = localStatFs.getBlockSize();
              long l3 = localStatFs.getAvailableBlocks();
              long l4 = l1 * l2 / 1024L / 1024L;
              long l5 = l3 * l2 / 1024L / 1024L;
              double l6 =(double)(l4/1024.0);
               a=(int)(l6*100.0);
               l6=(double)(a/100.0);
              double l7 = (double)(l5/1024.0);
              a=(int)(l7*100.0);
              l7=(double)(a/100.0);
//l6 = df.format(l6);
//l7 = df.format(l7);
          if(l4 == 0){
              sb.append(this.getString(R.string.sdcard_tips_failed));
          }else{

              sb.append(this.getString(R.string.sdcard_tips_success)).append("\n\n");
              sb.append("SDcard ").append(this.getString(R.string.sdcard_totalsize)).append(l6).append("GB").append("\n\n");
              sb.append("SDcard ").append(this.getString(R.string.sdcard_freesize)).append(l7).append("GB").append("\n\n");
          }
          /* zouguanbo 20160330 add begin for when don't have sd card , it will be exception */
          }catch (IllegalArgumentException e) {
          		sb.append(this.getString(R.string.sdcard_tips_failed));
          }
          /* zouguanbo 20160330 add end for when don't have sd card , it will be exception */
      return sb.toString();
     //Tim add for TI-15
    }catch (IllegalArgumentException e) {
    	Log.v("zouguanbo","e = " + e);
      return this.getString(R.string.mounting_memery_devices);
    }
     //Tim add end
  }
    
}
