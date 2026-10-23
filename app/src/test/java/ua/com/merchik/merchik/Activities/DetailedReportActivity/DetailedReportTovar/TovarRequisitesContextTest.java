package ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.ContextThemeWrapper;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class TovarRequisitesContextTest {
    @Test
    public void acceptsActivityDirectly() throws Exception {
        Activity activity = new Activity();
        assertSame(activity, findActivity(activity));
    }

    @Test
    public void unwrapsContextWrapper() throws Exception {
        Activity activity = new Activity();
        assertSame(activity, findActivity(wrap(activity)));
    }

    @Test
    public void unwrapsNestedThemedDialogContext() throws Exception {
        Activity activity = new Activity();
        Context wrapped = wrap(activity);
        Context themed = new ContextThemeWrapper() {
            @Override
            public Context getBaseContext() {
                return wrapped;
            }
        };
        assertSame(activity, findActivity(wrap(themed)));
    }

    @Test
    public void returnsNullWhenWrapperHasNoActivity() throws Exception {
        assertNull(findActivity(wrap(wrap(null))));
    }

    @Test
    public void returnsNullForMissingContext() throws Exception {
        assertNull(findActivity(null));
    }

    private static Activity findActivity(Context context) throws Exception {
        Method method = TovarRequisites.class.getDeclaredMethod("findActivity", Context.class);
        method.setAccessible(true);
        return (Activity) method.invoke(new TovarRequisites(), context);
    }

    private static Context wrap(Context base) {
        return new ContextWrapper(null) {
            @Override
            public Context getBaseContext() {
                return base;
            }
        };
    }
}
