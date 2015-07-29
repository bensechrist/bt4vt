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

package com.bt4vt.repository.listener;

import com.bt4vt.repository.domain.Bus;

import java.io.Serializable;
import java.util.List;

/**
 * Callback for receiving bus updates.
 *
 * @author Ben Sechrist
 */
public interface BusListener extends Serializable {

  /**
   * Called when information is received about buses
   * @param buses the updated bus information
   */
  void onUpdateBuses(List<Bus> buses);
}
