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

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * ActiveAndroid database model for favorited stops.
 *
 * @author Ben Sechrist
 */
@Table(name = "StopFavorites")
public class StopFavorite extends Model {

  @Column(notNull = true, unique = true)
  private Integer code;

  @Column(notNull = true)
  private boolean isFavorited = false;

  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public boolean isFavorited() {
    return isFavorited;
  }

  public void setFavorited(boolean favorited) {
    isFavorited = favorited;
  }

  @Override
  public int hashCode() {
    if (getCode() != null) {
      return getCode().hashCode();
    }
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof Stop)) return false;
    Stop that = (Stop) o;
    return !(this.getCode() == null || that.getCode() == null) && this.getCode().equals(that.getCode());
  }

  @Override
  public String toString() {
    return String.format("#%d - ", getCode(), (isFavorited() ? "favorited" : "not favorited"));
  }
}
