package tk.lefourretoutsonore.lefourre_toutsonore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tk.lefourretoutsonore.lefourre_toutsonore.service.PollService;

/**
 * Created by M4gicT0 on 11/01/2016.
 */
public class BootCompletedIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, PollService.class);
            context.startService(pushIntent);
        }
    }
}
