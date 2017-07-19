package com.tricktrap.rps.activity;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.tricktrap.rps.R;
import com.tricktrap.rps.data.Friend;
import com.tricktrap.rps.data.FriendService;
import com.tricktrap.rps.utils.UiRunner;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String TAG = FriendListFragment.class.getSimpleName();
    private OnFriendSelectedListener listener;
    private FriendsAdapter friendsAdapter;

    public FriendListFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFriendSelectedListener) {
            listener = (OnFriendSelectedListener)context;
        } else {
            Log.w(TAG, "Parent does not implement OnFriendSelectedListener");
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
        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);

        friendsAdapter = new FriendsAdapter(getContext());
        ListView friendsList = (ListView)view.findViewById(R.id.friend_list);
        friendsList.setAdapter(friendsAdapter);
        friendsList.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        FriendService.getInstance()
                .getFriends()
                .whenCompleteAsync(friends -> friendsAdapter.updateList(friends), new UiRunner(getActivity()));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Friend friend = friendsAdapter.getItem(position);
        if (listener != null) {
            listener.onFriendSelected(friend);
        }
    }

    public interface OnFriendSelectedListener {
        void onFriendSelected(Friend friend);
    }

    private static class FriendsAdapter extends BaseAdapter {

        private Context context;
        private Friend[] friends = new Friend[0];

        public FriendsAdapter(Context context) {
            this.context = context;
        }

        public void updateList(Friend[] friends) {
            this.friends = friends;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return friends.length;
        }

        @Override
        public Friend getItem(int position) {
            return friends[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.fragment_friend_list_item, parent, false);
            }

            Friend friend = getItem(position);

            TextView name = (TextView)view.findViewById(R.id.name);
            name.setText(friend.getDisplayName());
            ImageView clientIndicator = (ImageView) view.findViewById(R.id.is_not_client);
            clientIndicator.setVisibility(friend.isClient() ? View.INVISIBLE : View.VISIBLE);

            return view;
        }
    }

}
