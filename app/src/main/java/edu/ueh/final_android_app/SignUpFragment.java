package edu.ueh.final_android_app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import edu.ueh.final_android_app.models.Account;
import edu.ueh.final_android_app.util.CommonUtil;
import edu.ueh.final_android_app.util.FirebaseUtil;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment {

    private EditText edtFirstName, edtLastName, edtUsername, edtPassword, edtEmail;
    private Button btnSignup;
    private TextView btnBackLogin;

    private GoogleSignInClient googleSignInClient;
    private String googleEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        edtFirstName = view.findViewById(R.id.edtFirstName);
        edtLastName = view.findViewById(R.id.edtLastName);
        edtUsername = view.findViewById(R.id.edtUsername);
        edtPassword = view.findViewById(R.id.edtPassword);
        edtEmail = view.findViewById(R.id.edtEmail);
        btnSignup = view.findViewById(R.id.btnSignup);
        btnBackLogin = view.findViewById(R.id.btnBackLogin);

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), options);

        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (acc != null) {
            googleEmail = acc.getEmail();
            edtEmail.setText(googleEmail);
        }

        btnSignup.setOnClickListener(v -> onSignup());
        btnBackLogin.setOnClickListener(v -> backToLogin());

        return view;
    }

    private void onSignup() {
        String fn = edtFirstName.getText().toString();
        String ln = edtLastName.getText().toString();
        String un = edtUsername.getText().toString();
        String pw = edtPassword.getText().toString();

        if (fn.isEmpty() || ln.isEmpty() || un.isEmpty() || pw.isEmpty()) {
            Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUtil firebaseUtil = new FirebaseUtil();
        Account newAccount = new Account("", fn, ln, un, pw, null, false);
        newAccount.setEmail(googleEmail);

        firebaseUtil.saveUserToFirestore(
                newAccount,
                documentReference -> {
                    newAccount.setId(documentReference.getId());
                    CommonUtil.currentUser = newAccount;
                    goToHomepage();
                },e -> {
                    Toast.makeText(requireContext(), "Sign up failed!", Toast.LENGTH_SHORT);
                });



    }

    private void backToLogin() {
        googleSignInClient.signOut().addOnCompleteListener(t -> {
            ((MainActivity) requireActivity()).goToSignIn();
        });
    }

    private void goToHomepage(){
        ((MainActivity) requireActivity()).onLoginSuccess();
    }
}