package edu.ueh.final_android_app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import edu.ueh.final_android_app.util.CommonUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {
    private TextView tvFullname;
    private TextView tvUsername;
    private ImageView btnLogout;

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        return new AccountFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account, container, false);
        tvFullname = root.findViewById(R.id.fullname);
        tvUsername = root.findViewById(R.id.username);
        btnLogout = root.findViewById(R.id.btnLogout);

        tvFullname.setText(CommonUtil.currentUser.getFullName());
        tvUsername.setText(String.format("@%s",CommonUtil.currentUser.getUsername()));

        addLogoutListen();
        return root;
    }
    private void logoutLocal(){
        ((MainActivity)requireActivity()).onLogoutSuccess();
    }
    private void addLogoutListen(){
        btnLogout.setOnClickListener(v -> {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

            googleSignInClient.signOut()
                    .addOnCompleteListener(task -> {
                        // Chuyển về màn hình login
                        logoutLocal();
                    });
        });
    }
}