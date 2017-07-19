package com.tricktrap.rps.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tricktrap.rps.R;
import com.tricktrap.rps.data.ApiProxy;
import com.tricktrap.rps.data.DeviceData;
import com.tricktrap.rps.data.FriendService;
import com.tricktrap.rps.events.InvalidateGameList;
import com.tricktrap.rps.utils.UiRunner;
import com.tricktrap.rps.utils.async.AsyncFuture;
import okhttp3.Response;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

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
            .supplyAsync(() -> ApiProxy.getInstance().doPost(body, "games/%d/%d", gameId, roundId))
            .whenCompleteAsync(resp -> {
                if (resp.isSuccessful()) {
                    EventBus.getDefault().post(new InvalidateGameList());
                } else if (isOutOfEnergy(resp)) {
                    Toast.makeText(this, getString(R.string.oom_error_body), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.srv_error_body), Toast.LENGTH_SHORT).show();
                    try {
                        Log.e(TAG, String.format("Play error (%d): %s", resp.code(), resp.body().string()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                finish();
            }, new UiRunner(this));
    }

    private boolean isOutOfEnergy(Response resp) {
        if (resp.code() != 400)
            return false;

        try {
            String body = resp.body().string();
            JsonElement elem = ApiProxy.getInstance().readJson(body);
            if (elem.isJsonObject() && elem.getAsJsonObject().has("detail")) {
                String detail = elem.getAsJsonObject().get("detail").getAsString();
                return detail.contains("No energy");
            } else {
                return false;
            }
        } catch (IOException ex) {
            Log.e(TAG, "Error while getting response content", ex);
        }
        return false;
    }
}
