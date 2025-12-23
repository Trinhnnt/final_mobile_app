package edu.ueh.final_android_app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.developer.gbuttons.GoogleSignInButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import edu.ueh.final_android_app.models.Account;
import edu.ueh.final_android_app.util.CommonUtil;
import edu.ueh.final_android_app.util.FirebaseUtil;

public class LoginFragment extends Fragment {
    private final ActivityResultLauncher<Intent> googleLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount googleAccount = task.getResult(ApiException.class);
                String email = googleAccount.getEmail();

                FirebaseUtil firebaseUtil = new FirebaseUtil();
                firebaseUtil.getUserByEmail(email, new FirebaseUtil.OnUserGetListener() {
                    @Override
                    public void onSuccess(Account account) {
                        if (account.isBanned()){
                            Toast.makeText(requireContext(), "Your account has been banned", Toast.LENGTH_LONG).show();
                            return;
                        }
                        CommonUtil.currentUser = account;
                        goToHomePage();
                    }

                    @Override
                    public void onNotFound() {
                        goToSignUp();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (ApiException e) {
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            } catch (Exception e){
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
    });
    GoogleSignInButton googleSignInButton;
    GoogleSignInOptions googleSignInOptions;
    GoogleSignInClient googleSignInClient;
    SharedPreferences sharedPreferences;
    EditText etUsername, etPassword;
    Button btnLogin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void goToHomePage() {
        ((MainActivity) requireActivity()).onLoginSuccess();
    }

    private void goToSignUp() {
        ((MainActivity) requireActivity()).goToSignUp();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        googleSignInButton = view.findViewById(R.id.btnGoogle);

        // Google Activity Result
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions);

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(requireContext());

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleLauncher.launch(signInIntent);
            }
        });

        etUsername = view.findViewById(R.id.edtEmail);
        etPassword = view.findViewById(R.id.edtPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        addLoginListener();

        // Inflate the layout for this fragment
        return view;
    }

    private void addLoginListener() {
        btnLogin.setOnClickListener(v -> {
            String username = String.valueOf(etUsername.getText());
            String password = String.valueOf(etPassword.getText());

            if (!CommonUtil.isRequired(username) || !CommonUtil.isRequired(password)) {
                Toast.makeText(requireContext(), "Please enter username/email and password", Toast.LENGTH_LONG).show();
                return;
            }
            FirebaseUtil firebaseUtil = new FirebaseUtil();
            firebaseUtil.login(username, password, new FirebaseUtil.OnUserGetListener() {
                @Override
                public void onSuccess(Account account) {
                    if (account.isBanned()) {
                        Toast.makeText(requireContext(), "Your account has been banned", Toast.LENGTH_LONG).show();
                        return;
                    }
                    CommonUtil.currentUser = account;
                    goToHomePage();
                }

                @Override
                public void onNotFound() {
                    Toast.makeText(requireContext(), "Username/Email or password is not correct!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}