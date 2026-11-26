package ua.com.merchik.merchik.Options.Controls;

import com.google.gson.Gson;
import org.junit.Test;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.CoordOnMapSDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.OptionsServer;
import ua.com.merchik.merchik.database.room.DaoInterfaces.CoordOnMapDao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Option1474AddressOperatorTest {
    private final CoordOnMapSDB address = new CoordOnMapSDB();
    private final CoordOnMapDao addresses = new CoordOnMapDao() {
        @Override public CoordOnMapSDB getById(long id) { return id == 109 ? address : null; }
        @Override public void saveAll(List<CoordOnMapSDB> rows) { throw new AssertionError(); }
    };

    @Test public void matchesOnlyTheOperatorOfTheVisitAddress() {
        address.operatorId = 72;
        address.supervisorId = 73;
        assertTrue(OptionControlOverTimelinessOfReporting.isAddressOperator(addresses, 109, 72));
        assertFalse(OptionControlOverTimelinessOfReporting.isAddressOperator(addresses, 109, 73));
        assertFalse(OptionControlOverTimelinessOfReporting.isAddressOperator(addresses, 110, 72));
    }

    @Test public void unassignedOperatorNeverGrantsException() {
        assertFalse(OptionControlOverTimelinessOfReporting.isAddressOperator(addresses, 109, 0));
        assertFalse(OptionControlOverTimelinessOfReporting.isAddressOperator(addresses, 109, 72));
    }

    @Test public void serverIndulgenceGrantsExceptionWithoutAddressData() {
        OptionsDB option = downloaded("\"1\"");
        assertEquals(1, option.getIndulgence());
        assertTrue(OptionControlOverTimelinessOfReporting.hasReplacementException(option, null, 109, 72));
    }

    @Test public void explicitServerZeroNoLongerGrantsIndulgenceButOperatorCanStillQualify() {
        OptionsDB option = downloaded("\"0\"");
        assertEquals(0, option.getIndulgence());
        assertFalse(OptionControlOverTimelinessOfReporting.hasReplacementException(option, addresses, 109, 72));
        address.operatorId = 72;
        assertTrue(OptionControlOverTimelinessOfReporting.hasReplacementException(option, addresses, 109, 72));
    }

    @Test public void onlyOneIsAnIndulgence() {
        assertFalse(OptionControlOverTimelinessOfReporting.hasReplacementException(new OptionsDB(), null, 109, 72));
        assertFalse(OptionControlOverTimelinessOfReporting.hasReplacementException(downloaded("2"), null, 109, 72));
    }

    private static OptionsDB downloaded(String flag) {
        String body = "{\"state\":true,\"list\":[{\"ID\":\"5\",\"option_id\":\"1474\",\"indulgence\":" + flag + "}]}";
        return new Gson().fromJson(body, OptionsServer.class).getList().get(0);
    }
}
