package com.example.bookjihc.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTabHost;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;

import com.example.bookjihc.BooksUserFragment;
import com.example.bookjihc.activities.MainActivity;
import com.example.bookjihc.databinding.ActivityDashBoardUserBinding;
import com.example.bookjihc.databinding.FragmentBooksUserBinding;
import com.example.bookjihc.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashBoardUserActivity extends AppCompatActivity {


    public ArrayList<ModelCategory> categoryArrayList;


    public ViewPagerAdapter viewPagerAdapter;

    private ActivityDashBoardUserBinding binding;


    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashBoardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        setupViewPagerAdaper(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);



        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(DashBoardUserActivity.this, MainActivity.class));
                finish();


            }
        });


        binding.ptofileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

              startActivity(new Intent(DashBoardUserActivity.this, ProfileActivity.class));

            }
        }


        );



    }


    private void setupViewPagerAdaper(ViewPager viewPager) {


        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);
        categoryArrayList = new ArrayList<>();

        categoryArrayList.clear();

        ModelCategory modelAll = new ModelCategory("01", "All", "", 1);
        ModelCategory modelMostViewed = new ModelCategory("02", "Most Viewed", "", 1);
        ModelCategory modelMostDownloaded = new ModelCategory("03", "Most Downloaded", "", 1);


        categoryArrayList.add(modelAll);
        categoryArrayList.add(modelMostViewed);
        categoryArrayList.add(modelMostDownloaded);


        viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                "" + modelAll.getId(),
                "" + modelAll.getCategory(),
                "" + modelAll.getUid()
        ), modelAll.getCategory());


        viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                "" + modelMostViewed.getId(),
                "" + modelMostViewed.getCategory(),
                "" + modelMostViewed.getUid()

        ), modelMostViewed.getCategory());

        viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                "" + modelMostDownloaded.getId(),
                "" + modelMostDownloaded.getCategory(),
                "" + modelMostDownloaded.getUid()

        ), modelMostDownloaded.getCategory());


        viewPagerAdapter.notifyDataSetChanged();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    categoryArrayList.add(model);

                    viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                            "" + model.getId(),
                            "" + model.getCategory(),
                            "" + model.getUid()), model.getCategory());

                    viewPagerAdapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onCancelled(DatabaseError error) {


            }
        });


        viewPager.setAdapter(viewPagerAdapter);


    }


    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<BooksUserFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;


        public ViewPagerAdapter(FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;

        }

        @Override
        public Fragment getItem(int position) {


            return fragmentList.get(position);
        }

        private void addFragment(BooksUserFragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);

        }


        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);

        }

        @Override
        public int getCount() {

            return fragmentList.size();
        }
    }


    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            binding.subTitleTv.setText("Жүйеге кірмеген");


        } else {
            String email = firebaseUser.getEmail();
            binding.subTitleTv.setText(email);


        }
    }


}