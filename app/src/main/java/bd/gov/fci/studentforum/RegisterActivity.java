package bd.gov.fci.studentforum;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

public class RegisterActivity extends AppCompatActivity  implements View.OnClickListener{
        private EditText userEmail,userPassword,conformPassword;
        private Button registerButton,loginButton;
        private ProgressBar regProgressbar;
        private ImageView imageView;


        private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reister);

        mAuth = FirebaseAuth.getInstance();



        userEmail = findViewById(R.id.register_email_id);
        userPassword = findViewById(R.id.register_password_id);
        conformPassword = findViewById(R.id.conform_register_password_id);
        regProgressbar = findViewById(R.id.progressBar_register);
        imageView = findViewById(R.id.imageView_register);

        registerButton = findViewById(R.id.register_button_id);
        loginButton = findViewById(R.id.register_login_id);

        registerButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.register_button_id:
                userRegister();
                break;
            case R.id.register_login_id:
               sendToLogin();
                break;
        }
    }

    private void userRegister() {
        String email = userEmail.getText().toString().trim();
        String password = userPassword.getText().toString().trim();
        String cPassword = conformPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(cPassword)){
                if (password.equals(cPassword)){
                    regProgressbar.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                    startActivity(new Intent(RegisterActivity.this,SetupActivity.class));
                                    finish();
                            }else {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "Error : "+errorMessage, Toast.LENGTH_SHORT).show();

                            }
                            regProgressbar.setVisibility(View.INVISIBLE);
                        }
                    });
                }else {
                    Toast.makeText(this, "Password mis match.", Toast.LENGTH_SHORT).show();
                }
        }else {

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
           sendToMain();
        }
    }
    private void sendToLogin() {
        startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
        finish();
    }
    private void sendToMain() {
        startActivity(new Intent(RegisterActivity.this,MainActivity.class));
        finish();
    }
}
