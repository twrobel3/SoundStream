/*
 * Copyright 2013 The Last Crusade ContactLastCrusade@gmail.com
 * 
 * This file is part of SoundStream.
 * 
 * SoundStream is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SoundStream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SoundStream.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.thelastcrusade.soundstream.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thelastcrusade.soundstream.CoreActivity;
import com.thelastcrusade.soundstream.R;
import com.thelastcrusade.soundstream.model.FoundGuest;
import com.thelastcrusade.soundstream.model.UserList;
import com.thelastcrusade.soundstream.service.ConnectionService;
import com.thelastcrusade.soundstream.service.ConnectionService.ConnectionServiceBinder;
import com.thelastcrusade.soundstream.service.MusicLibraryService;
import com.thelastcrusade.soundstream.service.MusicLibraryService.MusicLibraryServiceBinder;
import com.thelastcrusade.soundstream.service.PlaylistService;
import com.thelastcrusade.soundstream.service.PlaylistService.PlaylistServiceBinder;
import com.thelastcrusade.soundstream.service.ServiceLocator;
import com.thelastcrusade.soundstream.service.ServiceNotBoundException;
import com.thelastcrusade.soundstream.service.UserListService;
import com.thelastcrusade.soundstream.service.UserListService.UserListServiceBinder;
import com.thelastcrusade.soundstream.util.BroadcastRegistrar;
import com.thelastcrusade.soundstream.util.IBroadcastActionHandler;
import com.thelastcrusade.soundstream.util.ITitleable;
import com.thelastcrusade.soundstream.util.Toaster;
import com.thelastcrusade.soundstream.util.TrackerAPI;
import com.thelastcrusade.soundstream.util.Transitions;
import com.thelastcrusade.soundstream.util.UserListAdapter;

/**
 * This fragment handles the ability for members to add new members to 
 * the network and to view the currently connected members
 */
public class NetworkFragment extends Fragment implements ITitleable {
    
    private static String TAG = NetworkFragment.class.getSimpleName();
    private final String SEARCHING_JOIN__DIFFERENT_TAG = "isSearchingJoinDifferent";
    private final String SEARCHING_ADD_TAG = "isSearchingAdd";
    
    private BroadcastRegistrar broadcastRegistrar;
    private UserListAdapter userAdapter;
    private LinearLayout addMembersButton, userView, disconnectDisband, joinDifferentNetworkButton;

    private ServiceLocator<ConnectionService> connectionServiceLocator;
    private ServiceLocator<UserListService>   userListServiceLocator;

    private TrackerAPI tracker;
    
    private boolean isSearchingAdd, isSearchingJoinDifferent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectionServiceLocator = new ServiceLocator<ConnectionService>(
                this.getActivity(), ConnectionService.class, ConnectionServiceBinder.class);

        connectionServiceLocator.setOnBindListener(new ServiceLocator.IOnBindListener() {
            @Override
            public void onServiceBound() {
                setDisconnectDisbandBtn();
            }
        });

        tracker = new TrackerAPI((CoreActivity)getActivity());
        userAdapter = new UserListAdapter(getActivity(), new UserList(), false);
        userView = new LinearLayout(getActivity());
        
        userListServiceLocator = new ServiceLocator<UserListService>(
                this.getActivity(), UserListService.class, UserListServiceBinder.class);

        userListServiceLocator.setOnBindListener(new ServiceLocator.IOnBindListener() {
            @Override
            public void onServiceBound() {
                updateUserView();
            }
        });

        registerReceivers();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_network, container,false);
        
        this.addMembersButton = (LinearLayout)v.findViewById(R.id.add_members);

        this.addMembersButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (getConnectionService().isNetworkSupported()) {
                    new WithBluetoothEnabled(getActivity(), getConnectionService()).run(new Runnable() {

                        @Override
                        public void run() {
                            setButtonToSearchingState(addMembersButton);
                            isSearchingAdd = true;
                            
                            getConnectionService().findNewGuests();
                            tracker.trackAddMembersEvent();
                        }
                    });
                } else {
                    Toaster.iToast(getActivity(), R.string.no_bt_support);
                }
           }
        });

        disconnectDisband = (LinearLayout)v.findViewById(R.id.disconnect_disband_btn);

        userView = (LinearLayout) v.findViewById(R.id.connected_users);
        updateUserView();

        //TODO react to changing state
        setDisconnectDisbandBtn();
        
        this.joinDifferentNetworkButton = (LinearLayout)v.findViewById(R.id.join_different_network_btn);
        joinDifferentNetworkButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new WithBluetoothEnabled(getActivity(), getConnectionService()).run(new Runnable() {

                    @Override
                    public void run() {
                        getConnectionService().broadcastSelfAsGuest(getActivity());
                    }
                });
            }
        });
        
        if(savedInstanceState != null){
            isSearchingAdd = savedInstanceState.getBoolean(SEARCHING_ADD_TAG);
            isSearchingJoinDifferent = savedInstanceState.getBoolean(SEARCHING_JOIN__DIFFERENT_TAG);
            
            if(isSearchingAdd)
                setButtonToSearchingState(addMembersButton);
            
            if(isSearchingJoinDifferent)
                setButtonToSearchingState(joinDifferentNetworkButton);
        }
        return v;
    }

    private void setDisconnectDisbandBtn() {
        // The only time a host should be connected is if we are a guest. In
        // this case, it should be a disband function, otherwise (when we are
        // host) it should be be a disconnect function.
        if (getConnectionService() == null || getConnectionService().isHostConnected()) {
            setDisconnectFunction();
        } else {
            setDisbandFunction();
        }
    }

    private void setDisconnectFunction(){
        ((TextView)disconnectDisband.findViewById(R.id.disconnect_disband_label))
            .setText(R.string.disconnect);
        
        //set the button to disconnect when pressed
        disconnectDisband.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.dialog_disconnect)
                        .setPositiveButton(R.string.disconnect, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "Disconnecting...");
                                tracker.trackDisconnectEvent(true);
                                disconnect();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                tracker.trackDisconnectEvent(false);
                            }
                        })
                        .show();
            }
        });
    }
    
    private void setDisbandFunction(){
        ((TextView)disconnectDisband.findViewById(R.id.disconnect_disband_label))
            .setText(R.string.disband);
        
        disconnectDisband.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.dialog_disband)
                        .setPositiveButton(R.string.disband, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                disband();
                                tracker.trackDisbandEvent(true);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                tracker.trackDisbandEvent(false);
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ((CoreActivity)getActivity()).getTracker().sendView(TAG);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(this.userAdapter != null){
            this.userAdapter.notifyDataSetChanged();
            updateUserView();
        }
        getActivity().setTitle(getTitle());
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SEARCHING_ADD_TAG, isSearchingAdd);
        outState.putBoolean(SEARCHING_JOIN__DIFFERENT_TAG, isSearchingJoinDifferent);
    }
    
    @Override
    public void onDestroy() {
        unregisterReceivers();
        this.connectionServiceLocator.unbind();
        this.userListServiceLocator.unbind();
        super.onDestroy();
    }

    private ConnectionService getConnectionService() {
        ConnectionService connectionService = null;
        try {
            connectionService = this.connectionServiceLocator.getService();
        } catch (ServiceNotBoundException e) {
            //TODO Make this a real error
            Log.v(TAG, e.toString());
        }
        return connectionService;
    }

    private void registerReceivers() {
        this.broadcastRegistrar = new BroadcastRegistrar();
        this.broadcastRegistrar
            .addLocalAction(ConnectionService.ACTION_FIND_FINISHED, new IBroadcastActionHandler() {

                @Override
                public void onReceiveAction(Context context, Intent intent) {
                    onFindFinished(intent);
                }
            })
            .addLocalAction(UserList.ACTION_USER_LIST_UPDATE, new IBroadcastActionHandler() {
                
                @Override
                public void onReceiveAction(Context context, Intent intent) {
                    updateUserView();
                }
            })
            .addLocalAction(ConnectionService.ACTION_GUEST_CONNECTED, new IBroadcastActionHandler() {
                @Override
                public void onReceiveAction(Context context, Intent intent) {
                    setDisconnectDisbandBtn();
                }
            })
            .addLocalAction(ConnectionService.ACTION_HOST_CONNECTED, new IBroadcastActionHandler() {
                @Override
                public void onReceiveAction(Context context, Intent intent) {
                    setDisconnectDisbandBtn();
                }
            })
            .addGlobalAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED, new IBroadcastActionHandler() {
                
                @Override
                public void onReceiveAction(Context context, Intent intent) {
                    int mode = intent.getIntExtra(
                            BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.SCAN_MODE_NONE);
                    
                    if(joinDifferentNetworkButton != null){
                        switch(mode){
                        case BluetoothAdapter.SCAN_MODE_NONE:
                        case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                            setButtonToDefaultState(joinDifferentNetworkButton);
                            isSearchingJoinDifferent = false;
                            break;
                        case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                            setButtonToSearchingState(joinDifferentNetworkButton);
                            isSearchingJoinDifferent = true;
                            break;
                        default:
                            Log.wtf(TAG, "Recieved scan mode changed with unknown mode");
                            break;
                        }
                    }
                    
                }
            })
            .register(this.getActivity());
    }

    private void unregisterReceivers() {
        this.broadcastRegistrar.unregister();
    }

    private void disconnect() {
        //Disconnect from the host
        getConnectionService().disconnectHost();
    }

    private void disband() {
        getConnectionService().disbandHost();
    }
    
    private void setButtonToDefaultState(LinearLayout button){
        button.setEnabled(true);
        button.findViewById(R.id.searching).setVisibility(View.INVISIBLE);
        button.findViewById(R.id.image_background)
            .setBackgroundColor(getActivity().getResources().getColor(R.color.white));
        
    }
    
    private void setButtonToSearchingState(LinearLayout button){
        button.setEnabled(false);

        //TODO: add a better indicator while discovering
        //...seconds until discovery is finished, number of clients found, etc
        button.findViewById(R.id.searching).setVisibility(View.VISIBLE);
        button.findViewById(R.id.image_background)
            .setBackgroundColor(getActivity().getResources().getColor(R.color.gray));
    }

    /**
     * Called to handle a find finished method.  This may be to pop up a dialog
     * or notify the user that no guests were found
     * 
     * @param intent
     */
    private void onFindFinished(Intent intent) {
        //first thing...reenable the add members button
        setButtonToDefaultState(addMembersButton);
        isSearchingAdd = false;
        
        //locally initiated device discovery...pop up a dialog for the user
        List<FoundGuest> guests = intent.getParcelableArrayListExtra(ConnectionService.EXTRA_GUESTS);
        
        //deduplicate and pull out the known items, so we can preselect them
        //NOTE: retained is the list of deduplicated found guests, known
        // is the list of guests that were previously paired.
        Set<FoundGuest>  unique = new HashSet<FoundGuest>(guests);
        List<FoundGuest> retained = new ArrayList<FoundGuest>();
        List<FoundGuest> known = new ArrayList<FoundGuest>();
        for (FoundGuest guest : guests) {
            if (unique.contains(guest)) {
                retained.add(guest);
                unique.remove(guest);
            }
            if (guest.isKnown()) {
                known.add(guest);
            }
        }
        
        if (guests.isEmpty()) {
            Toaster.iToast(this.getActivity(), R.string.no_guests_found);
        } else {
            new MultiSelectListDialog<FoundGuest>(this.getActivity(),
                    R.string.select_guests, R.string.connect)
                    .setItems(retained)
                    .setSelected(known)
                    .setOnClickListener(
                            new IOnDialogMultiItemClickListener<FoundGuest>() {
    
                                @Override
                                public void onItemsClick(
                                        List<FoundGuest> foundGuests) {
                                    tracker.trackFoundGuestsEvent(foundGuests.size());
                                    getConnectionService().connectToGuests(foundGuests);
                                }
                            })
                    .show();
        }
    }

    @Override
    public int getTitle() {
        return R.string.network;
    }

    private UserList getUserListFromService(){
        UserList activeUsers;
        UserListService userService = getUserListService();
        if(userService != null){
            activeUsers = userService.getUserList();
        } else {
            activeUsers = new UserList();
            Log.i(TAG, "UserListService null, returning empty userlist");
        }
        return activeUsers;
    }

    private UserListService getUserListService(){
        UserListService userService = null;
        try{
            userService = userListServiceLocator.getService();
        } catch (ServiceNotBoundException e) {
            Log.w(TAG, "UserListService not bound");
        }
        return userService;
    }
    
    private void updateUserView(){
        userView.removeAllViews();
        userAdapter.updateUsers(getUserListFromService());
        for(int i=0; i<userAdapter.getCount(); i++){
            View user = userAdapter.getView(i, null, userView);
            userView.addView(user);
        }
        
    }
}
