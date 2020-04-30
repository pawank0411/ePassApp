package com.example.epassapp.utilities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.epassapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;

public class AskSignature {
    private Context context;
    private static String storedPath;
    private static MaterialButton mGetSign;
    private static Bitmap bitmap;
    private LinearLayout linearLayout;
    public boolean isSuccessfullySaved;

    public AskSignature(Context context) {
        this.context = context;
    }


    public void GetSignature(String username) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_signature);
        dialog.setCancelable(true);

        String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ePass/";
        File file = new File(directory);
        boolean isDirectoryCreated = file.exists();
        if (!isDirectoryCreated) {
            isDirectoryCreated = file.mkdir();
            storedPath = directory + username + ".png";
        }
        if (isDirectoryCreated) {
            storedPath = directory + username + ".png";
        }

        linearLayout = dialog.findViewById(R.id.linearLayout);
        final signature mSignature = new signature(context, null);
        mSignature.setBackgroundColor(Color.WHITE);
        linearLayout.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        MaterialButton mClear = dialog.findViewById(R.id.clear);
        MaterialButton mCancel = dialog.findViewById(R.id.mcancel);
        mGetSign = dialog.findViewById(R.id.getsign);
        mGetSign.setEnabled(false);
        final View view = linearLayout;

        mClear.setOnClickListener(v -> {
            Log.v("tag", "Panel Cleared");
            mSignature.clear();
            mGetSign.setEnabled(false);
        });
        mGetSign.setOnClickListener(v -> {
            Log.v("tag", "Panel Saved");
            view.setDrawingCacheEnabled(true);
            mSignature.save(view);
            dialog.dismiss();
        });
        mCancel.setOnClickListener(view1 -> dialog.dismiss());
        dialog.show();
    }

    public class signature extends View {
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        @SuppressLint("WrongThread")
        public void save(View v) {
            Log.v("tag", "Width: " + v.getWidth());
            Log.v("tag", "Height: " + v.getHeight());
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(linearLayout.getWidth(), linearLayout.getHeight(), Bitmap.Config.RGB_565);
            }
            Canvas canvas = new Canvas(bitmap);
            try {
                FileOutputStream mFileOutStream = new FileOutputStream(storedPath);
                v.draw(canvas);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream);
                mFileOutStream.flush();
                mFileOutStream.close();
                if (storedPath != null) {
                    final ProgressDialog progressDialog = new ProgressDialog(context);
                    progressDialog.setTitle("Saving your signature");
                    progressDialog.setMessage("Please wait..");
                    progressDialog.show();

                    File file = new File(storedPath);
                    StorageReference signsRef = FirebaseStorage.getInstance().getReference().child("signatures/" + file.getName());
                    signsRef.putFile(Uri.fromFile(new File(storedPath)))
                            .addOnSuccessListener(taskSnapshot -> {
                                progressDialog.dismiss();
                                Toast.makeText(context, "Now you're ready to accept passes!", Toast.LENGTH_LONG).show();
                                isSuccessfullySaved = true;
                            })
                            .addOnFailureListener(exception -> {
                                progressDialog.dismiss();
                                Log.d("error", exception.getMessage());
                            });
                }
            } catch (Exception e) {
                Log.v("log_tag", e.toString());
            }
        }

        public void clear() {
            path.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            mGetSign.setEnabled(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:
                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;
                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string) {
            Log.v("log_tag", string);
        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }
}
