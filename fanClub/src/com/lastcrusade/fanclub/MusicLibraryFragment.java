package com.lastcrusade.fanclub;

import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockListFragment;
import com.lastcrusade.fanclub.model.Song;
import com.lastcrusade.fanclub.model.SongMetadata;
import com.lastcrusade.fanclub.service.MusicLibraryService;
import com.lastcrusade.fanclub.util.MusicListAdapter;
import com.lastcrusade.fanclub.util.Titleable;

public class MusicLibraryFragment extends SherlockListFragment implements Titleable {
    private final String TAG = "MusicLibraryFragment";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        //whatever our song list is needs to be passed to the musiclistadapter
        List<SongMetadata> musicLibrary = (((CoreActivity) this.getActivity()).getMusicLibraryService()).getLibrary();
        Song[] songs = new Song[musicLibrary.size()];
        for(int i=0; i<musicLibrary.size(); i++){
            songs[i] = new Song(musicLibrary.get(i));
            songs[i].getMetadata().setUsername("Reid");
        }
        CustomApp curApp = (CustomApp) this.getActivity().getApplication();
        setListAdapter(new MusicAdapter( this.getActivity(), songs, curApp.getUserList().getUsers()));
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // construct the RelativeLayout
        
        View v = inflater.inflate(R.layout.list, container, false);

        return v;
    }
    
    public void onResume(Bundle savedInstanceState){
        getActivity().setTitle(R.string.music_library);
    }
    
    private class MusicAdapter extends MusicListAdapter{

        public MusicAdapter(Context mContext, Song[] songs, Hashtable<String, String> users) {
            super(mContext, songs, users);
        }
        
        public View getView(int position, View convertView, ViewGroup parent){
            View v = super.getView(position, convertView, parent);
            
            v.findViewById(R.id.add_to_playlist).setVisibility(View.VISIBLE);
            
            
            return v;
        }
        
    }
    
    @Override
    public String getTitle() {
        return getString(R.string.music_library);
    }
    
    
}
