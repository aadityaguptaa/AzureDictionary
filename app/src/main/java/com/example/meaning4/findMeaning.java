package com.example.meaning4;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.loader.content.Loader;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.common.io.ByteStreams;
import com.microsoft.azure.cognitiveservices.vision.computervision.*;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.meaning4.R.id.bottomSheet;
import static com.example.meaning4.R.id.etydef;
import static com.example.meaning4.R.id.etymologies;
import static com.example.meaning4.R.id.gone;
import static com.example.meaning4.R.id.invisible;
import static com.example.meaning4.R.id.wordsheet;

public class findMeaning extends AppCompatActivity {

    String subscriptionKey = "4ca7b53995e041cbab318cbde301835e";
    String endpoint ="https://dictdetection.cognitiveservices.azure.com/";
    public String statusBarHeight = "";
    List<List<Integer>> listOfLists = new ArrayList<>();
    List<String> listOfWords = new ArrayList<>();
    public int selectedInt;
    public Bitmap mSelectedImage;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    public String currentPhotoPath;
    public String audioUri;



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

        TextView knowmore = findViewById(R.id.knowmore);
        knowmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://"));
                final ResolveInfo resolveInfo = getApplicationContext().getPackageManager()
                        .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                String defaultBrowserPackageName = resolveInfo.activityInfo.packageName;

                String url = "https://www.oxfordlearnersdictionaries.com/definition/english/"+listOfWords.get(selectedInt);
                final Intent intent2 = new Intent(Intent.ACTION_VIEW);
                intent2.setData(Uri.parse(url));

                if (!defaultBrowserPackageName.equals("android")){
                    // android = no default browser is set
                    // (android < 6 or fresh browser install or simply no default set)
                    // if it's the case (not in this block), it will just use normal way.
                    intent2.setPackage(defaultBrowserPackageName);
                }

                startActivity(intent2);
            }
        });

        View bottomSheet = findViewById(R.id.bottomSheet);
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        Button findAnotherMeaning = findViewById(R.id.findAnotherMeaning);
        findAnotherMeaning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            }
        });

        ImageView audioPlay = findViewById((R.id.speaker));
        audioPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    PlayAudioManager.playAudio(findMeaning.this, audioUri);
                }catch (Exception e){
                    Log.i("Errororororo", e.toString());
                }
            }
        });

        //Request For Camera Permission
        if(ContextCompat.checkSelfPermission(findMeaning.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(findMeaning.this, new String[]{
                    Manifest.permission.CAMERA
            }, 100);
        }

        ImageView cameraClick = findViewById(R.id.floating_action_button);
        cameraClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView tapHere = findViewById(R.id.tapHere);
                tapHere.setVisibility(View.INVISIBLE);
                String fileName = "photo";
                File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                try{
                    File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
                    currentPhotoPath = imageFile.getAbsolutePath();

                    Uri imageUri = FileProvider.getUriForFile(findMeaning.this, "com.example.meaning4.fileprovider", imageFile);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, 1);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    wordAPI i = new wordAPI();
                    String x = i.execute(listOfWords.get(selectedInt)).get();
                    JSONObject jsonObject = new JSONObject(x);
                    String word = jsonObject.getString("id");
                    TextView wordSheet = findViewById(R.id.wordsheet);
                    wordSheet.setText(word);
                    JSONArray results = jsonObject.getJSONArray("results");
                    JSONObject etyq = results.getJSONObject(0);
                    JSONArray lexicalEntries = etyq.getJSONArray("lexicalEntries");
                    JSONObject etyw = lexicalEntries.getJSONObject(0);
                    JSONArray entries = etyw.getJSONArray("entries");
                    JSONObject etye = entries.getJSONObject(0);
                    try {
                        JSONArray ety = etye.getJSONArray("etymologies");
                        String etymologies = ety.getString(0);
                        TextView etymo = findViewById(R.id.etydef);
                        etymo.setVisibility(View.VISIBLE);
                        TextView tre = findViewById(R.id.etymologies);
                        tre.setVisibility(View.VISIBLE);
                        etymo.setText(etymologies);
                    }catch (Exception e){
                        TextView tre = findViewById(etymologies);
                        tre.setVisibility(View.INVISIBLE);
                        TextView tre2 = findViewById(etydef);
                        tre2.setVisibility(View.INVISIBLE);

                        Log.e("e", e.toString());
                    }

                    JSONArray audioLinkarray = etye.getJSONArray("pronunciations");
                    JSONObject audioLinkObject = audioLinkarray.getJSONObject(0);
                    String audioLink = audioLinkObject.getString("audioFile");
                    audioUri = audioLink;
                    JSONArray definitionarray = etye.getJSONArray("senses");
                    JSONObject definitionObject = definitionarray.getJSONObject(0);
                    JSONArray firstdefArr = definitionObject.getJSONArray("definitions");
                    String firstDefinition = firstdefArr.getString(0);

                    TextView def1 = findViewById(R.id.def1);
                    if(firstDefinition.isEmpty()){
                        def1.setVisibility(View.INVISIBLE);
                    }else {
                        def1.setVisibility(View.VISIBLE);
                    }
                    def1.setText(firstDefinition);
                    JSONArray firstexArr = definitionObject.getJSONArray("examples");
                    JSONObject firstexob = firstexArr.getJSONObject(0);
                    String firstExample = "|   ";
                    firstExample += firstexob.getString("text");
                    TextView exa1 = findViewById(R.id.exa1);
                    exa1.setText(firstExample);
                    try {
                        JSONArray ssde = definitionObject.getJSONArray("subsenses");
                        JSONObject wer = ssde.getJSONObject(0);
                        JSONArray asd = wer.getJSONArray("definitions");
                        String secondDefinition = asd.getString(0);
                        TextView def2 = findViewById(R.id.def2);
                        if(secondDefinition.isEmpty()){
                            def2.setVisibility(View.INVISIBLE);
                        }else {
                            def2.setVisibility(View.VISIBLE);
                        }
                        def2.setText(secondDefinition);
                        JSONArray fds = wer.getJSONArray("examples");
                        JSONObject www = fds.getJSONObject(0);
                        String secondExample = "|   ";
                        secondExample += www.getString("text");
                        TextView exa2 = findViewById(R.id.exa2);
                        if(secondExample.isEmpty()){
                            exa2.setVisibility(View.INVISIBLE);
                        }else {
                            exa2.setVisibility(View.VISIBLE);
                        }
                        exa2.setText(secondExample);
                    }catch (Exception e){
                        TextView dd = findViewById(R.id.def2);
                        dd.setVisibility(View.INVISIBLE);
                        TextView dd2 = findViewById(R.id.exa2);
                        dd2.setVisibility(View.INVISIBLE);
                    }



                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            }
        });








    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.i("tyu", "00");
        Log.i("tyu", String.valueOf(requestCode));

        if (resultCode == RESULT_OK && requestCode == 1) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            if (bitmap != null) {
                Log.i("tyu", "02");
                ImageView im = findViewById(R.id.image98);
                mSelectedImage = Bitmap.createScaledBitmap(bitmap, im.getWidth(), im.getHeight(), true);
                im.setImageBitmap(mSelectedImage);

                @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    @SuppressLint("WrongThread")
                    @Override
                    protected Void doInBackground(Void... voids) {
                        ProgressBar progressBar = findViewById(R.id.indeterminateBar);

                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setProgress(5);
                        ComputerVisionClient compVisClient = ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint);
                        Log.i("a", "\nAzure Cognitive Services Computer Vision - Java Quickstart Sample");
                        RecognizeTextOCRLocal(compVisClient);
                        progressBar.setVisibility(View.INVISIBLE);

                        return null;
                    }
                };
                task.execute();
            }
        }




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
                    TextView worder = findViewById(R.id.worder);
                    worder.setVisibility(View.VISIBLE);
                    choosenWord.setText(listOfWords.get(k).toUpperCase());
                    selectedInt = k;
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
        Bitmap icon4 = BitmapFactory.decodeResource(getResources(),R.drawable.camera6);
        Bitmap icon = mSelectedImage;



        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        try {
            byte[] localImageBytes = ByteStreams.toByteArray(inputStream);

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


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

}
