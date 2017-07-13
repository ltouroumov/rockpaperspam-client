package com.securingapps.rps.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.securingapps.rps.R;
import com.securingapps.rps.data.ApiProxy;
import com.securingapps.rps.data.ContactService;
import com.securingapps.rps.events.InvalidateGameList;
import com.securingapps.rps.utils.UiRunner;
import com.securingapps.rps.utils.async.AsyncFuture;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Use the {@link LobbyHeaderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LobbyHeaderFragment extends Fragment {

    private static final int AUTO_REFRESH_RATE = 60 * 1000;

    private TextView playerName;
    private TextView victoriesText;
    private TextView defeatsText;
    private TextView energyText;
    private Handler handler;
    private boolean refresh = false;
    private boolean active = false;
    private StatsResponse lastResponse;
    private View energyView;
    private View lobbyHeader;
    private PopupWindow pw;
    private ViewGroup containerView;

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

        containerView = container;
        lobbyHeader = (View) view.findViewById(R.id.lobbyHeader);

        playerName = (TextView) view.findViewById(R.id.playerName);
        playerName.setText(getString(R.string.hello, ContactService.getInstance().getProfile().getName()));

        victoriesText = (TextView) view.findViewById(R.id.counterVictories);
        victoriesText.setText("\u2205");
        defeatsText = (TextView) view.findViewById(R.id.counterDefeats);
        defeatsText.setText("\u2205");
        energyText = (TextView) view.findViewById(R.id.energyValue);
        energyText.setText("\u2205");

        energyView = (View) view.findViewById(R.id.energyView);
        energyView.setOnClickListener(this::showEnergyPopup);

        return view;
    }

    private void showEnergyPopup(View view) {
        try {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.energy_info_popup, (ViewGroup)containerView);

            if (lastResponse != null) {
                TextView maxEnergyText = (TextView) layout.findViewById(R.id.maxEnergy);
                maxEnergyText.setText(getString(R.string.maximum_energy, lastResponse.energy.pool_size));

                TextView regenRateText = (TextView) layout.findViewById(R.id.regenRate);
                regenRateText.setText(getString(R.string.regeneration_rate, lastResponse.energy.regen_rate));
            }

            pw = new PopupWindow(layout,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

            pw.showAsDropDown(energyText, 0, 0, Gravity.CENTER);

            layout.setOnClickListener(this::hideEnergyPopup);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void hideEnergyPopup(View view) {
        if (pw != null) {
            pw.dismiss();
            pw = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void updateStats(InvalidateGameList evt) {
        AsyncFuture.supplyAsync(() ->
            ApiProxy.getInstance().doGetJson(StatsResponse.class, "status")
        ).whenCompleteAsync(stats -> {
            if (stats != null) {
                lastResponse = stats;

                victoriesText.setText(Integer.toString(stats.stats.won));
                defeatsText.setText(Integer.toString(stats.stats.lost));
                energyText.setText(Integer.toString(stats.energy.current_level));
                if (stats.energy.current_level == 0) {
                    energyText.setTextAppearance(getContext(), R.style.counterValueLow);
                } else {
                    energyText.setTextAppearance(getContext(), R.style.counterValue);
                }
            }

            if (refresh) {
                handler.postDelayed(() -> this.updateStats(null), AUTO_REFRESH_RATE);
            } else {
                active = false;
            }
        }, new UiRunner(getActivity()));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        refresh = true;
        if (!active) {
            active = true;
            handler.postDelayed(() -> this.updateStats(null), AUTO_REFRESH_RATE);
        }
        updateStats(null);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        refresh = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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
        public float regen_rate;
        public int current_level;

    }
}
