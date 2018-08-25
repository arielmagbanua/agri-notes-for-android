package ph.com.agrinotes.agrinotes;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TakeNotesActivity extends AppCompatActivity {
    public static final String TAG = "TakeNotesActivity";
    private static final int REQUEST_TAKE_PHOTO = 21;
    // private static final int REQUEST_IMAGE_CAPTURE = 211;
    private static final int REQUEST_PICK_IMAGE = 212;
    private String currentPhotoPath = "";

    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "final_result";

    private Toolbar imageToolbar = null;
    private ImageView imageView = null;
    private Classifier classifier;
    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;
    // private static final String INPUT_NAME = "input";
    // private static final String OUTPUT_NAME = "MobilenetV1/Predictions/Softmax";

    private static final String MODEL_FILE = "file:///android_asset/optimized_graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/retrained_labels.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_note);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageToolbar = findViewById(R.id.image_toolbar);
        imageToolbar.inflateMenu(R.menu.image_toolbar_menu);
        Menu imageToolbarMenu = imageToolbar.getMenu();
        MenuItem takePictureMenuItem = imageToolbarMenu.findItem(R.id.action_take_picture);
        MenuItem pickPictureMenuItem = imageToolbarMenu.findItem(R.id.action_pick_picture);

        imageView = findViewById(R.id.note_image);

        // set up
        takePictureMenuItem.setOnMenuItemClickListener(imageToolbarMenuItemClickListener);
        pickPictureMenuItem.setOnMenuItemClickListener(imageToolbarMenuItemClickListener);

        classifier =
                TensorFlowImageClassifier.create(
                        getAssets(),
                        MODEL_FILE,
                        LABEL_FILE,
                        INPUT_SIZE,
                        IMAGE_MEAN,
                        IMAGE_STD,
                        INPUT_NAME,
                        OUTPUT_NAME);
    }

    private MenuItem.OnMenuItemClickListener imageToolbarMenuItemClickListener = new MenuItem.OnMenuItemClickListener(){

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()){
                case R.id.action_take_picture:

                    if(currentPhotoPath != null && currentPhotoPath.isEmpty()){
                        showImagePlaceHolder();
                    }

                    dispatchTakePictureIntent();
                    break;
                case R.id.action_pick_picture:

//                    if(currentPhotoPath != null && currentPhotoPath.isEmpty()){
//                        // showImagePlaceHolder();
//                        imageView.setImageDrawable(null);
//                    }

                    pickAnImage();
                    break;
            }

            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.take_note_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();

        switch (itemID){
            case R.id.action_save_note:
                // Save the note here

                return true;
        }

        return false;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "ph.com.agrinotes.agrinotes.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
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

//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
//    }

    private void pickAnImage(){
        Intent intent = new Intent();
        intent.setType("image/jpeg");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_an_image)), REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(Activity.RESULT_OK == resultCode){
            switch (requestCode){
                case REQUEST_TAKE_PHOTO:

                    // classifyImage(currentPhotoPath);
                    loadImage(currentPhotoPath);
                    runClassifier(currentPhotoPath);
                    break;

                case REQUEST_PICK_IMAGE:
                    Uri selectedImageUri = data.getData();
//                    selectedImagePath = getRealPathFromURI(selectedImageUri);
//                    classifyImage(selectedImagePath);
                    loadImage(selectedImageUri);
                    break;
            }
        }
    }

    private void runClassifier(String path) {

    }

    private void classifyImage(final Bitmap bitmap){
        new Runnable() {
            @Override
            public void run() {
                final long startTime = SystemClock.uptimeMillis();
                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
                long lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                // cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                // resultsView.setResults(results);
                // requestRender();
                // computing = false;
            }
        }.run();
    }

    private void loadImage(Uri imageUri){
        try {
            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
            imageView.setImageBitmap(selectedImage);

            classifyImage(selectedImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadImage(String path){

        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        imageView.setImageBitmap(bitmap);

        classifyImage(bitmap);
    }

    private void showImagePlaceHolder(){
        imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_add_note));
    }

    /**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }

        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);

        if( cursor != null ){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }

        // this is our fallback here
        return uri.getPath();
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);

        if(cursor.moveToFirst()){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }

        cursor.close();
        return res;
    }
}
