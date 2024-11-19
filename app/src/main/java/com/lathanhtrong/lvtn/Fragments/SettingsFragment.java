package com.lathanhtrong.lvtn.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lathanhtrong.lvtn.Models.User;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.Values;
import com.lathanhtrong.lvtn.databinding.FragmentSettingsBinding;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private GoogleSignInClient googleSignInClient;
    int RC_SIGN_IN = 30;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Values.webClientId)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        loadLanguage(requireContext());
        loadModel(requireContext());
        binding.languageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageDialog();
            }
        });
        binding.modelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModelDialog();
            }
        });
        binding.modelConfigBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.frame_layout, new ModelConfigFragment()).commit();
            }
        });
        binding.modelLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        binding.modelSignOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Auth.GoogleSignInApi.signOut(googleSignInClient.asGoogleApiClient()).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        auth.signOut();
                    }
                });
                binding.modelLoginBtn.setVisibility(View.VISIBLE);
                binding.modelSignOutBtn.setVisibility(View.GONE);
            }
        });
    }

    public void signIn() {
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuth(account.getIdToken());

        } catch (ApiException e) {
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuth(String idToken) {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage(getResources().getString(R.string.signing_in));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        String uid = auth.getCurrentUser().getUid();
                        String name = auth.getCurrentUser().getDisplayName();
                        String email = auth.getCurrentUser().getEmail();
                        String photo = auth.getCurrentUser().getPhotoUrl().toString();
                        User user = new User(uid, name, email, photo);

                        DatabaseReference ref = FirebaseDatabase.getInstance(Values.region).getReference("Users").child(uid);
                        ref.setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.dismiss();
                                Toast.makeText(requireContext(), getResources().getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                                binding.modelLoginBtn.setVisibility(View.GONE);
                                binding.modelSignOutBtn.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            binding.modelLoginBtn.setVisibility(View.GONE);
            binding.modelSignOutBtn.setVisibility(View.VISIBLE);
        }
        else {
            binding.modelLoginBtn.setVisibility(View.VISIBLE);
            binding.modelSignOutBtn.setVisibility(View.GONE);
        }
    }

    private void showModelDialog() {
        final String[] models = {"YOLOv11-GhostNet","YOLOv11", "YOLOv10"};
        SharedPreferences preferences = getContext().getSharedPreferences("MODEL_SETTINGS", getContext().MODE_PRIVATE);
        int item = preferences.getInt("item", 0);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.select_model);
        builder.setSingleChoiceItems(models, item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                if (position == 0) {
                    setModel(requireContext(), Values.YOLOCustom, 0);
                    getFragmentManager().beginTransaction().detach(SettingsFragment.this).commit();
                    getFragmentManager().beginTransaction().attach(SettingsFragment.this).commit();
                } else if (position == 1) {
                    setModel(requireContext(), Values.YOLOv8n, 1);
                    getFragmentManager().beginTransaction().detach(SettingsFragment.this).commit();
                    getFragmentManager().beginTransaction().attach(SettingsFragment.this).commit();
                }
                else if (position == 2) {
                    setModel(requireContext(), Values.YOLOv10n, 2);
                    getFragmentManager().beginTransaction().detach(SettingsFragment.this).commit();
                    getFragmentManager().beginTransaction().attach(SettingsFragment.this).commit();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showLanguageDialog() {
        final String[] languages = {"English", "Tiếng Việt"};

        SharedPreferences preferences = getContext().getSharedPreferences("LANGUAGE_SETTINGS", getContext().MODE_PRIVATE);
        int item = preferences.getInt("item", 0);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.select_language);
        builder.setSingleChoiceItems(languages, item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                Intent i = getActivity().getIntent();
                if (position == 0) {
                    setLanguage(requireContext(),"en", 0);
                    i.putExtra("LOAD_SETTINGS_FRAGMENT", true);
                    getActivity().finish();
                    startActivity(i);
                } else if (position == 1) {
                    setLanguage(requireContext(),"vi", 1);
                    i.putExtra("LOAD_SETTINGS_FRAGMENT", true);
                    getActivity().finish();
                    startActivity(i);
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static void setModel(Context context, String model, int item) {
        SharedPreferences.Editor editor = context.getSharedPreferences("MODEL_SETTINGS", context.MODE_PRIVATE).edit();
        editor.putString("model", model);
        editor.putInt("item", item);
        editor.apply();
    }

    public static void setLanguage(Context context, String language, int item) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = context.getSharedPreferences("LANGUAGE_SETTINGS", context.MODE_PRIVATE).edit();
        editor.putString("language", language);
        editor.putInt("item", item);
        editor.apply();
    }

    public static void loadModel(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("MODEL_SETTINGS", Context.MODE_PRIVATE);
        String model = preferences.getString("model", Values.YOLOCustom);
        int item = preferences.getInt("item", 0);
        setModel(context, model, item);
    }

    public static void loadLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("LANGUAGE_SETTINGS", Context.MODE_PRIVATE);
        String language = preferences.getString("language", "en");
        int item = preferences.getInt("item", 0);
        setLanguage(context ,language, item);
    }

}