package play.modules.plovr;

import play.Logger;
import play.PlayPlugin;

public class PlovrPlugin extends PlayPlugin {
    @Override
    public void onLoad() {
        Plovr.load();
    }
}
