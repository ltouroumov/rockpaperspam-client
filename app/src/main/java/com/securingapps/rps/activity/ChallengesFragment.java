package com.securingapps.rps.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.securingapps.rps.R;
import com.securingapps.rps.data.*;
import com.securingapps.rps.events.InvalidateGameList;
import com.securingapps.rps.utils.UiRunner;
import com.securingapps.rps.utils.async.AsyncFuture;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ChallengesFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String TAG = ChallengesFragment.class.getSimpleName();
    private ChallengeAdapter challengesAdapter;

    private String status;
    private OnChallengeSelected listener;

    public ChallengesFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ChallengesFragment.
     */
    public static ChallengesFragment newInstance(String status) {
        ChallengesFragment fragment = new ChallengesFragment();
        Bundle args = new Bundle();
        args.putString("status", status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        status = getArguments().getString("status", "O");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_challenges, container, false);

        challengesAdapter = new ChallengeAdapter();
        ListView challenges = (ListView) view.findViewById(R.id.challenges);
        challenges.setAdapter(challengesAdapter);
        challenges.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "Challenges resumed");
        AsyncFuture.runAsync(() -> this.reloadChallenges(null));
    }

    private void buildChallengeList(Game[] games) {

        Challenge[] challenges = new Challenge[games.length];

        for (int n = 0; n < games.length; n++) {
            challenges[n] = new Challenge(games[n]);
        }

        getActivity().runOnUiThread(() -> {
            challengesAdapter.update(challenges);
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChallengeSelected) {
            listener = (OnChallengeSelected) context;
        } else {
            Log.w(TAG, "Context is not a listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
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

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void reloadChallenges(InvalidateGameList evt) {
        Game[] games = ApiProxy.getInstance()
            .doGetJson(Game[].class, "games?status=%s", status);

        buildChallengeList(games);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (listener != null) {
            listener.onChallengeSelected(this, challengesAdapter.getItem(position));
        }
    }

    private class ChallengeAdapter extends BaseAdapter {

        private Challenge[] challenges;

        public ChallengeAdapter() {
            this.challenges = new Challenge[0];
        }

        public void update(Challenge[] challenges) {
            this.challenges = challenges;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return challenges.length;
        }

        @Override
        public Challenge getItem(int position) {
            return challenges[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(ChallengesFragment.this.getContext());
                view = layoutInflater.inflate(R.layout.challenge_cell, parent, false);
            }

            Challenge challenge = getItem(position);

            TextView opponent = (TextView)view.findViewById(R.id.opponent);
            opponent.setText(getString(R.string.versus, "..."));
            FriendService.getInstance()
                .getFriend(challenge.getOpponentId())
                .whenCompleteAsync(friend -> opponent.setText(getString(R.string.versus, friend.getDisplayName())), new UiRunner(getActivity()));

            TextView bet = (TextView)view.findViewById(R.id.betAmount);
            bet.setText(getString(R.string.rounds_fmt, challenge.getCurrentRound(), challenge.getTotalRounds()));

            // LinearLayout rounds = (LinearLayout)view.findViewById(R.id.rounds);
            TextView rounds = (TextView)view.findViewById(R.id.tmpRounds);
            String[] tmpRounds = new String[challenge.getTotalRounds()];
            for (int round = 0; round < challenge.getTotalRounds(); round++) {
                String roundResult = challenge.getRounds()[round].symbol;
                tmpRounds[round] = roundResult;
            }
            rounds.setText(TextUtils.join(" ", tmpRounds));

            return view;
        }
    }

    interface OnChallengeSelected {
        void onChallengeSelected(ChallengesFragment sender, Challenge challenge);
    }
}
