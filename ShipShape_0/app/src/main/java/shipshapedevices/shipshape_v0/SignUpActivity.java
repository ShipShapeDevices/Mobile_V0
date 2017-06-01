package shipshapedevices.shipshape_v0;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";
    private FirebaseAuth mAuthUsers;
    private DatabaseReference userRef;
    private String fireUserName;



    // declare modular level varaiables
    List<List<String>> Users = new ArrayList();
    static HashMap<String, String> userMap= new HashMap();

    //inject views
    @BindView(R.id.firstNameInput)
    EditText firstName;
    @BindView(R.id.lastNameInput) EditText lastName;
    @BindView(R.id.userNameInput) EditText userName;
    @BindView(R.id.passwordInput) EditText password;
    @BindView(R.id.passwordVerifyInput) EditText passwordVerify;
    @BindView(R.id.loginLink)
    TextView loginLink;
    @BindView(R.id.signUpButton)
    Button signUpButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        //Get Firebase auth instance
        mAuthUsers = FirebaseAuth.getInstance();
        // Get instance off of firebase database
        userRef= FirebaseDatabase.getInstance().getReference("users");

        // add button listener for sign up button to sign up user
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // try to signup user
                signUp();

            } // end onclikck action
        });// end setting up listener


        // add button listener for login link to launch login activity
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "Launching Login Activity");
                // Start Sign Up Activity
                Intent i = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(i);

            } // end onclikck action
        });// end setting up listener

    }// end on creat override


    // try to sign up user
    private void signUp(){

        //if we are missing fields notify user
        if(firstName.getText().toString().equals("")
                || lastName.getText().toString().equals("")
                || userName.getText().toString().equals("")
                || password.getText().toString().equals("")
                || passwordVerify.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), getString(R.string.signUpBlanks), Toast.LENGTH_SHORT).show();
        }// end if

        // else if passwords do not match notify user
        else if (!password.getText().toString().equals(passwordVerify.getText().toString())){

            Toast.makeText(getApplicationContext(), getString(R.string.signUpPasswordError), Toast.LENGTH_SHORT).show();

        }// end else if passwords dont match

        //else user successfully signed up
        else{
             fireUserName= userName.getText().toString();
             String firePassword= passwordVerify.getText().toString();

            //add new user to arraylist
            Users.add(Arrays.asList(fireUserName,firePassword));
            userMap.put(fireUserName,firePassword);

            //create user in Firebase
            registerUser(fireUserName,firePassword);
        } // end else successful login

    }// end signUp function


    //register the user via Firebase
    private void registerUser(String email, String pw){
        mAuthUsers.createUserWithEmailAndPassword(email, pw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(SignUpActivity.this, "Successfully registered!",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuthUsers.getCurrentUser();
                            // Create new user object for linking package references
                            User u = new User(user.getUid());
                            u.setUserName(fireUserName);
                            // Add new user to firebase user reference db
                            addUserToFirebase(u);
                            //updateUI(user);
                            startActivity(new Intent(SignUpActivity.this,LoginActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }


    private void addUserToFirebase(User u){
        //add the new user to firebase using the key
        Log.d(TAG, "Username" +u.getUserName());
        userRef.child(u.getUserName().replace('.',',')).setValue(u).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //do nothing
                        }
                        else{
                            //display error toast
                            Toast toast = Toast.makeText(getApplicationContext(),"Firebase user write error", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }
        );
    }


    //update the application UI for the current user
    private void updateUI(FirebaseUser user) {
        //for now, just enter the email into the text entry & clear the pw entry
        if (user != null) {
            //nameEntry.setText(user.getEmail());
            //pwEntry.setText("");
        }
    }


} //end signUp activity