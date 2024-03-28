package com.example.myapplication;

import android.net.Uri;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;

public class EditProfileActivity extends AppCompatActivity {


    private static final String TAG = "EditProfileActivity";
    RadioGroup sexe;
    RadioButton rdM, rdF;
    private Button saveProfile;
    private EditText fnameTxt, lnameTxt, phoneTxt,emailTxt;
    private ImageView imageProfile;
    private FirebaseAuth auth;
    private FirebaseFirestore fstore;
    private DocumentSnapshot docuser;

    private String randomKey;
    private Uri imageUri, imageOldUri;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        auth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        saveProfile = findViewById(R.id.idBtnSave);
        fnameTxt = findViewById(R.id.idRegisterfname);
        lnameTxt = findViewById(R.id.idRegisterlname);
        phoneTxt = findViewById(R.id.idRegisterPhone);
        emailTxt = findViewById(R.id.idRegisterEmail);
        emailTxt.setEnabled(false);
        sexe =(RadioGroup) findViewById(R.id.radioGroup2);
        rdM = (RadioButton) findViewById(R.id.radioButton2);
        rdF = (RadioButton) findViewById(R.id.radioButton3);
        imageProfile = findViewById(R.id.imageProfile);

        //Change image profile
        imageProfile.setOnClickListener(e->{
            choosePicture();
        });
        //Save change profile
        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selected = sexe.getCheckedRadioButtonId();
                RadioButton gender= findViewById(selected);

                DocumentReference docref = fstore.collection("freelancers").document(auth.getCurrentUser().getUid());
                HashMap<String,Object> user = new HashMap<>();
                user.put("firstname", fnameTxt.getText().toString());
                user.put("lastname", lnameTxt.getText().toString());
                user.put("email", auth.getCurrentUser().getEmail());
                user.put("profile", String.valueOf(imageUri));
                user.put("phone", phoneTxt.getText().toString());
                user.put("sexe", gender.getText().toString());
                docref.update(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(EditProfileActivity.this, LocationActivity.class));
                        }
                    }
                });
            }
        });
    }

    //Choose and save profile image
    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/**");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }
    private void uploadPicture() {
        Toast.makeText(this, "Uploaaaaaaad", Toast.LENGTH_LONG).show();
        randomKey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("images/"+ randomKey);
        riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Snackbar.make(findViewById(android.R.id.content), "Image Uploaded", Snackbar.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(EditProfileActivity.this, "Failed To Upload!", Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    protected void onActivityResult(int reqCode, int resCode, @Nullable Intent data){
        super.onActivityResult(reqCode, resCode, data);
        if(reqCode==1 && resCode==RESULT_OK){
            imageUri=data.getData();
            imageProfile.setImageURI(imageUri);
            //uploadPicture();
        }
    }

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    //clicked menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch ((item.getItemId())){
            case R.id.idMaps:
                startActivity(new Intent(EditProfileActivity.this, LocationActivity.class));
                return true;
            case R.id.idAction:
                startActivity(new Intent(EditProfileActivity.this, ActionActivity.class));
                return true;
            case R.id.idLogout:
                startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(auth.getCurrentUser() != null) {

            DocumentReference docref = fstore.collection("freelancers").document(auth.getCurrentUser().getUid());
            docref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();

                        if(doc.exists()){
                            phoneTxt.setText(doc.get("phone").toString());
                            emailTxt.setText(doc.get("email").toString());
                            fnameTxt.setText(doc.get("firstname").toString());
                            lnameTxt.setText(doc.get("lastname").toString());
                            if(doc.get("sexe").equals("Male")){
                                rdM.setChecked(true);
                                rdF.setChecked(false);
                            }
                            else {
                                rdM.setChecked(false);
                                rdF.setChecked(true);
                            }
                            imageProfile.setImageURI(Uri.parse(doc.get("profile").toString()));
                        }else{
                            Toast.makeText(EditProfileActivity.this, "USer nulll", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "no data");
                        }
                    }
                }
            });

        }else {
            docuser = null;
        }
    }
}