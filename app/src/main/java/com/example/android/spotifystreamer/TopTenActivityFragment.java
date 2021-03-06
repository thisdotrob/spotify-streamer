package com.example.android.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Detailed fragment which displays a user selected artist's top ten tracks in a list.
 */
public class TopTenActivityFragment extends Fragment {

    // Log tag
    private final String LOG_TAG = TopTenActivityFragment.class.getSimpleName();
    // Constant for the parcelable Tracks ArrayList used in onSaveInstanceState.
    private static final String STATE_TRACKS = "state tracks";
    // Instance variable for the RecyclerView to allow access in both onCreateView method (where
    // the layout manager is set) and setAdapter method (where adapter is set)
    private RecyclerView mRecyclerView;
    // Private variable for the artist's ID number.
    private String artistId;
    // Stores the list of tracks and associated variables
    private ArrayList<ParcelableTrack> trackList;
    // Constant for the country code used when requesting the top 10 tracks from Spotify API
    private static final String SEARCH_COUNTRY = "GB";

    // Default constructor
    public TopTenActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the MainActivityFragment bundle
        Bundle bundle = getActivity().getIntent().getExtras();
        // Retrieve the spotify Artist ID for the clicked on artist
        artistId = bundle.getString("EXTRA_ARTIST_ID");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_top_ten, container, false);
        // Set the layout manager for the RecyclerView.
        mRecyclerView = (RecyclerView) v.findViewById(R.id.rvTopTenResults);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(v.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        // If there is a savedInstanceState bundle, use its data to set the adapter.
        // Else, retrieve fresh data from spotify api wrapper and set adapter.
        if (savedInstanceState != null){
            trackList = savedInstanceState.getParcelableArrayList(STATE_TRACKS);
            setAdapter();
        } else {
            Map<String, Object> options = new HashMap<>();
            options.put("country", SEARCH_COUNTRY);
            SpotifyApi api = new SpotifyApi();
            SpotifyService service = api.getService();
            service.getArtistTopTrack(artistId, options, new Callback<Tracks>() {
                @Override
                public void success(final Tracks tracks, Response response) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                                // Convert the List<Track> into a ArrayList<ParcelableTrack>, store in the private
                                // instance variable in the outer class and set the adapter on the RecyclerView.
                                trackList = new ArrayList<>();
                                int imgSizeInPixels = (int) getResources().getDimension(
                                        R.dimen.thumbnail_size);
                                for(Track track : tracks.tracks){
                                    trackList.add(new ParcelableTrack(track,imgSizeInPixels));
                                }
                                setAdapter();
                        }
                    });
                }

                @Override
                public void failure(final RetrofitError error) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(LOG_TAG, error.getMessage());
                        }
                    });
                }
            });
        }
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_TRACKS, trackList);
    }

    // Set the adapter on the RecyclerView
    private void setAdapter() {
        TracksAdapter mTracksAdapter = new TracksAdapter(trackList);
        mRecyclerView.setAdapter(mTracksAdapter);
    }
}
