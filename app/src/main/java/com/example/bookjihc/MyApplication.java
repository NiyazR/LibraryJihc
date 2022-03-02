package com.example.bookjihc;

import static com.example.bookjihc.Canstants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.provider.SyncStateContract;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.bookjihc.adapters.AdaptePdfAdmin;
import com.example.bookjihc.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import kotlin.text.UStringsKt;

public class MyApplication extends Application {

    private static final String TAG_DOWNLOAD = "DOQNLOAD_TAG";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static final String formatTimestamp(long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy", cal).toString();

        return date;


    }


    public static void deleteBook(Context context, String bookId, String bookUrl, String bookTitle) {
        String Tag = "DELETED_BOOK_TAG";


        Log.d(Tag, "deleteBook:Жойылуда... ");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Күте тұрыңыз");


        progressDialog.setMessage("Жойылуда" + bookTitle + "...");
        progressDialog.show();

        Log.d(Tag, "deleteBook: Жадтан жойылуда...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(Tag, "onSuccess: Жадтан жойылды...");
                Log.d(Tag, "onSuccess: Енді db-ден ақпаратты жою");

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                reference.child(bookId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(Tag, "onSuccess: db-ден де жойылды");
                        progressDialog.dismiss();
                        Toast.makeText(context, "Кітаптар сәтті жойылды", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(Tag, "onFailure: Осыған байланысты db файлынан жойылмады " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                    Log.d(Tag, "onFailure: Жадтан жою мүмкін болмады" + e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }


    public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTv) {
        String Tag = "PDF_SIZE_TAG";


        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                double bytes = storageMetadata.getSizeBytes();
                Log.d(Tag, "onSuccess: " + pdfTitle + "" + bytes);

                double kb = bytes / 1024;
                double mb = kb / 1024;


                if (mb >= 1) {
                    sizeTv.setText(String.format("%.2f", mb) + "MB");


                } else if (kb >= 1) {
                    sizeTv.setText(String.format("%.2f", kb) + "KB");


                } else {
                    sizeTv.setText(String.format("%.2f", bytes) + "bytes");


                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(Tag, "onFailure: " + e.getMessage());
            }
        });
    }


    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar, TextView pagesTv) {

        String Tag = "PDF_LOAD_SINGLE_TAG";


        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {

                        Log.d(Tag, "onSuccess: " + pdfTitle + "файлды сәтті алды");


                        pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {

                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(Tag, "onError: " + t.getMessage());
                                    }
                                }).onPageError(new OnPageErrorListener() {
                            @Override
                            public void onPageError(int page, Throwable t) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(Tag, "onPageError: " + t.getMessage());
                            }
                        }).onLoad(new OnLoadCompleteListener() {
                            @Override
                            public void loadComplete(int nbPages) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(Tag, "loadComplete: pdf жүктелген ");


                                if (pagesTv != null) {

                                    pagesTv.setText("" + nbPages);
                                }

                            }
                        }).load();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Log.d(Tag, "onFailure: url файлынан алу мүмкін болмады  " + e.getMessage());

            }
        });


    }

    public static void loadCategory(String categoryId, TextView categoryTv) {


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String category = "" + snapshot.child("category").getValue();

                categoryTv.setText(category);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public static void incrementBookViewCount(String bookId) {


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String viewsCount = "" + snapshot.child("viewsCount").getValue();


                if (viewsCount.equals("") || viewsCount.equals("null")) {
                    viewsCount = "0";

                }

                Long newViewsCount = Long.valueOf(viewsCount) + 1;

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("viewsCount", newViewsCount);


                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                reference.child(bookId).updateChildren(hashMap);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public static void downloadBook(Context context, String bookId, String bookTitle, String bookUrl) {

        Log.d(TAG_DOWNLOAD, "downloadBook: кітапты жүктеп алу...");
        String namewithExtension = bookTitle + ".pdf";
        Log.d(TAG_DOWNLOAD, "downloadBook: NAME:" + namewithExtension);


        ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Күте тұрыңыз");
        progressDialog.setMessage("жүктеп алынуда" + namewithExtension + "..");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_PDF).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG_DOWNLOAD, "onSuccess: Кітапты жүктеп алу ");

                saveDownloadedBook(context, progressDialog, bytes, namewithExtension, bookId);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG_DOWNLOAD, "onFailure: Себебі жүктеп алу мүмкін болмады" + e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(context, "Себебі жүктеп алу мүмкін болмады" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private static void saveDownloadedBook(Context context, ProgressDialog progressDialog, byte[] bytes, String namewithExtension, String bookId) {
        Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Жүктеп алынған кітап сақталуда");
        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdirs();

            String filePath = downloadsFolder.getPath() + "/" + namewithExtension;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            Toast.makeText(context, "Жүктеп алу қалтасына сақталды", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Файл жүктеп алу үшін сақталды");
            progressDialog.dismiss();


            incrementBookDownloadCount(bookId);


        } catch (Exception e) {

            Log.d(TAG_DOWNLOAD, "saveDownloadedBook:Жүктеп алу қалтасына сақтау мүмкін болмады" + e.getMessage());
            Toast.makeText(context, "Жүктеп алу қалтасына сақтау мүмкін болмады" + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }

    }

    private static void incrementBookDownloadCount(String bookId) {
        Log.d(TAG_DOWNLOAD, "incrementBookDownloadCount: Кітапты жүктеу санының артуы ");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String downloadsCount = "" + snapshot.child("downloadsCount").getValue();
                Log.d(TAG_DOWNLOAD, "onDataChange: Жүктеп алулар саны:" + downloadsCount);


                if (downloadsCount.equals("") || downloadsCount.equals("nll")) {
                    downloadsCount = "0";
                }

                long newDownloadsCount = Long.parseLong(downloadsCount) + 1;


                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("downloadsCount", newDownloadsCount);


                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                reference.child(bookId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG_DOWNLOAD, "onSuccess: Жүктеп алулар саны жаңартылды....");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG_DOWNLOAD, "onFailure: Жүктеп алынғандар санын жаңарту мүмкін болмады " + e.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void loadPdfPageCount(Context context, String pdfUrl, TextView pagesTv) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        storageReference
                .getBytes(Canstants.MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {

                        PDFView pdfView = new PDFView(context, null);
                        pdfView.fromBytes(bytes)
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        pagesTv.setText("" + nbPages);

                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }

    public static void addToFavorite(Context context, String bookId) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {

            Toast.makeText(context, "Сіз жүйеге кірмегенсіз", Toast.LENGTH_SHORT).show();

        } else {
            long timestamp = System.currentTimeMillis();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("bookId", "" + bookId);
            hashMap.put("timestamp", "" + timestamp);


            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Таңдаулылар тізіміне қосылды...", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(context, "Себебі қосу мүмкін болмады" + e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });


        }


    }


    public static void removeFromFavorite(Context context, String bookId) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {

            Toast.makeText(context, "Сіз жүйеге кірмегенсіз", Toast.LENGTH_SHORT).show();

        } else {


            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Таңдаулылар тізімінен жойылды...", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(context, "себебінен жою мүмкін болмады" + e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });


        }


    }

}
