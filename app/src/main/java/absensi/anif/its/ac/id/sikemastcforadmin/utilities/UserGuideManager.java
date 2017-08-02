package absensi.anif.its.ac.id.sikemastcforadmin.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class UserGuideManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;

    public UserGuideManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences("user_guide_manager", 0);
        editor = pref.edit();
    }

    public void setFirstPage(boolean isFirst) {
        editor.putBoolean("check", isFirst);
        editor.commit();
    }

    public boolean Check() {
        return pref.getBoolean("check", true);
    }
}
