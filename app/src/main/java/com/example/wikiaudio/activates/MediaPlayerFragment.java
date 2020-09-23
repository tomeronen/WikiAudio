package com.example.wikiaudio.activates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.wikiaudio.AppData;
import com.example.wikiaudio.R;
import com.example.wikiaudio.WikiAudioApp;
import com.example.wikiaudio.playlist.Playlist;
import com.example.wikiaudio.wikipedia.WikipediaPlayer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ohoussein.playpause.PlayPauseView;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MediaPlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MediaPlayerFragment extends Fragment {

    PlayPauseView playButton;
    FloatingActionButton nextButton;
    FloatingActionButton previousButton;
    TextView title;
    WikipediaPlayer player;
    private boolean showPlayingData;
    private AppData appData;


    public void showTitle(boolean showPlayingData)
    {
        if(showPlayingData)
        {
            title.setVisibility(View.VISIBLE);
            title.bringToFront();
        }
        else
        {
            title.setVisibility(View.GONE);
        }
    }

    public MediaPlayerFragment() {
    }

    public MediaPlayerFragment(boolean showPlayingData) {
        this.showPlayingData = showPlayingData;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MediaPlayerFragment.
     */
    public static MediaPlayerFragment newInstance(String param1, String param2) {
        MediaPlayerFragment fragment = new MediaPlayerFragment(false);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentInflated =
                inflater.inflate(R.layout.fragment_media_player, container, false);
        previousButton = fragmentInflated.findViewById(R.id.previuosSoung);
        playButton = fragmentInflated.findViewById(R.id.play_pause);
        nextButton = fragmentInflated.findViewById(R.id.nextSoung);
        player = new WikipediaPlayer(this.getActivity(), Locale.ENGLISH,1f);
        title = fragmentInflated.findViewById(R.id.audioPlayingTitle);
        if(!showPlayingData)
        {
            title.setVisibility(View.GONE);
        }
        setOnClickButtons();
        return fragmentInflated;
    }

    private void setOnClickButtons() {
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // previous song logic:
                int curPosition = getAppData().getCurPosition();
                Playlist playlist = getAppData().getPlaylist();
                if(playlist != null
                        && curPosition > 0 // if we are in zero do nothing
                        && playlist.size() >= curPosition)
                {
                    --curPosition; // go back one step.
                    getAppData().setCurPositionInPlaylist(curPosition);
                    title.setText(playlist.get(curPosition).getTitle());
                    if(getAppData().getPlayingStatus()) // if we were playing, stop it.
                    {
                        player.stopPlaying();
                    }
                    else
                    {
                        playButton.toggle();
                    }
                    player.playWiki(playlist.get(curPosition));
                    getAppData().setPlayingStatus(true);
                }
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int curPosition = getAppData().getCurPosition();
                Playlist playlist = getAppData().getPlaylist();
                boolean currentlyPlaying = appData.getPlayingStatus();
                playButton.toggle();
                if(!canPlay(curPosition, playlist))
                {
                    playButton.toggle(); // doing toggle and return if needed makes cool animation.
                }
                if (currentlyPlaying) {
                    pausePlaying();
                    getAppData().setPlayingStatus(false);
                }
                // already playing -> pause.
                else {
                    startPlaying();
                    getAppData().setPlayingStatus(true);
                }

            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int curPosition = getAppData().getCurPosition();
                Playlist playlist = getAppData().getPlaylist();
                boolean playingStatus = appData.getPlayingStatus();
                if(playlist != null
                        && curPosition < (playlist.size() - 1)){
                    ++curPosition;
                    getAppData().setCurPositionInPlaylist(curPosition);
                    title.setText(playlist.get(curPosition).getTitle());
                    player.playWiki(playlist.get(curPosition));
                    if(!playingStatus)
                    {
                        // we were not playing.
                        playButton.toggle();
                    }
                    getAppData().setPlayingStatus(true);

                }
            }
        });
    }

    public void pausePlaying() {
        if(getAppData() != null)
        {
            appData.setPlayingStatus(false);
            player.pausePlaying();
        }
    }

    public void startPlaying() {
        int curPosition = getAppData().getCurPosition();
        Playlist playlist = getAppData().getPlaylist();
        boolean playingStatus = appData.getPlayingStatus();
        if(canPlay(curPosition, playlist))
        {
            Toast.makeText(getActivity(),
                    "start playing" + playlist.get(curPosition).getTitle(),
                    Toast.LENGTH_SHORT).show();
            player.playWiki(playlist.get(curPosition));
            title.setText(playlist.get(curPosition).getTitle());
            getAppData().setPlayingStatus(true);
        }
    }

    private boolean canPlay(int curPosition, Playlist playlist) {
        return playlist != null
                && !playlist.isEmpty()
                && playlist.get(curPosition)  != null;
    }

    private AppData getAppData()
    {
        if(appData != null)
        {
            return appData;
        }
        else
        {
            FragmentActivity activity = getActivity();
            if(activity != null)
            {
                appData = ((WikiAudioApp) activity.getApplication()).getAppData();
                return appData;
            }
        }
        return null;
    }

}