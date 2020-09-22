package com.example.wikiaudio.activates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.wikiaudio.R;
import com.example.wikiaudio.playlist.Playlist;
import com.example.wikiaudio.wikipedia.Wikipage;
import com.example.wikiaudio.wikipedia.WikipediaPlayer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ohoussein.playpause.PlayPauseView;

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

    private Playlist playableList;
    private int curPosition = 0;
    private Wikipage curPlaying;
    private boolean playingStatus = false;
    private boolean showPlayingData;



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
//        previousButton = fragmentInflated.findViewById(R.id.previuosSoung);
//        playButton = fragmentInflated.findViewById(R.id.play_pause);
//        nextButton = fragmentInflated.findViewById(R.id.nextSoung);
//        player = new WikipediaPlayer(this.getActivity(), Locale.ENGLISH,1f);
//        title = fragmentInflated.findViewById(R.id.audioPlayingTitle);
////        playableList =((WikiAudioApp) getActivity().getApplication()).getPlaylist();
//
//        if(!showPlayingData)
//        {
//            title.setVisibility(View.GONE);
//        }
//        if(playableList != null) {
//            curPlaying = playableList.get(curPosition);
//            title.setText(curPlaying.getTitle());
//        }
//        setOnClickButtons();
        return fragmentInflated;
    }

//    private void setOnClickButtons() {
//        previousButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // previous song logic:
//                if(playableList != null && curPosition > 0 && playableList.size() >= curPosition)
//                {
//                    --curPosition;
//                    curPlaying = playableList.get(curPosition);
//                    title.setText(curPlaying.getTitle());
//                    if(playingStatus)
//                    {
//                        player.stopPlaying();
//                    }
//                    else
//                    {
//                        playButton.toggle();
//                    }
//                    player.playWiki(curPlaying);
//                    playingStatus = true;
//                }
//            }
//        });
//
//        playButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(!playingStatus) // not already playing
//                {
//                    startPlaying();
//                    playButton.toggle();
//                    if(!playingStatus) // starting playing music failed.
//                    {
//                        playButton.toggle();
//                    }
//                }
//                else // already playing -> pause.
//                {
//                    pausePlaying();
//                    playButton.toggle();
//                    if(playingStatus) // pausing playing music failed.
//                    {
//                        playButton.toggle();
//                    }
//                }
//            }
//        });
//
//        nextButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(playableList != null && curPosition < playableList.size()){
//                    ++curPosition;
//                    curPlaying = playableList.get(curPosition);
//                    title.setText(curPlaying.getTitle());
//                    if(playingStatus)
//                    {
//                        player.stopPlaying();
//                    }
//                    else
//                    {
//                        playButton.toggle();
//                    }
//                    player.playWiki(curPlaying);
//                    playingStatus = true;
//                }
//            }
//        });
//    }
//
//    public void pausePlaying() {
//        player.pausePlaying();
//        playingStatus = false;
//    }
//
//    public void startPlaying() {
//        if(curPlaying != null)
//        {
//            Toast.makeText(getActivity(),  "start playing" + curPlaying.getTitle(), Toast.LENGTH_SHORT).show();
//            player.playWiki(curPlaying);
//            title.setText(curPlaying.getTitle());
//            playingStatus = !playingStatus;
//        }
//        else
//        {
//            if(playableList != null && playableList.size() > curPosition) {
//                curPlaying = playableList.get(curPosition);
//                Toast.makeText(getActivity(),  "start playing" + curPlaying.getTitle(), Toast.LENGTH_SHORT).show();
//                player.playWiki(curPlaying);
//                title.setText(curPlaying.getTitle());
//                playingStatus = !playingStatus;
//            }
//        }
//    }
//
//    /**
//     * resets the playlist to a new content, and starts playing from the first value.
//     * @param playableList the new value of the playlist.
//     */
//    public void updatePlayList(Playlist playableList, boolean startPlaying)
//    {
//        curPosition = 0;
//        if(playableList != null) {
//            this.playableList = playableList;
//
//            if(!playableList.isEmpty())
//            {
//                curPlaying = playableList.get(curPosition);
//                title.setText(curPlaying.getTitle());
//                if(startPlaying)
//                {
//                    player.playWiki(curPlaying);
//                    playingStatus = true;
//                }
//            }
//        }
//    }
//
//    /**
//     * resets the playlist to a new content.
//     * @param playableList the new value of the playlist.
//     * @param position the position from in the playlist to start.
//     */
//    public void updatePlayList(List<Wikipage> playableList, boolean startPlaying, int position)
//    {
//        curPosition = position;
//        if(playableList != null) {
//            this.playableList = playableList;
//            if (playableList.size() > position) {
//                curPlaying = playableList.get(curPosition);
//                title.setText(curPlaying.getTitle());
//                if (startPlaying) {
//                    player.playWiki(curPlaying);
//                    playingStatus = true;
//                }
//
//            }
//        }
//    }
//
//    /**
//     * adds a wikipage to the playlist.
//     * @param wikipage the wikipage to be add.
//     * @param skipToWiki skip to the add wikipage.
//     */
//    public void addWikiToPlayList(Wikipage wikipage, boolean skipToWiki)
//    {
//        if(wikipage == null)
//        {
//            return;
//        }
//
//        if(playableList != null) {
//
//            this.playableList.add(wikipage);
//            if(skipToWiki)
//            {
//                curPosition = this.playableList.size() - 1;
//                curPlaying = playableList.get(curPosition);
//                title.setText(curPlaying.getTitle());
//                player.playWiki(curPlaying);
//            }
//        }
//        else
//        {
//            this.playableList =((WikiAudioApp) getActivity().getApplication()).getPlaylist();
//            this.playableList.add(wikipage);
//            if(skipToWiki) {
//                title.setText(wikipage.getTitle());
//                player.playWiki(wikipage);
//
//            }
//        }
//    }
}