package com.example.vision_jy.redeyereomove2;

        import android.app.Activity;
        import android.content.Intent;
        import android.os.Environment;
        import android.provider.MediaStore;
        import android.support.v7.app.AppCompatActivity;
        import android.graphics.Bitmap;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.Toast;

        import org.opencv.core.Core;

        import java.io.File;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.text.SimpleDateFormat;
        import java.util.Date;

public class MainActivity extends AppCompatActivity {

    int numFace=0;
    int nlocalThresh=0;
    boolean bSucSingle=false;
    boolean bSucDouble=false;
    final int REQ_CODE_SELECT_IMAGE=100;

    ImageView iv;
    Bitmap bmp;
    RedeyeFunction_jy redeye;

    static {
        System.loadLibrary("opencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RedeyeFunction_jy redeye = new RedeyeFunction_jy(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onRedeyeRemove(View view) {

        ImageView iv = (ImageView) findViewById(R.id.faceImage);
        RedeyeFunction_jy redeye = new RedeyeFunction_jy(this);

        redeye.MakeImage(bmp);
        redeye.FindFace();

        numFace = redeye.nFaceCnt;

        RedeyeFunction_jy[] ClassFace =  new RedeyeFunction_jy[numFace];

        for(int i =0; i< numFace; i++)
            ClassFace[i] = new RedeyeFunction_jy(this);

        if (numFace == 0) {
            Log.i("TAG", "Not Found Face");
        }
        else {
            for(int i=0; i< numFace; i++) {
                ClassFace[i].copyFace(redeye, i);

                Log.i("TAG", "ClassFace[i]'s Height" + ClassFace[i].nH);
                Log.i("TAG", "ClassFace[i]'s Width"+ClassFace[i].nW);

                do {
                    ClassFace[i].Redness(nlocalThresh);

                    bSucSingle = ClassFace[i].DetectEye_single();

                    if(bSucSingle==true) {
                        bSucDouble = ClassFace[i].DetectEye_double();
                    }

                    if(bSucSingle==false || bSucDouble==false) {
                        Log.i("TAG", "Operate nlocalThresh + 2 ");
                        nlocalThresh += 10;
                    }
                }
                while(bSucSingle==false||bSucDouble==false);
                ClassFace[i].CorrectRedEye();
            }

            redeye.CorrectFace(ClassFace,numFace);
            Toast.makeText(getBaseContext(), " Success Redeye Removal ", Toast.LENGTH_SHORT).show();

            bmp = redeye.MakeBmp();
            iv.setImageBitmap(bmp);
        }
    }

    public void onTakeImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(getBaseContext(), "resultCode : "+resultCode, Toast.LENGTH_SHORT).show();

        if(requestCode == REQ_CODE_SELECT_IMAGE)
        {
            if(resultCode== Activity.RESULT_OK)
            {
                try {
                    //Uri에서 이미지 이름을 얻어온다.
                    //String name_Str = getImageNameToUri(data.getData());

                    //이미지 데이터를 비트맵으로 받아온다.
                    bmp 	= MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    ImageView image = (ImageView)findViewById(R.id.faceImage);

                    //배치해놓은 ImageView에 set
                    image.setImageBitmap(bmp);

                    //Toast.makeText(getBaseContext(), "name_Str : "+name_Str , Toast.LENGTH_SHORT).show();


                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onFindFace(View view) {
        ImageView iv = (ImageView) findViewById(R.id.faceImage);
        Bitmap Bmp_CheckFace;
        RedeyeFunction_jy redeye = new RedeyeFunction_jy(this);

        redeye.MakeImage(bmp);
        Bmp_CheckFace=redeye.CheckFace();

        iv.setImageBitmap(Bmp_CheckFace);
    }

    public void onSaveImage(View view) {

        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d("TAG", "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("TAG", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("TAG", "Error accessing file: " + e.getMessage());
        }

        Toast.makeText(getBaseContext(), " Success Save ", Toast.LENGTH_SHORT).show();
    }

    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/DCIM/FaceSave");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }
}