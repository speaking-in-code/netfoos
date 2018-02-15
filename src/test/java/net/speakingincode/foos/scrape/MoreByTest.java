package net.speakingincode.foos.scrape;

import org.junit.Test;

import static org.junit.Assert.*;

public class MoreByTest {
    @Test
    public void filtersNoNickname() {
        assertEquals("Eaton, Brian", MoreBy.removeNickName("Eaton, Brian"));
    }

    @Test
    public void filtersNickname() {
        assertEquals("Eaton, Brian", MoreBy.removeNickName("Eaton, Brian \"Wise Guy\""));
        assertEquals("Eaton, Brian", MoreBy.removeNickName("Eaton, Brian \"Wise Guy\""));
    }
}