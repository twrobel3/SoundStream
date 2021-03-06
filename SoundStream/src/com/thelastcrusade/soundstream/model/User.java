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

package com.thelastcrusade.soundstream.model;

import android.util.Log;

public class User {

    private final static String TAG = User.class.toString();
    //keep track of bluetoothID for display name
    private String bluetoothID;
    //keep track of macAddress for associating data 
    //with the correct user
    private String macAddress;
    private int color;
    
    public User(String bluetoothID, String macAddress, int color){
        this.bluetoothID = bluetoothID;
        this.macAddress = macAddress;
        this.color = color;
    }
    
    public String getBluetoothID(){
        return bluetoothID;
    }
    
    public int getColor(){
        return color;
    } 
    
    public void setColor(int color){
        this.color = color;
    }
    
    public String getMacAddress(){
        if(macAddress == null){
            Log.wtf(TAG, "Mac address null");
        }
        return macAddress;

    }

    @Override
    public boolean equals(Object obj){
        boolean result;
        if ((obj == null) || !(obj instanceof User)) {
            result = false;
        } else {
            User other = (User) obj;
            result = macAddress.equals(other.getMacAddress()) && bluetoothID.equals(other.getBluetoothID());
        }
        return result;
    }
}
