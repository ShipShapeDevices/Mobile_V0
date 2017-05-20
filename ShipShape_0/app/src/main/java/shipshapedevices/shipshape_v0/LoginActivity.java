package shipshapedevices.shipshape_v0;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    @BindView(R.id.textedit_username_login)
    EditText mUserName;
    @BindView(R.id.textedit_password_login)
    EditText mPassword;
    @BindView(R.id.button_login)
    Button buttonLogin;
    @BindView(R.id.button_sign_up)
    Button buttonSignUp;

    @BindString(R.string.successful_toast)
    String successfulToast;
    boolean appConnected=false;

    private FirebaseAuth mAuthUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        //get users from firebase
        mAuthUsers = FirebaseAuth.getInstance();
        //check firebase if connected
        DatabaseReference connectedFirebase = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedFirebase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected){
                    appConnected  = true;
                }else{
                    appConnected  = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuthUsers.getCurrentUser();

        if (currentUser != null){
            // start Control activity
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
            // finish this activity
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // grab text edit fields and store values
                String userName = mUserName.getText().toString();
                String password = mPassword.getText().toString();

                if(userName.matches("")) {
                    Toast.makeText(getApplicationContext(), R.string.please_enter_username_toast, Toast.LENGTH_SHORT).show();
                    mPassword.setText("");
                }else if(password.matches("")) {
                    Toast.makeText(getApplicationContext(), R.string.please_enter_password_toast, Toast.LENGTH_SHORT).show();
                }else if (appConnected) {
                    // sign in

                    mAuthUsers.signInWithEmailAndPassword(userName, password)
                            .addOnCompleteListener(LoginActivity.this,
                                    new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                //sign in success
                                                FirebaseUser user = mAuthUsers.getCurrentUser();
                                                // start Control activity
                                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                                startActivity(i);
                                                // finish this activity
                                                finish();
                                            } else {
                                                //sign in failed
                                                Log.e(TAG, ((FirebaseAuthException) task.getException()).getMessage());

                                                try {
                                                    throw task.getException();
                                                } catch (FirebaseAuthInvalidUserException e) {
                                                    Toast.makeText(getApplicationContext(), R.string.username_does_not_exist_toast, Toast.LENGTH_SHORT).show();
                                                    mUserName.setText("");
                                                    mPassword.setText("");
                                                } catch (Exception e) {
                                                    // If sign up  in fails, display a message to the user.
                                                    Log.w(TAG, "createUserWithEmailAndPassword:failure", task.getException());
                                                    Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                                    mUserName.setText("");
                                                    mPassword.setText("");

                                                }


                                            }
                                        }
                                    });

                }else{
                    Toast.makeText(LoginActivity.this, "No Network Connection", Toast.LENGTH_SHORT).show();
                }


            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // clear text field
                mUserName.setText("");
                mPassword.setText("");
                // on click of sign up button open dialog
                LoginActivity.MySignUpDialog dialog = new LoginActivity.MySignUpDialog(LoginActivity.this); // Context, this, etc.
                //show Dialog
                dialog.show();

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }


    public class MySignUpDialog extends Dialog {

        @BindView(R.id.textedit_name_sign_up)
        EditText mNameSignUp;
        @BindView(R.id.textedit_username_sign_up)
        EditText mUsernameSignUp;
        @BindView(R.id.textedit_password_sign_up)
        EditText mPasswordSignUp;
        @BindView(R.id.button_cancel_sign_up)
        Button buttonCancelSignUp;
        @BindView(R.id.button_ok_sign_up)
        Button buttonOkSignUp;

        public MySignUpDialog(Context context) {
            super(context);
        }


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setCancelable(false);
            // This is the layout XML file that describes your Dialog layout
            this.setContentView(R.layout.dialog_register);
            //Bind butterknife
            ButterKnife.bind(this);


            //set button setOnClickListener
            buttonCancelSignUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // on click of cancel dismiss and go back to screen
                    dismiss();
                    // clear text fields
                    mNameSignUp.setText("");
                    mUsernameSignUp.setText("");
                    mPasswordSignUp.setText("");

                }
            });

            buttonOkSignUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // grab text edit fields and store values
                    String username = mUsernameSignUp.getText().toString();
                    String password = mPasswordSignUp.getText().toString();
                    if(username.matches("")) {
                        Toast.makeText(getApplicationContext(), R.string.please_enter_username_toast, Toast.LENGTH_SHORT).show();
                    }else if(password.matches("")) {
                        Toast.makeText(getApplicationContext(), R.string.please_enter_password_toast, Toast.LENGTH_SHORT).show();
                    }else if(password.length() <6 ) {
                        Toast.makeText(getApplicationContext(), R.string.password_to_short_toast, Toast.LENGTH_SHORT).show();
                    }else if (appConnected) {
                        mAuthUsers.createUserWithEmailAndPassword(username, password)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            //get current user
                                            FirebaseUser user = mAuthUsers.getCurrentUser();

                                            // clear text fields
                                            mNameSignUp.setText("");
                                            mUsernameSignUp.setText("");
                                            mPasswordSignUp.setText("");
                                            //close
                                            dismiss();
                                            // start Control activity
                                            Intent i = new Intent(LoginActivity.this,MainActivity.class);
                                            startActivity(i);
                                            // finish this activity
                                            finish();

                                        } else {

                                            try {
                                                throw task.getException();
                                            } catch (FirebaseAuthWeakPasswordException e) {
                                                Toast.makeText(LoginActivity.this,R.string.weak_password_text, Toast.LENGTH_SHORT).show();
                                                mPasswordSignUp.setText("");
                                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                                Toast.makeText(LoginActivity.this, R.string.invalid_email_text, Toast.LENGTH_SHORT).show();
                                                mPasswordSignUp.setText("");
                                            } catch (FirebaseAuthUserCollisionException e) {
                                                //uesrname already exists
                                                Toast.makeText(getApplicationContext(), R.string.username_exists_toast, Toast.LENGTH_SHORT).show();
                                                mUsernameSignUp.setText("");
                                                mPasswordSignUp.setText("");
                                            } catch (Exception e) {
                                                // If sign up  in fails, display a message to the user.
                                                Log.w(TAG, "createUserWithEmailAndPassword:failure", task.getException());
                                                Toast.makeText(LoginActivity.this, R.string.login_failed_text, Toast.LENGTH_SHORT).show();

                                            }

                                        }

                                    }
                                });
                    }else {
                        Toast.makeText(LoginActivity.this,R.string.no_network_text, Toast.LENGTH_SHORT).show();
                    }


                }
            });

        }

    }

}
