/*
 * Copyright 2015 Ben Sechrist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bt4vt.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bt4vt.R;
import com.bt4vt.repository.domain.Departure;
import com.bt4vt.repository.domain.NextDeparture;

import java.util.List;

/**
 * Adapter for {@link NextDeparture} lists.
 *
 * @author Ben Sechrist
 */
public class DepartureArrayAdapter extends ArrayAdapter<Departure> {

  public DepartureArrayAdapter(Context context, List<Departure> departures) {
    super(context, R.layout.departure_entry, departures);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    Departure departure = getItem(position);

    if (convertView == null) {
      convertView = View.inflate(getContext(), R.layout.departure_entry, null);
    }

    TextView departureText = (TextView) convertView.findViewById(R.id.departure_text);
    departureText.setText(String.format("%s: %s", departure.getShortRouteName(),
        departure.getTextDepartureTime()));

    return convertView;
  }
}
