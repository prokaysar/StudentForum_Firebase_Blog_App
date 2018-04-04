package bd.gov.fci.studentforum;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth mAuth;

    private ImageView imageView;
    private EditText userEmail,userPassword;
    private Button userLogin,userRegister;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        imageView = findViewById(R.id.imageView_register);
        userEmail = findViewById(R.id.register_email_id);
        userPassword = findViewById(R.id.register_password_id);
        userLogin = findViewById(R.id.loginButtonId);
        userRegister = findViewById(R.id.register_login_id);
        progressBar = findViewById(R.id.progressBar2);



        userLogin.setOnClickListener(this);
        userRegister.setOnClickListener(this);

    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.loginButtonId:
                userLogin();
                break;
            case R.id.register_login_id:
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
                break;
        }

    }

    private void userLogin() {
        final String email = userEmail.getText().toString().trim();
        String password = userPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            progressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        gotoMainActivity();
                    }else {

                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(LoginActivity.this, "Error : "+errorMessage, Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null ){
          gotoMainActivity();
        }
    }

    private void gotoMainActivity() {
        startActivity(new Intent(LoginActivity.this,MainActivity.class));
        finish();
    }


}
