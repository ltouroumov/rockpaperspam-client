package com.securingapps.rps.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.gson.JsonObject;
import com.securingapps.rps.R;
import com.securingapps.rps.data.ApiProxy;
import com.securingapps.rps.data.DeviceData;
import com.securingapps.rps.data.FriendService;
import com.securingapps.rps.events.InvalidateGameList;
import com.securingapps.rps.utils.UiRunner;
import com.securingapps.rps.utils.async.AsyncFuture;
import org.greenrobot.eventbus.EventBus;

public class PlayRoundActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = PlayRoundActivity.class.getSimpleName();
    private ImageButton playRock;
    private ImageButton playPaper;
    private ImageButton playScissors;
    private ImageButton playLizard;
    private ImageButton playSpock;
    private int gameId;
    private int roundId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_round);

        gameId = getIntent().getExtras().getInt("gameId");
        String opponentId = getIntent().getExtras().getString("opponentId");
        roundId = getIntent().getExtras().getInt("roundId");

        Log.d(TAG, String.format("Play in game %d", gameId));

        TextView opponent = (TextView) findViewById(R.id.opponentName);
        FriendService.getInstance()
            .getFriend(opponentId)
            .whenCompleteAsync(friend -> opponent.setText(getString(R.string.versus, friend.getDisplayName())), new UiRunner(this));
        TextView round = (TextView) findViewById(R.id.roundNumber);
        round.setText(getString(R.string.round_fmt, roundId));

        playRock = (ImageButton) findViewById(R.id.playRock);
        playRock.setOnClickListener(this);
        playPaper = (ImageButton) findViewById(R.id.playPaper);
        playPaper.setOnClickListener(this);
        playScissors = (ImageButton) findViewById(R.id.playScissors);
        playScissors.setOnClickListener(this);
        playLizard = (ImageButton) findViewById(R.id.playLizard);
        playLizard.setOnClickListener(this);
        playSpock = (ImageButton) findViewById(R.id.playSpock);
        playSpock.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String move = null;
        if (view == playRock) {
            move = "ROC";
        } else if (view == playPaper) {
            move = "PAP";
        } else if (view == playScissors) {
            move = "SIS";
        } else if (view == playLizard) {
            move = "LIZ";
        } else if (view == playSpock) {
            move = "SPO";
        } else {
            Log.w(TAG, "WTF Move!");
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("from", DeviceData.getDeviceId());
        body.addProperty("move", move);
        AsyncFuture
            .runAsync(() -> ApiProxy.getInstance().doPost(body, "games/%d/%d", gameId, roundId))
            .whenComplete(val -> {
                EventBus.getDefault().post(new InvalidateGameList());
                finish();
            });
    }
}
