package com.parsons.bakery.ui.acct;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;
import com.parsons.bakery.Settings;
import com.parsons.bakery.databinding.FragmentAcctBinding;

public class AcctFragment extends Fragment {

    private FragmentAcctBinding binding;
    public static View rootBase = null;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAcctBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        rootBase = root;
        DBHandler dbHandler = new DBHandler(getContext());

        View navBar = getActivity().getWindow().findViewById(R.id.nav_view);
        if (navBar != null) {
            int navBarHeight = navBar.getHeight();
            (root.findViewById(R.id.AccountLayout)).setPadding(0, 0, 0, navBarHeight);
        }

        if (dbHandler.executeOne("SELECT * FROM acct").isEmpty()) {
            root.findViewById(R.id.isSignedIn).setVisibility(View.GONE);
            root.findViewById(R.id.notSignedIn).setVisibility(View.VISIBLE);
            Button signIn = root.findViewById(R.id.signIn);
            Button signUp = root.findViewById(R.id.signUp);
            signIn.setOnClickListener(v -> {
                startActivity(new Intent(getContext(), Login.class));
                //DialogFragment dialogFragment = new LoginDialog(getContext(), this, root.findViewById(R.id.notSignedIn));
                //dialogFragment.show(getChildFragmentManager(), "Login");
            });
            signUp.setOnClickListener(view -> root.getContext().startActivity(new Intent(getContext(), Registration.class)));
        } else {
            root.findViewById(R.id.isSignedIn).setVisibility(View.VISIBLE);
            root.findViewById(R.id.notSignedIn).setVisibility(View.GONE);
            Fragment childFragment = getChildFragmentManager().findFragmentById(R.id.fragContainer);
            if (childFragment == null) {
                getChildFragmentManager().beginTransaction()
                        .add(R.id.fragContainer, new Settings())
                        .commit();

            }
        }
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

