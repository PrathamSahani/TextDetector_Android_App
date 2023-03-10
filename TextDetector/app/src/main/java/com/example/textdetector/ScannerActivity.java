package com.example.textdetector;

import static android.Manifest.permission_group.CAMERA;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.security.PrivateKey;

public class ScannerActivity extends AppCompatActivity {

    private ImageView captureIV;
    private TextView resultTV;
    private Button snapBtn, detectBtn;
    private Bitmap imageBitmap;
    static  final int  REQUEST_IMAGE_CAPTURE=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        captureIV = findViewById(R.id.idTVCaptureImage);
        resultTV = findViewById(R.id.idTVDetectedText);
        snapBtn = findViewById(R.id.idBtnSnap);
        detectBtn = findViewById(R.id.idBtnDetect);

        detectBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                detectText();

            }
        });
        snapBtn.setOnClickListener(new View.OnClickListener(){
             @Override
            public void onClick(View v){
                     if(checkPermissions()){
                         captureImage();
                     }else{
                         requestPermission();
                     }
             }
        });
    }
    private  boolean checkPermissions(){
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission(){
         int PERMISSION_CODE = 200;
         ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PERMISSION_CODE);
    }
    private  void captureImage(){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePicture.resolveActivity(getPackageManager())!=null){
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
        }

    }

    @Override
    public  void onRequestPermissionsResult(int requestCode , String[] permission, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permission, grantResults);
        if(grantResults.length>0){
            boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if(cameraPermission){
                Toast.makeText(this, "Permission denied..", Toast.LENGTH_SHORT).show();

            }else{
                Toast.makeText(this, "Permission  Granted..", Toast.LENGTH_SHORT).show();
                captureImage();
            }
        }
    }


    @Override
    protected  void onActivityResult(int requestCode, int resultCode, @Nullable  Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            captureIV.setImageBitmap(imageBitmap);
        }
    }

    private  void detectText(){
        InputImage image = InputImage.fromBitmap( imageBitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
         Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
             @Override
             public void onSuccess( @NonNull Text text) {
                 StringBuilder result = new StringBuilder();
                for(Text.TextBlock block: text.getTextBlocks()){
                    String blockText = block.getText();
                    Point[] blocCornerPoint = block.getCornerPoints();
                    Rect blockFrame =  block.getBoundingBox();
                    for(Text.Line line : block.getLines()){
                        String lineTExt = line.getText();
                        Point[]  lineCornerPoint = line.getCornerPoints();
                        Rect lineRect = line.getBoundingBox();
                        for(Text.Element element: line.getElements()){
                            String elementText = element.getText();
                            result.append(elementText);
                        }
                        resultTV.setText(blockText);
                    }
                }

             }
         }).addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception e) {
                 Toast.makeText(ScannerActivity.this, "Fail to detect text from image.."+e.getMessage(), Toast.LENGTH_SHORT).show();
             }
         });
    }
}