package com.example.aperture.core.launcher;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;
import java.util.ArrayList;

import com.example.aperture.core.IModule;
import com.example.aperture.core.Module;


public class LauncherModule extends Module {


    @Override
    protected IModule createBinder() {
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
                        launcherIntent);

                for(ResolveInfo info: activities) {
                    String label = info.activityInfo.name;
                    try {
                        label = pm.getText(info.activityInfo.packageName,
                                info.activityInfo.labelRes,
                                info.activityInfo.applicationInfo);
                    }
                    catch(Exception e) {}
                    if(label.contains(query)) {
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
