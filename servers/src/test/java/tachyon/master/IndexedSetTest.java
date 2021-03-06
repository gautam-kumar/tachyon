/*
 * Licensed to the University of California, Berkeley under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package tachyon.master;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IndexedSetTest {
  private static class Pair {
    private int mInt;
    private long mLong;

    public Pair(int i, long l) {
      mInt = i;
      mLong = l;
    }

    public int intValue() {
      return mInt;
    }

    public long longValue() {
      return mLong;
    }
  }

  private IndexedSet<Pair> mSet;
  private IndexedSet.FieldIndex<Pair> mIntIndex;
  private IndexedSet.FieldIndex<Pair> mLongIndex;

  @Before
  public void before() {
    mIntIndex = new IndexedSet.FieldIndex<Pair>() {
      public Object getFieldValue(Pair o) {
        return o.intValue();
      }
    };
    mLongIndex = new IndexedSet.FieldIndex<Pair>() {
      public Object getFieldValue(Pair o) {
        return o.longValue();
      }
    };
    mSet = new IndexedSet<Pair>(mIntIndex, mLongIndex);
    for (int i = 0; i < 3; i ++) {
      for (long l = 0; l < 3; l ++) {
        mSet.add(new Pair(i, l));
      }
    }
  }

  @Test
  public void containsTest() {
    for (int i = 0; i < 3; i ++) {
      Assert.assertTrue(mSet.contains(mIntIndex, i));
    }
    Assert.assertFalse(mSet.contains(mIntIndex, 4));
    for (long l = 0; l < 3; l ++) {
      Assert.assertTrue(mSet.contains(mLongIndex, l));
    }
    Assert.assertFalse(mSet.contains(mLongIndex, 4L));
  }

  @Test
  public void getTest() {
    for (int i = 0; i < 3; i ++) {
      Set<Pair> set = mSet.getByField(mIntIndex, i);
      Assert.assertEquals(3, set.size());
      List<Long> longs = new ArrayList<Long>(set.size());
      for (Pair o : set) {
        longs.add(o.longValue());
      }
      Collections.sort(longs);
      for (int j = 0; j < 3; j ++) {
        Assert.assertEquals(new Long(j), longs.get(j));
      }

      set = mSet.getByField(mLongIndex, i);
      Assert.assertEquals(0, set.size()); // i is integer, must be in the same type
      set = mSet.getByField(mLongIndex, (long) i);
      Assert.assertEquals(3, set.size());
      List<Integer> ints = new ArrayList<Integer>(set.size());
      for (Pair o : set) {
        ints.add(o.intValue());
      }
      Collections.sort(ints);
      for (int j = 0; j < 3; j ++) {
        Assert.assertEquals(new Integer(j), ints.get(j));
      }
    }
  }

  @Test
  public void removeTest() {
    Pair toRemove = mSet.getFirstByField(mLongIndex, 1L);
    Assert.assertEquals(3, mSet.getByField(mLongIndex, toRemove.longValue()).size());
    Assert.assertEquals(9, mSet.size());
    Assert.assertTrue(mSet.remove(toRemove));
    Assert.assertEquals(8, mSet.size());
    Assert.assertEquals(2, mSet.getByField(mIntIndex, toRemove.intValue()).size());
    Assert.assertEquals(2, mSet.getByField(mLongIndex, toRemove.longValue()).size());
  }

  @Test
  public void removeNonExistTest() {
    Assert.assertFalse(mSet.remove(new Pair(-1, -1)));
    Assert.assertFalse(mSet.removeByField(mIntIndex, -1));
    Assert.assertFalse(mSet.removeByField(mLongIndex, -1L));
  }

  @Test
  public void removeByFieldTest() {
    Assert.assertEquals(3, mSet.getByField(mIntIndex, 1).size());
    Assert.assertEquals(9, mSet.size());
    Assert.assertTrue(mSet.removeByField(mIntIndex, 1));
    Assert.assertEquals(6, mSet.size());
    Assert.assertEquals(0, mSet.getByField(mIntIndex, 1).size());
    Assert.assertEquals(3, mSet.getByField(mIntIndex, 0).size());
    Assert.assertEquals(3, mSet.getByField(mIntIndex, 2).size());
    for (long l = 0; l < 3; l ++) {
      Assert.assertEquals(2, mSet.getByField(mLongIndex, l).size());
    }
  }

  @Test
  public void addTheSameObjectMultipleTimesTest() {
    for (int i = 0; i < 3; i ++) {
      Assert.assertEquals(9, mSet.size());
      Assert.assertEquals(3, mSet.getByField(mIntIndex, i).size());
      for (Pair p : mSet.getByField(mIntIndex, i)) {
        mSet.add(p);
      }
      Assert.assertEquals(9, mSet.size());
      Assert.assertEquals(3, mSet.getByField(mIntIndex, i).size());
    }
  }
}
