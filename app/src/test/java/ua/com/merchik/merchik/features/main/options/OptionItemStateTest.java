package ua.com.merchik.merchik.features.main.options;

import android.view.View;
import org.junit.Test;

import static org.junit.Assert.*;

public class OptionItemStateTest {
    @Test
    public void hidesMoneyOnlyWhenSettingIsDisabled() {
        OptionItemState.TextPart part = new OptionItemState.TextPart();
        part.monetary = true;
        assertTrue(part.shouldDisplay(true));
        assertFalse(part.shouldDisplay(false));
    }

    @Test
    public void keepsCountersAndTimeWhenMoneyIsHidden() {
        for (String text : new String[]{"3/10", "14:59", "2"}) {
            OptionItemState.TextPart part = new OptionItemState.TextPart();
            part.text = text;
            assertTrue(part.shouldDisplay(false));
        }
    }

    @Test
    public void settingDoesNotMakeGonePartsVisible() {
        OptionItemState.TextPart part = new OptionItemState.TextPart();
        part.visibility = View.GONE;
        assertFalse(part.shouldDisplay(true));
        assertFalse(part.shouldDisplay(false));
        part.monetary = true;
        assertFalse(part.shouldDisplay(true));
    }

    @Test
    public void preservesInvisibleSpaceUnlessMoneyIsHidden() {
        OptionItemState.TextPart part = new OptionItemState.TextPart();
        part.visibility = View.INVISIBLE;
        assertTrue(part.shouldDisplay(false));
        part.monetary = true;
        assertTrue(part.shouldDisplay(true));
        assertFalse(part.shouldDisplay(false));
    }

    @Test
    public void filteringDoesNotMutateContentOrOriginalVisibility() {
        OptionItemState.TextPart part = new OptionItemState.TextPart();
        part.monetary = true;
        part.text = "100";
        assertFalse(part.shouldDisplay(false));
        assertEquals(View.VISIBLE, part.visibility);
        assertEquals("100", part.text);
        assertTrue(part.shouldDisplay(true));
    }
}
