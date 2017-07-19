package com.tricktrap.rps.activity;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.tricktrap.rps.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class GameSetupFragment extends Fragment {

    private static final String TAG = GameSetupFragment.class.getSimpleName();

    private EditText roundLengthView;
    private TextView opponentName;
    private int gameLength = 3;

    private OnGameStartListener listener;
    private Button betPlus;
    private Button betMinus;

    public static GameSetupFragment newInstance(String opponent) {
        GameSetupFragment self = new GameSetupFragment();
        Bundle params = new Bundle();
        params.putString("opponentName", opponent);
        self.setArguments(params);
        return self;
    }

    public GameSetupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGameStartListener) {
            listener = (OnGameStartListener) context;
        } else {
            Log.w(TAG, "No game start listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game_setup, container, false);

        opponentName = (TextView)view.findViewById(R.id.opponentName);
        roundLengthView = (EditText)view.findViewById(R.id.game_length);
        roundLengthView.setText(getString(R.string.rounds_count_fmt, gameLength));

        opponentName.setText(getString(R.string.versus, getArguments().getString("opponentName", "???")));

        betPlus = (Button)view.findViewById(R.id.len_plus);
        betPlus.setOnClickListener(this::onBetPlus);
        betMinus = (Button)view.findViewById(R.id.len_minus);
        betMinus.setOnClickListener(this::onBetMinus);

        Button gameStart = (Button)view.findViewById(R.id.game_start);
        gameStart.setOnClickListener(this::onGameStart);

        return view;
    }

    public void onBetPlus(View view) {
        gameLength += 2;
        roundLengthView.setText(getString(R.string.rounds_count_fmt, gameLength));
        if (gameLength > 3) {
            betMinus.setEnabled(true);
        }
    }

    public void onBetMinus(View view) {
        gameLength -= 2;
        roundLengthView.setText(getString(R.string.rounds_count_fmt, gameLength));
        if (gameLength <= 3) {
            betMinus.setEnabled(false);
        }
    }

    public void onGameStart(View view) {
        Log.d(TAG, "Starting Game");
        if (listener != null) {
            listener.onStartGame(gameLength);
        }
    }

    public interface OnGameStartListener {
        void onStartGame(int gameLength);
    }

}
