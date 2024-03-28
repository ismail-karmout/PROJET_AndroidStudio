package com.example.myapplication;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity" ;
    private Button btnRegister;
    private EditText emailTxt, passwordTxt, confirmPasswordTxt,fnameTxt, lnameTxt, phoneTxt;
    private TextView errorTxt;
    private RadioGroup sexe;

    private FirebaseAuth auth;
    private FirebaseFirestore fstore;
    private String userID;
    private String randomKey;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;
    private String[] cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private String[] storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};;
    private static final int IMAGE_PICK_CAMERA_CODE = 102;
    private static final int IMAGE_PICK_GALLERY_CODE = 103;

    private ImageView imageProfile;
    private Uri imageUri;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailTxt = findViewById(R.id.idRegisterEmail);
        passwordTxt = findViewById(R.id.idRegisterPassword);
        confirmPasswordTxt = findViewById(R.id.idRegisterConfirmPassword);
        fnameTxt = findViewById(R.id.idRegisterfname);
        lnameTxt = findViewById(R.id.idRegisterlname);
        phoneTxt = findViewById(R.id.idRegisterPhone);
        errorTxt = findViewById(R.id.idTextErrorView);
        btnRegister = findViewById(R.id.idBtnRegister);
        imageProfile = findViewById(R.id.idImageProfile);
        sexe = findViewById(R.id.radioGroup);

        auth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        // save image
        imageProfile.setOnClickListener(e->{
            optionChooseImage();
        });
        //Register info
        btnRegister.setOnClickListener(e->{
            errorTxt.setVisibility(View.GONE);

            String txtEmail = emailTxt.getText().toString();
            String txtPassword = passwordTxt.getText().toString();
            String txtconfirmPassword = confirmPasswordTxt.getText().toString();
            String txtfname = fnameTxt.getText().toString();
            String txtlname = lnameTxt.getText().toString();
            String txtphone = phoneTxt.getText().toString();
            int selected = sexe.getCheckedRadioButtonId();
            RadioButton gender=(RadioButton) findViewById(selected);
            String sexeUser = gender.getText().toString();

            if (TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword)
                    || TextUtils.isEmpty(txtconfirmPassword) || TextUtils.isEmpty(txtfname) || TextUtils.isEmpty(sexeUser)
                    || TextUtils.isEmpty(txtlname) || TextUtils.isEmpty(txtphone) || TextUtils.isEmpty(randomKey)){
                Toast.makeText(RegisterActivity.this, "Empty credentials!", Toast.LENGTH_SHORT).show();
                errorTxt.setText("Empty credentials!");
                errorTxt.setVisibility(View.VISIBLE);
            } else if (txtPassword.length() < 6){
                Toast.makeText(RegisterActivity.this, "Password too short!", Toast.LENGTH_SHORT).show();
            } else if (!txtconfirmPassword.equals(txtPassword)){
                Toast.makeText(RegisterActivity.this, "Password and confirm password not matched", Toast.LENGTH_SHORT).show();

            }else {
                registerUser(txtEmail , txtPassword, txtfname, txtlname, txtphone, sexeUser);
            }
        });
    }

    private void optionChooseImage(){
        String[] options = {"Camera","Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }else {
                        pickFromCamera();
                    }
                }else if(i == 1) {
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }else {
                        choosePicture();
                    }
                }
            }
        });
        builder.create().show();
    }
    // Photo from camera
    private void pickFromCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Image title");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Image description");
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
        }
    }

    // Chose image from gallery
    private void choosePicture() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICK_GALLERY_CODE);
    }
    // save image in db
    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CAMERA_CODE && resultCode == RESULT_OK) {
            imageProfile.setImageURI(imageUri);
            uploadPicture();
        }

            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == IMAGE_PICK_CAMERA_CODE && resultCode == RESULT_OK) {
                imageProfile.setImageURI(imageUri);
                uploadPicture();
            } else if (requestCode == IMAGE_PICK_GALLERY_CODE && resultCode == RESULT_OK) {
                imageUri = data.getData();
                imageProfile.setImageURI(imageUri);
                uploadPicture();
            }

    }

    private void uploadPicture() {
        randomKey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("images/" + randomKey);
        riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(RegisterActivity.this, "Image uploaded!", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(RegisterActivity.this, "Failed To Upload!", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Check permission
    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }

    private void registerUser(String email, String password,String fname,String lname,String phone, String sexe) {

        auth.createUserWithEmailAndPassword(email ,password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(RegisterActivity.this, "Registering user successfully", Toast.LENGTH_SHORT).show();
                    userID = auth.getCurrentUser().getUid();
                    DocumentReference docref = fstore.collection("freelancers").document(userID);

                    // add data with empty
                    HashMap<String,String> user = new HashMap<>();
                    user.put("firstname", fname);
                    user.put("lastname", lname);
                    user.put("phone", phone);
                    user.put("email", email);
                    user.put("sexe", sexe);
                    user.put("profile", String.valueOf(imageUri));
                    docref.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG,"user profile created with Id"+userID);
                        }
                    });

                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                }else {
                    Toast.makeText(RegisterActivity.this, "Registering user failed", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}