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

package com.bt4vt.repository.module;

import com.bt4vt.repository.HttpBlacksburgTransitRepository;
import com.bt4vt.repository.TransitRepository;
import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * Module for the transit repository.
 *
 * @author Ben Sechrist
 */
public class TransitModule implements Module {
  @Override
  public void configure(Binder binder) {
    binder.bind(TransitRepository.class).to(HttpBlacksburgTransitRepository.class);
  }
}
