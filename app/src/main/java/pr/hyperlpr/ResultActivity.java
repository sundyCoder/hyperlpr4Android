package pr.hyperlpr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;


public class ResultActivity extends AppCompatActivity {
    private TextView completeButton;
    private Bitmap mSourceBitmap;
    private ImageView secondImageView;
    private TextView secondTextView;
    private Uri sourceImagePath;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        initView();
    }

    public void initView() {
        ImageView firstImageView = (ImageView) findViewById(R.id.first_face_image);
        secondImageView = (ImageView) findViewById(R.id.second_face_image);
        secondTextView = (TextView) findViewById(R.id.score);
        completeButton = (TextView) findViewById(R.id.complete);

        float faceFloat = 0;
        Intent intent = getIntent();
        faceFloat = intent.getFloatExtra("score",0.0f);
        sourceImagePath = intent.getParcelableExtra("sourceBitmap");
        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(sourceImagePath);
            mSourceBitmap = BitmapFactory.decodeStream(imageStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (mSourceBitmap != null && !mSourceBitmap.isRecycled()){
            firstImageView.setImageBitmap(mSourceBitmap);
        }
        secondTextView.setText(String.valueOf(faceFloat));

    }
}
