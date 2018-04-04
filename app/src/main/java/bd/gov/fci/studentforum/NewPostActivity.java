package bd.gov.fci.studentforum;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {
    private ImageView newPostImage;
    private TextView newPostText;
    private Button newPostButton;
    private ProgressBar newPostProgressbar;

    private Uri postImageUri = null;
    private Bitmap compressedImageFile;

    private StorageReference reference;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    private String current_user_id;


    private Toolbar newPostToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        reference = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        newPostToolbar = findViewById(R.id.new_post_toolbar_id);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPostImage = findViewById(R.id.mage_id);
        newPostText = findViewById(R.id.kawsarId);
        newPostButton = findViewById(R.id.post_button_id);
        newPostProgressbar = findViewById(R.id.new_post_progress_id);

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage
                        .activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(NewPostActivity.this);
            }
        });

        newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String description = newPostText.getText().toString();
                if (!TextUtils.isEmpty(description) && postImageUri  !=null){
                    newPostProgressbar.setVisibility(View.VISIBLE);

                    final String randomName = UUID.randomUUID().toString();

                    final StorageReference filePath = reference.child("Post_image").child(randomName + ".jpg");

                    filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                            final String download_uri = task.getResult().getDownloadUrl().toString();

                            if (task.isSuccessful()){

                                File newImageFile = new File(postImageUri.getPath());
                                // Compressing image
                                try {
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                     .setMaxHeight(100)
                                     .setMaxWidth(100)
                                     .setQuality(2)
                                    .compressToBitmap(newImageFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                byte[] thumbData = baos.toByteArray();

                                UploadTask uploadTask = reference.child("Post_image/thumb").child(randomName+".jpg").putBytes(thumbData);
                               uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                   @Override
                                   public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                       String download_thumb_url = taskSnapshot.getDownloadUrl().toString();
                                       Map<String,Object> postMap = new HashMap<>();
                                       postMap.put("image_url",download_uri);
                                       postMap.put("thumb_image",download_thumb_url);
                                       postMap.put("desc",description);
                                       postMap.put("user_id",current_user_id);
                                       postMap.put("timestamp",FieldValue.serverTimestamp());

                                       firestore.collection("Post").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                           @Override
                                           public void onComplete(@NonNull Task<DocumentReference> task) {
                                               if (task.isSuccessful()){

                                                   Toast.makeText(NewPostActivity.this, "Post was added.", Toast.LENGTH_SHORT).show();
                                                   startActivity(new Intent(NewPostActivity.this,MainActivity.class));
                                                   finish();
                                               }else {

                                               }
                                               newPostProgressbar.setVisibility(View.INVISIBLE);
                                           }
                                       });
                                   }
                               }).addOnFailureListener(new OnFailureListener() {
                                   @Override
                                   public void onFailure(@NonNull Exception e) {

                                   }
                               });



                            }else {
                                newPostProgressbar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });



                }else {
                    Toast.makeText(NewPostActivity.this, "Field must not be empty !", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    //now user this method for image process
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
