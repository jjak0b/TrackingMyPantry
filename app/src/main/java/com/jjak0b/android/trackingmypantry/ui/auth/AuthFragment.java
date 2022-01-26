package com.jjak0b.android.trackingmypantry.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.jjak0b.android.trackingmypantry.R;

public class AuthFragment extends Fragment {

    public AuthFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button gotoSignIn = view.findViewById(R.id.btnGotoSignIn);
        final Button gotoSignUp = view.findViewById(R.id.btnGotoSignUp);

        gotoSignIn.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate( R.id.action_nav_auth_to_nav_signin );
        });
        gotoSignUp.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate( R.id.action_nav_auth_to_nav_signup );
        });

    }
}