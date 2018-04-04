package bd.gov.fci.studentforum;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore fireStore;

    private String current_user_id;

    private Toolbar toolbar;
    private FloatingActionButton addPostButton;

    private BottomNavigationView navigationView;

    private HomeFragment homeFragment;
    private AccountFragment accountFragment;
    private NotificationFragment notificationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();

        navigationView = findViewById(R.id.main_button_nav);
        addPostButton = findViewById(R.id.add_post_button_id);

        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Student Forum");


        if (mAuth.getCurrentUser() != null) {
            //Fragment initilize
            homeFragment = new HomeFragment();
            accountFragment = new AccountFragment();
            notificationFragment = new NotificationFragment();

            // call home fragment
            replaceFragment(homeFragment);

            navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.bottom_home_id:

                            replaceFragment(homeFragment);

                            break;
                        case R.id.bottom_account_id:

                            replaceFragment(accountFragment);

                            break;
                        case R.id.bottom_notivfication_id:

                            replaceFragment(notificationFragment);

                            break;
                    }

                    return true;
                }


            });

        }

        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,NewPostActivity.class));
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
       if (currentUser == null ){
           startActivity(new Intent(MainActivity.this,LoginActivity.class));
           finish();
       }else {
           current_user_id = mAuth.getCurrentUser().getUid();
           fireStore.collection("User").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
               @Override
               public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                  if (task.isSuccessful()){
                      if (!task.getResult().exists()){
                          startActivity(new Intent(MainActivity.this,SetupActivity.class));
                          finish();
                      }else {

                      }
                  }else {
                      String error = task.getException().getMessage();
                      Toast.makeText(MainActivity.this, "Error : "+error, Toast.LENGTH_SHORT).show();
                  }
               }
           });
       }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.seach_id:
                break;
            case R.id.setting_id:
                startActivity(new Intent(MainActivity.this,SetupActivity.class));
                break;
            case R.id.logout_id:
                logOut();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logOut() {
        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {
        startActivity(new Intent(MainActivity.this,LoginActivity.class));
        finish();
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container,fragment);
        fragmentTransaction.commit();

    }
}
