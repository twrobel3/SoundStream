package com.lastcrusade.soundstream;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lastcrusade.soundstream.model.UserList;
import com.lastcrusade.soundstream.service.ConnectionService;
import com.lastcrusade.soundstream.service.ConnectionService.ConnectionServiceBinder;
import com.lastcrusade.soundstream.service.IMessagingService;
import com.lastcrusade.soundstream.service.MessagingService;
import com.lastcrusade.soundstream.service.MessagingService.MessagingServiceBinder;
import com.lastcrusade.soundstream.service.ServiceLocator;
import com.lastcrusade.soundstream.service.ServiceNotBoundException;
import com.lastcrusade.soundstream.util.BroadcastIntent;
import com.lastcrusade.soundstream.util.BroadcastRegistrar;
import com.lastcrusade.soundstream.util.IBroadcastActionHandler;

public class CustomApp extends Application {
    private final String TAG = CustomApp.class.getName();
    
    private UserList userList;
    
    private ServiceLocator<ConnectionService> connectionServiceLocator;
    private ServiceLocator<MessagingService>  messagingServiceLocator;

    private BroadcastRegistrar registrar;

    public CustomApp() {
        super();
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        userList = new UserList();
        
        connectionServiceLocator = new ServiceLocator<ConnectionService>(
                this, ConnectionService.class, ConnectionServiceBinder.class);
        
        messagingServiceLocator = new ServiceLocator<MessagingService>(
                this, MessagingService.class, MessagingServiceBinder.class);
        
        registerReceivers();
    }
    
    @Override
    public void onTerminate() {
        unregisterReceivers();
        super.onTerminate();
    }

    private void registerReceivers() {
        this.registrar = new BroadcastRegistrar();
        this.registrar
            .addAction(ConnectionService.ACTION_FAN_CONNECTED, new IBroadcastActionHandler() {
                @Override
                public void onReceiveAction(Context context, Intent intent) {
                    String bluetoothID = intent.getStringExtra(ConnectionService.EXTRA_FAN_NAME);
                    String macAddress  = intent.getStringExtra(ConnectionService.EXTRA_FAN_ADDRESS);
                    userList.addUser(bluetoothID, macAddress);
                    Log.i(TAG, "User List: " + userList.toString());
                    notifyUserListUpdate();
                }
            })
            .addAction(ConnectionService.ACTION_FAN_DISCONNECTED, new IBroadcastActionHandler() {
                
                @Override
                public void onReceiveAction(Context context, Intent intent) {
                    String macAddress  = intent.getStringExtra(ConnectionService.EXTRA_FAN_ADDRESS);
                    userList.removeUser(macAddress);
                    notifyUserListUpdate();
                }
            })
            .addAction(MessagingService.ACTION_NEW_CONNECTED_USERS, new IBroadcastActionHandler() {
                
                @Override
                public void onReceiveAction(Context context, Intent intent) {
                    Log.i(TAG, "New Connected Users Message Recieved");
                    userList = intent.getParcelableExtra(MessagingService.EXTRA_USER_LIST);
                    Log.i(TAG,"New User List:" + userList.toString());
                    new BroadcastIntent(UserList.ACTION_USER_LIST_UPDATE).send(CustomApp.this);
                }
            })
            .register(this);
        
    }

    private void unregisterReceivers() {
        this.registrar.unregister();
    }

    public UserList getUserList(){
        return userList;
    }
    
    public ConnectionService getConnectionService() {
        ConnectionService connectionService = null;
        try {
            connectionService = this.connectionServiceLocator.getService();
        } catch (ServiceNotBoundException e) {
            Log.wtf(TAG, e);
        }
        return connectionService;
    }

    public IMessagingService getMessagingService() {
        MessagingService messagingService = null;
        try {
            messagingService = this.messagingServiceLocator.getService();
        } catch (ServiceNotBoundException e) {
            Log.wtf(TAG, e);
        }
        return messagingService;
    }
    
    public void notifyUserListUpdate() {
        new BroadcastIntent(UserList.ACTION_USER_LIST_UPDATE).send(this);
        getMessagingService().sendUserListMessage(userList);
    }
}
