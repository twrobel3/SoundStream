package com.lastcrusade.soundstream.service;

import java.util.List;

import com.lastcrusade.soundstream.model.SongMetadata;
import com.lastcrusade.soundstream.model.UserList;

public interface IMessagingService {

    public void sendLibraryMessage(List<SongMetadata> library);
    public void sendFindNewFansMessage();
    public void sendPauseMessage();
    public void sendPlayMessage();
    public void sendSkipMessage();
    public void sendStringMessage(String message);
    public void sendUserListMessage(UserList userlist);
}