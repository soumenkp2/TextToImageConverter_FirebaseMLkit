package com.example.textconverter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;


import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    ImageView image;
    TextView text, data;
    private Uri uri;

    String final_data = "";
    String prev= "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (ImageView) findViewById(R.id.image);
        text = (TextView) findViewById(R.id.text_camera);
        data = (TextView) findViewById(R.id.data);


        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String value = text.getText().toString();

                if(value.equals("Convert"))
                {
                    data.setVisibility(View.VISIBLE);
                    image.setVisibility(View.GONE);

                    try {
                        recognizeText();
                        //data.setText(final_data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    ImagePicker.with(MainActivity.this)
                            .crop()	    			//Crop image(Optional), Check Customization for more option
                            .compress(1024)			//Final image size will be less than 1 MB(Optional)
                            .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                            .start();
                }
            }
        });


    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            // Image Uri will not be null for RESULT_OK
            uri = data.getData();

            // Use Uri object instead of File to avoid storage permissions
            image.setImageURI(uri);
            text.setText("Convert");

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void recognizeText() throws IOException {
        if(uri!=null)
        {
            try {
                InputImage image = InputImage.fromBitmap(uriToBitmap(uri),0);
                TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text text) {
                        StringBuilder result = new StringBuilder();
                        for (Text.TextBlock block : text.getTextBlocks()) {
                            String blockText = block.getText();
                            Point[] blockCornerPoints = block.getCornerPoints();
                            Rect blockFrame = block.getBoundingBox();
                            for (Text.Line line : block.getLines()) {
                                String lineText = line.getText();
                                Point[] lineCornerPoints = line.getCornerPoints();
                                Rect lineFrame = line.getBoundingBox();
                                for (Text.Element element : line.getElements()) {
                                    String elementText = element.getText();
                                    result.append(elementText);
                                }

                                if(blockText!=prev)
                                {
                                    final_data += blockText;
                                    prev = blockText;
                                }

                                data.setText(final_data);
                                //Toast.makeText(getApplicationContext(),blockText,Toast.LENGTH_LONG).show();

                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

        }
    }

    public Bitmap uriToBitmap(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();
        return bitmap;
    }



}