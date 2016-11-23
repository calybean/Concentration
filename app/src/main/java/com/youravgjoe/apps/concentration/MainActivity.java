package com.youravgjoe.apps.concentration;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import static android.content.Intent.EXTRA_ALLOW_MULTIPLE;
import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int SELECT_PICTURE = 47;
    private static final int PERMISSION_REQUEST_WRITE_FILES = 47;
    private static final String PHOTO_LIST = "PHOTO_LIST";
    private ArrayList<String> mFilePathList;
    private LinearLayout mImageContainer;
    private TextView mPhotoCountTextView;
    private TextView mPhotoPromptTextView;
    private int mPhotoCount;
    private Button mAddPhotosButton;
    private int mBitmapId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button startGame = (Button) findViewById(R.id.start_game_new);
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GameActivity.class));
            }
        });



//        mImageContainer = (LinearLayout) findViewById(R.id.image_container);
//        mPhotoCountTextView = (TextView) findViewById(R.id.photo_count_textview);
//        mPhotoPromptTextView = (TextView) findViewById(R.id.photo_prompt_textview);
//        mAddPhotosButton = (Button) findViewById(R.id.add_photos);
//        mFilePathList = new ArrayList<>();
//
//        Button startGame = (Button) findViewById(R.id.start_game);
//        startGame.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mPhotoCount == 8) {
//                    Intent newGameIntent = new Intent(MainActivity.this, GameActivity.class);
//                    newGameIntent.putStringArrayListExtra(PHOTO_LIST, mFilePathList);
//                    startActivity(newGameIntent);
//                } else {
//                    startActivity(new Intent(MainActivity.this, GameActivity.class));
//                }
//            }
//        });
//
//        mAddPhotosButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (ContextCompat.checkSelfPermission(v.getContext(),
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                        != PackageManager.PERMISSION_GRANTED) {
//                    // if we don't have the permission yet, get it.
//                    checkPermission();
//                } else {
//                    // if we do, then go get a photo from their files
//                    Intent pickIntent = new Intent();
//                    pickIntent.setType("image/*");
//                    pickIntent.setAction(Intent.ACTION_GET_CONTENT);
//                    String pickTitle = getResources().getString(R.string.prompt_import_photo);
//                    Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
//
//                    // todo: not working:
//                    chooserIntent.putExtra(EXTRA_ALLOW_MULTIPLE, true);
//
//                    startActivityForResult(chooserIntent, SELECT_PICTURE);
//                }
//            }
//        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != RESULT_CANCELED && data != null) {
            if (requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK) {
                try {
                    // use BitmapFactory.Options.inSampleSize to make sure that ONE picture isn't too much memory
                    final InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
                    final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                    // First decode with inJustDecodeBounds=true to check dimensions
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(bufferedInputStream, null, options);

                    final double heightWidthRatio = (double)options.outHeight / (double)options.outWidth;
                    Log.d(TAG, "heightWidthRatio: " + heightWidthRatio);

                    WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
                    Display display = wm.getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);

                    final int dpHeight = 100;

                    // convert dp to pixels
                    float scale = getResources().getDisplayMetrics().density;
                    int reqHeight = (int) (dpHeight * scale + 0.5f);
                    double reqWidth = (int) ((dpHeight / heightWidthRatio) * scale + 0.5f);

                    // Calculate inSampleSize
                    options.inSampleSize = calculateInSampleSize(options, (int)reqWidth, reqHeight);

                    // Decode bitmap with inSampleSize set
                    final InputStream newInputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
                    final BufferedInputStream newBufferedInputStream = new BufferedInputStream(newInputStream);
                    options.inJustDecodeBounds = false;
                    Bitmap bitmap = BitmapFactory.decodeStream(newBufferedInputStream, null, options);

                    // set a new bitmap id every time we get a new bitmap
                    mBitmapId = bitmap.getGenerationId();

                    // add photo to a card, and then to the horizontal scroll view
                    addPhotoToContainer(bitmap);

                    // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                    Uri tempUri = getImageUri(bitmap);

                    // CALL THIS METHOD TO GET THE ACTUAL PATH
                    File finalFile = new File(getRealPathFromURI(tempUri));

                    // add filepath to list
                    mFilePathList.add(finalFile.getAbsolutePath());

                    // increment photo count and update view
                    mPhotoCount++;
                    mPhotoCountTextView.setText(getResources().getString(R.string.photo_count, mPhotoCount));

                    // if this is the first time we're adding a photo, make the prompt visible
                    if (mPhotoPromptTextView.getVisibility() == GONE) {
                        mPhotoPromptTextView.setVisibility(View.VISIBLE);
                    }

                    // if we've reached 8 photos, disable the add button and color it grey
                    if (mPhotoCount == 8) {
                        mAddPhotosButton.setEnabled(false);
                        mAddPhotosButton.setBackgroundColor(getResources().getColor(R.color.grey));
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else {
            //Display an error
            Toast.makeText(this, "Error retrieving photo", Toast.LENGTH_LONG).show();
        }
    }

    public Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        mBitmapId = bitmap.getGenerationId();

        // todo: make this line not create copies of the images we're "importing"
        String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "Title", null);

        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        String filePath = cursor.getString(idx);
        cursor.close();
        return filePath;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void addPhotoToContainer(Bitmap bmp) {
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bmp);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        CardView card = new CardView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPixels(200), dpToPixels(200));
        params.setMargins(dpToPixels(4),dpToPixels(4),dpToPixels(4),dpToPixels(4));

        card.setLayoutParams(params);
        card.setContentPadding(dpToPixels(8), dpToPixels(8), dpToPixels(8), dpToPixels(8));
        card.setMaxCardElevation(5);
        card.setCardElevation(9);

        card.addView(imageView);

        mImageContainer.addView(card);
    }

    private int dpToPixels(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                showMessageOKCancel("Concentration would like permission to access your photos.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_REQUEST_WRITE_FILES);
                            }
                        });
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_WRITE_FILES);
            }
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("Grant Permission", okListener)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .create()
                .show();
    }
}
