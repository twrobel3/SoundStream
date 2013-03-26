package com.lastcrusade.soundstream.net.message;

import static com.lastcrusade.soundstream.util.CustomAssert.assertSongMetaEquals;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.lastcrusade.soundstream.model.Playlist;
import com.lastcrusade.soundstream.model.SongMetadata;

public class PlaylistMessageTest extends SerializationTest<PlaylistMessage> {

    @Test
    public void testSerializePlaylistMessage() throws IOException {
        List<SongMetadata> songsToPlay = populateTestPlaylist();

        PlaylistMessage preSerializationPlaylistMessage = new PlaylistMessage(
                songsToPlay);
        PlaylistMessage postSerializationPlaylistMessage = super
                .testSerializeMessage(preSerializationPlaylistMessage);

        assertEquals(songsToPlay.size(), postSerializationPlaylistMessage.getSongsToPlay().size());
        for (int i = 0; i < songsToPlay.size(); i++) {
            assertSongMetaEquals(songsToPlay.get(i),
                    postSerializationPlaylistMessage.getSongsToPlay().get(i));
        }
    }

    public List<SongMetadata> populateTestPlaylist() {
        List<SongMetadata> library = new ArrayList<SongMetadata>(Arrays.asList(
                new SongMetadata(69, "Driver that Had a Dick on His Shoulder",
                        "Aziz Ansari", "Dangerously Delicious", 2345, "David"),
                new SongMetadata(1, "Lady with the Puppies", null,
                        "Dangerously Delicious", 23462346, "David"),
                new SongMetadata(23, "Toronto Customs Lady", "Aziz Ansari",
                        null, 3423462, "David"), 
                new SongMetadata(42, "Motley Crue Tour vs. Aziz Tour", "Aziz Ansari",
                        "Dangerously Delicious", 2346236, null)));
        return library;
    }
}