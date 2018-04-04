package bd.gov.fci.studentforum;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity implements View.OnClickListener{
    private Toolbar toolbar;
    private CircleImageView setupImage;
    private Uri mainImageUri = null;
    private String user_id;
    private boolean isChange = false;

    private EditText setupName;
    private Button setupButton;
    private ProgressBar setupProgress;

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        //firebase initilization
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();
        //getting user Id from frirebase authentication
        user_id = mAuth.getCurrentUser().getUid();

        //setup toolbar
        toolbar = findViewById(R.id.setup_toolbar_id);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Setting");

        // initializing
        setupImage = findViewById(R.id.profile_image);
        setupName = findViewById(R.id.user_name_id);
        setupButton = findViewById(R.id.setup_button_id);
        setupProgress = findViewById(R.id.setup_progress_bar);


        //retriving data from firebase firestore
        setupProgress.setVisibility(View.VISIBLE);
        setupButton.setEnabled(false);
        firestore.collection("User").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        //getting date from firestore
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        mainImageUri = Uri.parse(image);

                        setupName.setText(name);
                        //creating a placeholder for shoing by default image before image viewing
                        RequestOptions placehoder = new RequestOptions();
                        placehoder.placeholder(R.drawable.default_profile_image);
                        //Glide is a libary for image view
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placehoder).load(image).into(setupImage);
                        }

                }else {
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "FIRESTORE Retrieve Error : "+errorMessage, Toast.LENGTH_SHORT).show();
                }
                setupProgress.setVisibility(View.INVISIBLE);
                setupButton.setEnabled(true);
            }
        });

        //set click event
        setupImage.setOnClickListener(this);
        setupButton.setOnClickListener(this);
    }   // end onCreate Method
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.profile_image:
                setupImage();  // call setup image method
                break;
            case R.id.setup_button_id:
                saveInformationOnFireStore();
                break;
        }
    }
    //Update user information on fireStore
    private void saveInformationOnFireStore() {
        final String user_name = setupName.getText().toString().trim();
        if (!TextUtils.isEmpty(user_name) && mainImageUri !=null){
        //start Progressing
        setupProgress.setVisibility(View.VISIBLE);
       if (isChange){
               //get user id from firebase auth
               user_id = mAuth.getCurrentUser().getUid();
               //1. child is create folder
               //2. child create unique name of image by the user id
               StorageReference image_path = mStorageRef.child("Profile_image").child(user_id+".jpg");
               //upload image on firebase storage
               image_path.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                       if (task.isSuccessful()){
                           stroreFireStore(task,user_name);

                       }else {
                           String errorMassage = task.getException().getMessage();
                           Toast.makeText(SetupActivity.this, "IMAGE Error : "+errorMassage, Toast.LENGTH_SHORT).show();
                       }
                       setupProgress.setVisibility(View.INVISIBLE);
                   }
               });
           }else {
           stroreFireStore(null,user_name);
            }

       } else {
            Toast.makeText(this, "Field Must not be empty !", Toast.LENGTH_SHORT).show();
        }

    }

    private void stroreFireStore(@NonNull Task<UploadTask.TaskSnapshot> task,String user_name) {
        Uri download_uri;
        if (task !=null){
            //download image url from Firebase Storage
             download_uri = task.getResult().getDownloadUrl();
        }else {
             download_uri = mainImageUri;
        }


        //creating collection for passing obj on firestore
        Map<String,String> userMap = new HashMap<>();
        userMap.put("name",user_name);
        userMap.put("image",download_uri.toString());
        setupProgress.setVisibility(View.VISIBLE);
        // collection create a collection with the name of user
        // document create a document with the user_id
        //on set method pass map object
        firestore.collection("User").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(SetupActivity.this, "User setting are updated.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();
                }else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "FIRESTORE Error : "+error, Toast.LENGTH_SHORT).show();
                }
                setupProgress.setVisibility(View.INVISIBLE);
            }
        });
    }

    //setup profile image
    private void setupImage() {
        // External storage read write permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(SetupActivity.this, "Permission denied.", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }else {
                imagePicker();  // call image picker method
            }
        }else{
            imagePicker();
        }
    }
// image picker method get image and crop
    private void imagePicker() {
        CropImage
                .activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SetupActivity.this);
    }
//now user this method for image process
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageUri = result.getUri();
                setupImage.setImageURI(mainImageUri);
                //for updating image
                isChange = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
