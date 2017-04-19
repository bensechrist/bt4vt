package com.bt4vt.external.bt4u;

import com.bt4vt.R;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import roboguice.inject.InjectResource;

/**
 * Service to get current bus information
 *
 * @author Ben Sechrist
 */
@Singleton
public class BusService {

  @InjectResource(R.string.bt4u_base_url)
  private String BT4U_BASE_URL;

  String BT4U_BUS_URL = BT4U_BASE_URL + "livemap";

  @Inject
  private HttpClient client;

  @Inject
  private Request.Builder requestBuilder;

  /**
   * Queries BT4U for information on all current buses.
   */
  public void getAll(final Callback<List<Bus>> callback) {
    Request request = requestBuilder
        .url(BT4U_BUS_URL)
        .build();

    client.newCall(request).enqueue(new okhttp3.Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        callback.onException(e);
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        try {
          int statusCode = response.code();
          if (statusCode == 200) {
            String stringBody = response.body().string();
            JSONArray jsonBuses = new JSONArray(stringBody).getJSONObject(0).getJSONArray("buses");
            List<Bus> buses = new ArrayList<Bus>();
            for (int i=0; i<jsonBuses.length()-1; i++) {
              buses.add(Bus.valueOf(jsonBuses.getJSONObject(i)));
            }
            callback.onResult(buses);
          } else {
            callback.onFail(statusCode, response.body().string());
          }
        } catch (JSONException e) {
          callback.onException(e);
        }
      }
    });
  }
}
