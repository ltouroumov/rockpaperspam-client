package com.securingapps.rps.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.securingapps.rps.R;
import com.securingapps.rps.data.Challenge;
import com.securingapps.rps.data.ConfigManager;
import com.securingapps.rps.data.ContactService;
import com.securingapps.rps.data.GameService;
import com.securingapps.rps.utils.async.AsyncFuture;
import com.securingapps.rps.utils.fn.Supplier;

public class LobbyActivity extends AppCompatActivity implements ChallengesFragment.OnChallengeSelected {


    public static final int REQUEST_OPPONENT = 1;
    private static final String TAG = LobbyActivity.class.getSimpleName();
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    // private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    // private ViewPager mViewPager;
    public LobbyActivity() {
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "Received New Intent");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        // mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        // mViewPager = (ViewPager) findViewById(R.id.container);
        // mViewPager.setAdapter(mSectionsPagerAdapter);

        // TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        // tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(LobbyActivity.this, GameStartActivity.class);
            startActivityForResult(intent, REQUEST_OPPONENT);
        });

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.container, ChallengesFragment.newInstance("O"))
            .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPPONENT) {
            if (resultCode != Activity.RESULT_OK)
                return;

            Log.d(TAG, "Starting Game ... " + TextUtils.join(", ", data.getExtras().keySet()));
            GameService.getInstance()
                .startGame(
                    data.getExtras().getString("opponent", "0"),
                    data.getExtras().getInt("gameLength", 0)
                );
        }
    }

    @Override
    public void onChallengeSelected(ChallengesFragment sender, Challenge challenge) {
        if (!challenge.isComplete()) {
            if (challenge.canPlay()) {
                // Open play activity
                Intent intent = new Intent(this, PlayRoundActivity.class);
                intent.putExtra("gameId", challenge.getGameId());
                intent.putExtra("opponentId", challenge.getOpponentId());
                intent.putExtra("roundId", challenge.getCurrentRound());
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.already_played, Toast.LENGTH_SHORT).show();
            }
        } else {
            // Open history activity
            Intent intent = new Intent(this, GameDisplayActivity.class);
            intent.putExtra("gameId", challenge.getGameId());
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh config when we get back to the home screen
        AsyncFuture.runAsync(() -> {
            ConfigManager.getInstance().refresh();
        });
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
//    public class SectionsPagerAdapter extends FragmentPagerAdapter {
//
//        private Fragment[] fragments;
//
//        public SectionsPagerAdapter(FragmentManager fm) {
//            super(fm);
//            fragments = new Fragment[2];
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//            // getItem is called to instantiate the fragment for the given page.
//            // Return a PlaceholderFragment (defined as a static inner class below).
//            switch (position) {
//                case 0:
//                    return getOrCreate(0, () -> ChallengesFragment.newInstance("O"));
//                case 1:
//                    return getOrCreate(1, () -> ChallengesFragment.newInstance("C"));
//            }
//            return null;
//        }
//
//        @Override
//        public int getCount() {
//            // Show 3 total pages.
//            return 2;
//        }
//
//        @Override
//        public CharSequence getPageTitle(int position) {
//            switch (position) {
//                case 0:
//                    return "Matches";
//                case 1:
//                    return "History";
//            }
//            return null;
//        }
//
//        private Fragment getOrCreate(int idx, Supplier<Fragment> creator) {
//            if (fragments[idx] == null) {
//                fragments[idx] = creator.get();
//            }
//
//            return fragments[idx];
//        }
//    }
}
