package com.securingapps.rps.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import com.securingapps.rps.R;
import com.securingapps.rps.data.ApiProxy;
import com.securingapps.rps.data.ContactService;
import com.securingapps.rps.utils.UiRunner;
import com.securingapps.rps.utils.async.AsyncFuture;

/**
 * Use the {@link LobbyHeaderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LobbyHeaderFragment extends Fragment {
    private TextView playerName;
    private TextView victoriesText;
    private TextView defeatsText;
    private TextView energyText;
    private Handler handler;
    private boolean refresh;

    public LobbyHeaderFragment() {
        // Required empty public constructor
        handler = new Handler();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LobbyHeaderFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LobbyHeaderFragment newInstance() {
        LobbyHeaderFragment fragment = new LobbyHeaderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lobby_header, container, false);

        playerName = (TextView) view.findViewById(R.id.playerName);
        playerName.setText(getString(R.string.hello, ContactService.getInstance().getProfile().getName()));

        victoriesText = (TextView) view.findViewById(R.id.counterVictories);
        victoriesText.setText("\u2205");
        defeatsText = (TextView) view.findViewById(R.id.counterDefeats);
        defeatsText.setText("\u2205");
        energyText = (TextView) view.findViewById(R.id.energyValue);
        energyText.setText("\u2205");

        return view;
    }


    private void updateStats() {
        AsyncFuture.supplyAsync(() ->
            ApiProxy.getInstance().doGetJson(StatsResponse.class, "status")
        ).whenCompleteAsync(stats -> {
            if (stats != null) {
                victoriesText.setText(Integer.toString(stats.stats.won));
                defeatsText.setText(Integer.toString(stats.stats.lost));
                energyText.setText(Integer.toString(stats.energy.current_level));
            }

            if (refresh)
                handler.postDelayed(this::updateStats, 10000);
        }, new UiRunner(getActivity()));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        refresh = true;
        handler.postDelayed(this::updateStats, 1000);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        refresh = false;
    }

    private static class StatsResponse {

        public String id;
        public GameStats stats;
        public EnergyInfo energy;

    }

    private static class GameStats {

        public int played;
        public int won;
        public int lost;
        public int draw;

    }

    private static class EnergyInfo {

        public int pool_size;
        public int regen_rate;
        public int current_level;

    }
}
