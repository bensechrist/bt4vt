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

package com.bt4vt.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Active Android favorite stop model.
 *
 * @author Ben Sechrist
 */
@Table(name = "StopFavorites")
public class FavoriteStop extends Model {

  @Column(notNull = true, unique = true)
  private String code;

  @Column(notNull = true)
  private boolean isFavorited = false;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public boolean isFavorited() {
    return isFavorited;
  }

  public void setFavorited(boolean favorited) {
    isFavorited = favorited;
  }
}
