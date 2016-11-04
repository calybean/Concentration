package com.youravgjoe.apps.concentration;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.content.Intent.EXTRA_ALLOW_MULTIPLE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int SELECT_PICTURE = 47;
    private static final int PERMISSION_REQUEST_READ_FILES = 47;
    private LinearLayout mImageContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mImageContainer = (LinearLayout) findViewById(R.id.image_container);

        Button startGame = (Button) findViewById(R.id.start_game);
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GameActivity.class));
            }
        });

        Button addPhotosButton = (Button) findViewById(R.id.add_photos);
        addPhotosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(v.getContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // if we don't have the permission yet, get it.
                    checkPermission();
                } else {
                    // if we do, then go get a photo from their files
                    Intent pickIntent = new Intent();
                    pickIntent.setType("image/*");
                    pickIntent.setAction(Intent.ACTION_GET_CONTENT);
                    String pickTitle = getResources().getString(R.string.prompt_import_photo);
                    Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);

                    // todo: not working:
                    chooserIntent.putExtra(EXTRA_ALLOW_MULTIPLE, true);

                    startActivityForResult(chooserIntent, SELECT_PICTURE);
                }
            }
        });
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

                    Log.d(TAG, "options: " + options.toString());

                    final double heightWidthRatio = (double)options.outHeight / (double)options.outWidth;
                    Log.d(TAG, "heightWidthRatio: " + heightWidthRatio);


                    WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
                    Display display = wm.getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);

                    final int dpHeight = 100;

                    // todo: something about height and width is still not working.
                    // todo: we should also probably upload these photos sometime...

                    // convert pixels to dp
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

                    addPhotoToContainer(bitmap);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else {
            //Display an error
            Toast.makeText(this, "Error retrieving photo", Toast.LENGTH_LONG).show();
        }
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

        // convert pixels to dp for margin
        float scale = getResources().getDisplayMetrics().density;
        int dp = (int) (8 * scale + 0.5f);

        if (mImageContainer.getChildCount() == 0) {
            imageView.setPadding(0, 0, 0, 0); // left, top, right, bottom
        } else {
            imageView.setPadding(dp, 0, 0, 0); // left, top, right, bottom
        }
        imageView.setImageBitmap(bmp);

        mImageContainer.addView(imageView);
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                showMessageOKCancel("Concentration would like permission to access your photos.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                        PERMISSION_REQUEST_READ_FILES);
                            }
                        });
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_READ_FILES);
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
