package com.example.android.travelmantics;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder> {
    private static final String TAG = "DealAdapter";

    ArrayList<TravelDeal> deals;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener;
    private Context context;


    public  DealAdapter(Context context, Activity a){

        firebaseDatabase = FireBaseUtil.firebaseDatabase;
        databaseReference = FireBaseUtil.databaseReference;
        this.context = context;
        deals = FireBaseUtil.Deals;
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TravelDeal td = dataSnapshot.getValue(TravelDeal.class);
                td.setId(dataSnapshot.getKey());
                deals.add(td);
                notifyItemInserted(deals.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseReference.addChildEventListener(childEventListener);
    }
    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.rv_row,parent,false);
    return new DealViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        TravelDeal deal = deals.get(position);
        holder.tvTitle.setText(deal.getTitle());
        holder.tvDescription.setText(deal.getDescription());
        holder.tvPrice.setText(deal.getPrice());

        String url = deal.getImageUrl();
        Log.d(TAG, "onBindViewHolder: " + url);

        if(url != null && !url.isEmpty()){

            Picasso.with(context)
                       .load(url)
                       .resize(80,80)
                       .centerCrop()
                       .into(holder.imageD);

        }
        else{
            Log.d(TAG, "showImage: No image");
        }

    }

    @Override
    public int getItemCount() {
        return deals.size();
    }

    public  class DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle,tvDescription,tvPrice;
        ImageView imageD;
        ConstraintLayout conts ;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            conts = (ConstraintLayout) itemView.findViewById(R.id.cont);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvDescription = (TextView) itemView.findViewById(R.id.tvDesc);
            tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
            imageD = (ImageView) itemView.findViewById(R.id.imageDeal);

            itemView.setOnClickListener(this);
        }

        public  void bind (TravelDeal deal){
            tvTitle.setText(deal.getTitle());
            tvDescription.setText(deal.getDescription());
            tvPrice.setText(deal.getPrice());

            showImage(deal.getImageUrl());
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            TravelDeal selectedDeal = deals.get(position);
            Intent intent = new Intent(view.getContext(),DealActivity.class);
            intent.putExtra("Deal",selectedDeal);
            view.getContext().startActivity(intent);
        }

        private void showImage(String url){
            if(url != null && !url.isEmpty()){
                Glide.with(context)
                        .load(url)
                        .into(imageD);
            }
            else{
                Log.d(TAG, "showImage: No image");
            }
        }
    }

}
