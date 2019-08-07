package com.example.android.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.data.model.Resource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {

    public static final int PICTURE_RESULT = 42;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    EditText txtTitle,txtDescription,txtPrice;
    TravelDeal deal;
    ImageView imageView;
    private  String currentUserID;
    private Uri imageuri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        firebaseDatabase = FireBaseUtil.firebaseDatabase;
        databaseReference = FireBaseUtil.databaseReference;


        currentUserID = FireBaseUtil.firebaseAuth.getCurrentUser().getUid();
        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtDescription = (EditText) findViewById(R.id.txtDesc);
        txtPrice = (EditText) findViewById(R.id.txtPrice);

        imageView = (ImageView) findViewById(R.id.imaged);
//        showImage(deal.getImageUrl());

        Button btnImage = findViewById(R.id.btnImage);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(intent.createChooser(intent,"Insert Picture"), PICTURE_RESULT);
            }
        });

        Intent intent = getIntent();
        TravelDeal deals = (TravelDeal) intent.getSerializableExtra("Deal");

        if(deals == null){
            deals = new TravelDeal();
        }
        this.deal = deals;
        txtTitle.setText(deal.getTitle());
        txtDescription.setText(deal.getDescription());
        txtPrice.setText(deal.getPrice());

        showImage(deal.getImageUrl());

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.save:
                saveDeal();
                Toast.makeText(this, "Deal Saved", Toast.LENGTH_LONG).show();
               clean();
               backToList();
                return  true;
            case R.id.delete:
                deleteDeal();
                Toast.makeText(this, "Deal Deleted", Toast.LENGTH_SHORT).show();
                backToList();
                return true;
                default:
                 return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu,menu);

        if(FireBaseUtil.isAdmin){
            menu.findItem(R.id.delete).setVisible(true);
            menu.findItem(R.id.save).setVisible(true);
            enableEditTexts(true);
            findViewById(R.id.btnImage).setEnabled(true);
        }else{
            menu.findItem(R.id.delete).setVisible(false);
            menu.findItem(R.id.save).setVisible(false);
            enableEditTexts(false);
            findViewById(R.id.btnImage).setEnabled(false);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICTURE_RESULT && resultCode == RESULT_OK){
//            create the uri and receive a data object from the intent which is the uri of our image
            imageuri = data.getData();

            uploadFile();
//            showImage(deal.getImageUrl());


        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploadFile(){

        //      set the reference of our storage
        StorageReference ref = FireBaseUtil.storageReference.child(currentUserID + ".jpg");

        //           upload the picture to Firebase storage
        ref.putFile(imageuri).addOnSuccessListener(this,new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                final String pictureName = taskSnapshot.getStorage().getPath();
                task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        deal.setImageUrl(uri.toString());
                        deal.setImageName(pictureName);
                        showImage(uri.toString());
                    }
                });

            }
        });

    }

    private void clean() {
        txtTitle.setText("");
        txtPrice.setText("");
        txtDescription.setText("");
        txtTitle.requestFocus();
    }

    private void saveDeal() {
        deal.setTitle(txtTitle.getText().toString());
        deal.setDescription(txtDescription.getText().toString());
        deal.setPrice(txtPrice.getText().toString());

//        to check if the id already exists

        if (deal.getId() == null){
            databaseReference.push().setValue(deal);
        }else{
            databaseReference.child(deal.getId()).setValue(deal);
        }
    }

    private  void deleteDeal(){
//        this will check whether the deal exists
        if(deal == null){
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child(deal.getId()).removeValue();

        if(deal.getImageName() != null && deal.getImageName().isEmpty() == false){
            StorageReference picRef = FireBaseUtil.storage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            });
        }
    }

    private  void backToList(){
        Intent intent = new Intent(this,ListActivity.class);
        startActivity(intent);
    }

    private  void  enableEditTexts(boolean isEnabled){
        txtTitle.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
    }

    private  void  showImage(String url){
           if(url != null && url.isEmpty() == false){
               int width = Resources.getSystem().getDisplayMetrics().widthPixels;
//               Picasso.with(this)
//                       .load(url)
//                       .resize(80,80)
//                       .centerCrop()
//                       .into(imageView);

               Log.d("DealActivity", "showImage: " + url);

               Glide.with(this)
                       .load(url)
                       .into(imageView);
           }

    }

}
