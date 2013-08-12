package com.example.aperture.core.launcher;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;

import java.util.List;
import java.util.ArrayList;

import com.example.aperture.core.IModule;
import com.example.aperture.core.Module;


public class LauncherModule extends Module {


    @Override
    protected IBinder createBinder() {
        return new IModule.Stub() {
            public List<Intent> process(Intent data) {
                List<Intent> response = new ArrayList<Intent>();

                String query = data.getStringExtra(Module.QUERY_TEXT);
                if(query.length() == 0)
                    return response;

                PackageManager pm = LauncherModule.this.getPackageManager();
                Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
                launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> activities = pm.queryIntentActivities(
                        launcherIntent, 0);

                for(ResolveInfo info: activities) {
                    String label = info.loadLabel(pm).toString();
/*
                    try {
                        if(info.activityInfo.labelRes != 0)
                        label = pm.getText(info.activityInfo.packageName,
                                info.activityInfo.labelRes,
                                info.activityInfo.applicationInfo).toString();
                    }
                    catch(Exception e) {}
//*/
                    if(label.toLowerCase().contains(query.toLowerCase())) {
                        Intent launchIntent = new Intent(Intent.ACTION_MAIN);
                        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                        launchIntent.setComponent(
                                new ComponentName(info.activityInfo.packageName,
                                        info.activityInfo.name));
                        launchIntent.putExtra(Module.RESPONSE_TEXT, label);
                        response.add(launchIntent);
                    }
                }

                return response;
            }
        };
    }

}
