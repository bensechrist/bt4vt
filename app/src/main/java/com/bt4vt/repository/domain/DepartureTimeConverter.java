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

package com.bt4vt.repository.domain;

import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Converts departure times.
 *
 * @author Ben Sechrist
 */
public class DepartureTimeConverter implements Converter<Date> {

  public static final SimpleDateFormat FORMAT = new SimpleDateFormat("M/d/yyyy h:mm:ss a", Locale.US);

  @Override
  public Date read(InputNode inputNode) throws Exception {
    FORMAT.setTimeZone(TimeZone.getTimeZone("EST"));
    return FORMAT.parse(inputNode.getValue());
  }

  @Override
  public void write(OutputNode outputNode, Date date) throws Exception {
    outputNode.setValue(FORMAT.format(date));
  }
}
