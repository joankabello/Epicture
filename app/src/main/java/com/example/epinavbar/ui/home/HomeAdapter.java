package com.example.epinavbar.ui.home;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.epinavbar.R;
import com.example.epinavbar.ui.dashboard.model.Photo;
import com.squareup.picasso.Picasso;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.PhotoVH> {
    private Context mContext;
    private List<Photo> photos;
    private LayoutInflater layoutInflater;

    public HomeAdapter(Context context, List<Photo> photos) {
        this.mContext = context;
        this.photos = photos;
        layoutInflater = LayoutInflater.from(context);
    }

    public void updatePhotosList(List<Photo> list){
        photos = list;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoVH holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
    @Override
    public PhotoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PhotoVH(layoutInflater.inflate(R.layout.item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoVH holder, int position) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(mContext).load("https://i.imgur.com/" + photos.get(position).getId() + ".gif").apply(requestOptions).into(holder.photo);
        Picasso.with(mContext)
                .load("https://i.imgur.com/" + photos.get(position).getId() + ".jpg")
                .into(holder.photo);
        holder.upvote.setText(photos.get(position).getUpvote());
        holder.downvote.setText(photos.get(position).getDownvote());
        holder.views.setText(photos.get(position).getViews());
        holder.title.setText(photos.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class PhotoVH extends RecyclerView.ViewHolder {
        private AppCompatImageView photo;
        private AppCompatTextView title;
        private AppCompatTextView views;
        private AppCompatTextView upvote;
        private AppCompatTextView downvote;

        public PhotoVH(View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.photo);
            title = itemView.findViewById(R.id.title);
            views = itemView.findViewById(R.id.views);
            upvote = itemView.findViewById(R.id.upvote);
            downvote = itemView.findViewById(R.id.downvote);
        }
    }
}
