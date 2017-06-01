package shipshapedevices.shipshape_v0;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;


public class LoginActivity extends AppCompatActivity {

    // declare module level variables
    private static final String TAG="LoginActivity";
    private FirebaseAuth mAuthUsers;
    String USER="";
    String PASSWORD="";

    //inject views
    @BindView(R.id.loginButton)
    Button loginButton;
    @BindView(R.id.inputUserID)
    EditText userName;
    @BindView(R.id.inputPassword) EditText password;
    @BindView(R.id.signUpLink)
    TextView signUpLink;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        //Get Firebase auth instance
        mAuthUsers = FirebaseAuth.getInstance();
        // add button listener for login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // verify if valid user
                validate();

            } // end onclikck action
        });// end setting up listener


        // add button listener for Sign up link
        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "Launching Sign UP Activity");
                // Start Sign Up Activity
                Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(i);
            } // end onclikck action
        });// end setting up listener

    } //end on create override

    @Override
    protected void onStart(){
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly. 
        FirebaseUser currentUser = mAuthUsers.getCurrentUser();
        if (currentUser != null){
            Intent i = new Intent (LoginActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }

    }


    private void validate(){
        // get username and password
        USER=userName.getText().toString();
        PASSWORD=password.getText().toString();
        // if username entry is empts
        if (TextUtils.isEmpty(USER)) {
            Toast.makeText(getApplicationContext(), getString(R.string.missingUsername), Toast.LENGTH_SHORT).show();
            return;
        }

        // if password doesnt meet requirements
        if (PASSWORD.length() < 6) {
            Toast.makeText(getApplicationContext(), getString(R.string.loginError), Toast.LENGTH_SHORT).show();
            return;
        }

        // Authenticate login with Firebase
        mAuthUsers.signInWithEmailAndPassword(USER, PASSWORD)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user.
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, getString(R.string.loginError), Toast.LENGTH_LONG).show();
                        } //end if not successfule

                        // else sign in successful login to app
                        else {
                            Intent intent = new Intent(LoginActivity.this, MyPackagesActivity.class);
                            startActivity(intent);
                            finish();
                        } //end if successful
                    } // end on complete authentification
                }); // end firebase user authentification
    } //end validate function


}// end login activity