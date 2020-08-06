package com.example.meaning4;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.common.io.ByteStreams;
import com.microsoft.azure.cognitiveservices.vision.computervision.*;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import static com.example.meaning4.R.id.bottomSheet;

public class findMeaning extends AppCompatActivity {

    String subscriptionKey = "4ca7b53995e041cbab318cbde301835e";
    String endpoint ="https://dictdetection.cognitiveservices.azure.com/";
    public String statusBarHeight = "";
    List<List<Integer>> listOfLists = new ArrayList<>();
    List<String> listOfWords = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_meaning);
        Button button = findViewById(R.id.button98);
        statusBarHeight = String.valueOf(getStatusBarHeight());
        ImageView imageView = findViewById(R.id.image98);
        int[] viewCoords = new int[2];
        imageView.getLocationOnScreen(viewCoords);
        Log.i("coords", viewCoords[0]+"   "+viewCoords[1]);
        wordAPI i = new wordAPI();

        View bottomSheet = findViewById(R.id.bottomSheet);
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        try {
            String x = i.execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                ComputerVisionClient compVisClient = ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint);
                Log.i("a", "\nAzure Cognitive Services Computer Vision - Java Quickstart Sample");
                RecognizeTextOCRLocal(compVisClient);
                return null;
            }
        };
        task.execute();





    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int x = (int)event.getX();
        int y = (int) event.getY();
        y -= getStatusBarHeight();
        int i = 1;
        for(int k = 0; k < listOfLists.size(); k++){
            if(x > listOfLists.get(k).get(0) && x < listOfLists.get(k).get(2) ){
                if(y > listOfLists.get(k).get(1) && y < listOfLists.get(k).get(3) ){
                    Log.i("index", String.valueOf(k)+" "+i);
                    TextView choosenWord = findViewById(R.id.choosenWord);
                    choosenWord.setText(listOfWords.get(k));
                    i+=1;

                    break;
                }
            }
        }

        switch (event.getAction()) {

            case MotionEvent.TOOL_TYPE_MOUSE:
        }

        return false;
    }

    public void RecognizeTextOCRLocal(ComputerVisionClient client) {
        Log.i("a", "-----------------------------------------------");
        Log.i("a", "RECOGNIZE PRINTED TEXT");

        // Replace this string with the path to your own image.
        Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.ggg);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        try {
            byte[] localImageBytes = ByteStreams.toByteArray(inputStream);

            // Recognize printed text in local image
            OcrResult ocrResultLocal = client.computerVision().recognizePrintedTextInStream()
                    .withDetectOrientation(true).withImage(localImageBytes).withLanguage(OcrLanguages.EN).execute();
            Log.i("a", "\n");
            Log.i("a", "Recognizing printed text from a local image with OCR ...");
            Log.i("a", "\nLanguage: " + ocrResultLocal.language());
            Log.i("a", "Text angle: %1.3f\n"+ ocrResultLocal.textAngle());
            Log.i("a", "Orientation: " + ocrResultLocal.orientation());


            boolean firstWord = true;
            String res = "";
            for (OcrRegion reg : ocrResultLocal.regions()) {

                for (OcrLine line : reg.lines()) {
                    for (OcrWord word : line.words()) {
                        List<Integer> innerList = new ArrayList<>();
                        String[] st = word.boundingBox().split(",");
                        innerList.add(Integer.valueOf(st[0]));
                        innerList.add(Integer.valueOf(st[1]));
                        innerList.add(Integer.valueOf(st[0]) + Integer.valueOf(st[2]));
                        innerList.add(Integer.valueOf(st[1]) + Integer.valueOf(st[3]));
                        listOfLists.add(innerList);
                        if (firstWord) {
                            Log.i("a", "\nFirst word in first line is \"" + word.text()
                                    + "\" with  bounding box: " + word.boundingBox());
                            firstWord = false;
                            Log.i("a", "\n");
                        }
                        Log.i("a", word.text() + " ");
                        listOfWords.add(word.text());
                        res += word.text() + " ";
                    }
                    Log.i("a", "\n");
                }
            }
        }catch (Exception e){
            Log.i("a", e.toString());
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }

        return result;
    }

}
