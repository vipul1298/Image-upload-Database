package android.example.imagestorage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
   private static final int IMAGE_REQUEST=1;
   private Button mchoose,mupload;
   private ImageView mimage;
   private Uri mImageUri;
   private EditText medit;

   private StorageReference mstorage;
   private DatabaseReference mreff;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mchoose=findViewById(R.id.choose);
        mupload=findViewById(R.id.upload);
        mimage=findViewById(R.id.image);
//        medit=findViewById(R.id.edit);
        mstorage= FirebaseStorage.getInstance().getReference("uploads");
        mreff= FirebaseDatabase.getInstance().getReference("uploads");

        mchoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFile();
            }
        });
        mupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              uploadFile();
            }
        });
    }
    private void openFile(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);

    }
    private String getExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    private void uploadFile(){
        if(mImageUri!=null){
          StorageReference fileref = mstorage.child(System.currentTimeMillis()+ "." +getExtension(mImageUri));
          fileref.putFile(mImageUri)
                  .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                      @Override
                      public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                          Upload upload = new Upload(taskSnapshot.getTask().toString());
                          String uploadId = mreff.push().getKey();
                          mreff.child(uploadId).setValue(upload);
                          Toast.makeText(MainActivity.this, "Uploaded Successfully", Toast.LENGTH_LONG).show();
                      }
                  })
                  .addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {
                          Toast.makeText(MainActivity.this, "Not uploaded", Toast.LENGTH_SHORT).show();
                      }
                  })
                  .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                      @Override
                      public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                      }
                  });
        }else{
            Toast.makeText(this, "Choose image first", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            mImageUri =data.getData();
            Picasso.get().load(mImageUri).into(mimage);
        }
    }
}
