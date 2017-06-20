package com.securingapps.rps.activity;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.securingapps.rps.R;
import com.securingapps.rps.data.Friend;

public class GameStartActivity
        extends AppCompatActivity
        implements FriendListFragment.OnFriendSelectedListener, GameSetupFragment.OnGameStartListener {

    private static final String TAG = GameStartActivity.class.getSimpleName();

    private Friend opponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_picker);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new FriendListFragment())
                .commit();
    }

    @Override
    public void onFriendSelected(Friend friend) {
        Log.d(TAG, "Selected Friend " + friend.getId());
        if (friend.isClient()) {
            opponent = friend;
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, GameSetupFragment.newInstance(opponent.getDisplayName()))
                .addToBackStack("gameSetup")
                .commit();
        } else {
            new AlertDialog.Builder(this)
                .setTitle(R.string.invite_your_friend_title)
                .setMessage(R.string.invite_your_friend_body)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    sendInvite(friend);
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    Log.d(TAG, "Do Nothing");
                })
                .show();
        }
    }

    private void sendInvite(Friend friend) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    @Override
    public void onStartGame(int gameLength) {
        Log.d(TAG, "Starting game");
        Intent intent = new Intent();
        intent.putExtra("opponent", opponent.getId());
        intent.putExtra("gameLength", gameLength);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (opponent != null) {
            getSupportFragmentManager()
                    .popBackStack();
            opponent = null;
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}
