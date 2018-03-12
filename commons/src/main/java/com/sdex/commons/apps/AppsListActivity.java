package com.sdex.commons.apps;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import com.sdex.commons.BaseActivity;
import com.sdex.commons.R;
import com.sdex.commons.util.IOUtils;
import com.sdex.commons.util.SpaceItemDecoration;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class AppsListActivity extends BaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_apps_list);
    setTitle(R.string.sdex_commons_more_apps);
    enableBackButton();

    LoadTask loadTask = new LoadTask(this);
    loadTask.execute();
  }

  private void showList(List<AppItem> items) {
    RecyclerView recyclerView = findViewById(R.id.apps_list);
    recyclerView.addItemDecoration(new SpaceItemDecoration(this, R.dimen.item_app_spacing));
    recyclerView.setAdapter(new AppsListAdapter(items));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    return true;
  }

  private static class LoadTask extends AsyncTask<Void, Void, List<AppItem>> {

    private final WeakReference<AppsListActivity> activityRef;
    private final Context context;

    public LoadTask(AppsListActivity activity) {
      activityRef = new WeakReference<>(activity);
      context = activity.getApplicationContext();
    }

    @Override
    protected List<AppItem> doInBackground(Void... voids) {
      String appList = IOUtils.loadAssetTextAsString(context, "apps.json");
      return parseList(appList);
    }

    private List<AppItem> parseList(String stringBody) {
      JSONTokener jsonTokener = new JSONTokener(stringBody);
      try {
        JSONArray array = (JSONArray) jsonTokener.nextValue();
        List<AppItem> items = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++) {
          JSONObject object = (JSONObject) array.get(i);
          String name = (String) object.get("name");
          String description = (String) object.get("description");
          String packageName = (String) object.get("package_name");
          String icon = (String) object.get("icon");
          items.add(new AppItem(name, description, packageName, icon));
        }
        return items;
      } catch (JSONException e) {
        e.printStackTrace();
      }
      return Collections.emptyList();
    }

    @Override
    protected void onPostExecute(List<AppItem> items) {
      super.onPostExecute(items);
      AppsListActivity activity = activityRef.get();
      if (activity != null && !activity.isFinishing()) {
        activity.showList(items);
      }
    }
  }
}