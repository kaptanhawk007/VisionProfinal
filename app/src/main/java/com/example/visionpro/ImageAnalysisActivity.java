package com.example.visionpro;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import edmt.dev.edmtdevcognitivevision.Contract.AnalysisResult;
import edmt.dev.edmtdevcognitivevision.Contract.Caption;
import edmt.dev.edmtdevcognitivevision.Rest.VisionServiceException;
import edmt.dev.edmtdevcognitivevision.VisionServiceRestClient;

public class ImageAnalysisActivity extends AppCompatActivity {
    private static final String API_KEY="0e45995084ea4300b148222edd3da5c2";
    private static final String API_ENDPOINT="https://southeastasia.api.cognitive.microsoft.com/vision/v3.0";

    MaterialToolbar materialToolbar;
    MaterialButton btnSelectFile,btnGo,btnCamera;
    ImageView imageView;
    TextView TvCaption,TvConfidence;
    int SELECT_FILE=101,REQUEST_IMAGE_CAPTURE=121;
    Uri uri;
    TextToSpeech textToSpeech;
    private static final String TAG = "ImageAnalysisActivity";
    VisionServiceRestClient visionServiceRestClient=new VisionServiceRestClient(API_KEY,API_ENDPOINT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_analysis);
        init();
        btnSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,SELECT_FILE);
            }
        });
        textToSpeech();
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Uri"+uri);
                InputStream is = null;
                try {
                    is=getContentResolver().openInputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap= BitmapFactory.decodeStream(is);
                ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
                imageView.setImageBitmap(bitmap);
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                final ByteArrayInputStream inputStream=new ByteArrayInputStream(outputStream.toByteArray());

                @SuppressLint("StaticFieldLeak") AsyncTask<InputStream,String,String> visionTask=new AsyncTask<InputStream, String, String>() {
                    ProgressDialog progressDialog=new ProgressDialog(ImageAnalysisActivity.this);

                    @Override
                    protected void onPreExecute() {
                        progressDialog.show();
                    }

                    @Override
                    protected String doInBackground(InputStream... inputStreams) {
                        try {
                            publishProgress("Recognizing..");
                            String[] features={"Description"};
                            String[] details={};
                            AnalysisResult result=visionServiceRestClient.analyzeImage(inputStreams[0],features,details);


                            String jsonResult= new Gson().toJson(result);
                            Log.d(TAG, "doInBackground: "+ jsonResult);
                            return jsonResult;
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (VisionServiceException e) {
                            e.printStackTrace();
                        }
                        return " ";
                    }

                    @Override
                    protected void onPostExecute(String s) {

                        if (TextUtils.isEmpty(s)){
                            Toast.makeText(ImageAnalysisActivity.this, "Empty text got", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                        AnalysisResult result=new Gson().fromJson(s,AnalysisResult.class);
                        StringBuilder result_text=new StringBuilder();
                        StringBuilder confidenceFinal=new StringBuilder();
                        double confidence = 0;
                        for (Caption caption:result.description.captions){
                            result_text.append(caption.text);
                            confidence=(caption.confidence)*100;



                        }
                        String text_to_Speak;
                        if (confidence<70){
                            text_to_Speak="I am not sure about this but it looks like "+result_text.toString();
                        }
                        else {
                            text_to_Speak="It looks like "+result_text.toString();
                        }


                        TvCaption.setText(result_text.toString());
                        TvConfidence.setText(String.valueOf(confidence));
                        textToSpeech.speak(text_to_Speak,TextToSpeech.QUEUE_FLUSH,null);
                    }

                    @Override
                    protected void onProgressUpdate(String... values) {
                        progressDialog.setMessage(values[0]);
                    }
                };
                visionTask.execute(inputStream);
            }
        });
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ImageAnalysisActivity.this, "Opening Camera", Toast.LENGTH_SHORT).show();
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==SELECT_FILE&&resultCode==RESULT_OK){
            if (data!=null){
                uri=data.getData();
                imageView.setImageURI(uri);
            }
            else {
                Toast.makeText(this, "No File Select", Toast.LENGTH_SHORT).show();
            }

        }
        if (requestCode==REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);

        }
    }

    void textToSpeech(){
        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status!=TextToSpeech.ERROR){
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });
    }

    void init(){
        btnSelectFile=findViewById(R.id.btnSelectFile);
        btnGo=findViewById(R.id.btnGo);
        imageView=findViewById(R.id.imageView);
        TvCaption=findViewById(R.id.TvCaption);
        TvConfidence=findViewById(R.id.TvConfidence);
        btnCamera=findViewById(R.id.btnCamera);

    }
}