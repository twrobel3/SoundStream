package com.lastcrusade.soundstream.util;

import com.lastcrusade.soundstream.model.PlaylistEntry;
import com.lastcrusade.soundstream.model.SongMetadata;

public class SongMetadataUtils {

    /**
     * Create a unique key for this song.  This unique key consists of:
     *  Mac address (uniquely identifies a device)
     *  Song id (uniquely identifies a song on the device)
     * 
     * @param song
     * @return
     */
    public static String getUniqueKey(SongMetadata song) {
        return getUniqueKey(song.getMacAddress(), song.getId());
    }
    
    public static String getUniqueKey(String songSourceAddress, long songId) {
        return songSourceAddress.replace(":", "") + "_" + songId;
    }

    public static boolean isTheSameSong(SongMetadata lhs, SongMetadata rhs) {
        return lhs.getMacAddress().equals(rhs.getMacAddress()) &&
               lhs.getId() == rhs.getId();
    }
    
    /**
     * Checks to see if the two playlist entries are identical - same mac address,
     * id, and entry id
     * 
     * @param lhs
     * @param rhs
     * @return
     */
    public static boolean isTheSameEntry(PlaylistEntry lhs, PlaylistEntry rhs) {
        return lhs.getMacAddress().equals(rhs.getMacAddress()) &&
               lhs.getId() == rhs.getId() && lhs.getEntryId() == rhs.getEntryId();
    }
    
    public static String getUniqueKey(PlaylistEntry song) {
        return getUniqueKey(song.getMacAddress(), song.getId(), song.getEntryId());
    }
    
    public static String getUniqueKey(String songSourceAddress, long songId, int entryId) {
        return songSourceAddress.replace(":", "") + "_" + songId + "_" + entryId;
    }

}
