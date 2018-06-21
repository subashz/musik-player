package com.blacpythoz.musik.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blacpythoz.musik.R;
import com.blacpythoz.musik.models.AlbumModel;
import com.blacpythoz.musik.models.ArtistModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by deadsec on 9/4/17.
 */

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder>{
    List<ArtistModel> artists;
    Context context;

    public ArtistAdapter(List<ArtistModel> artists, Context context) {
        this.artists = artists;
        this.context = context;
    }

    @Override
    public ArtistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_artist,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ArtistAdapter.ViewHolder holder, int position) {
        ArtistModel artistModel = artists.get(position);
        Picasso.with(context).load(artistModel.getArtistImage()).placeholder(R.drawable.default_album_icon).into(holder.artistImage);
        holder.artistName.setText(artistModel.getArtistName());
        holder.songCount.setText(artistModel.getSongCount()+"");
        holder.albumCount.setText(artistModel.getAlbumCount()+"");
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView artistImage;
        TextView artistName;
        TextView songCount;
        TextView albumCount;
        public ViewHolder(View itemView) {
            super(itemView);
            artistImage = (ImageView)itemView.findViewById(R.id.iv_artist_coverart);
            artistName = (TextView)itemView.findViewById(R.id.tv_artist_name);
            songCount = (TextView)itemView.findViewById(R.id.tv_artist_song_count);
            albumCount = (TextView)itemView.findViewById(R.id.tv_artist_album_count);
        }
    }
}
