// Copyright 2016 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.skyframe;

import com.google.devtools.build.lib.cmdline.RepositoryName;
import com.google.devtools.build.lib.vfs.RootedPath;
import com.google.devtools.build.skyframe.LegacySkyKey;
import com.google.devtools.build.skyframe.SkyKey;
import com.google.devtools.build.skyframe.SkyValue;

/**
 * A value that represents a local repository lookup result.
 *
 * <p>Local repository lookups will always produce a value. The {@code #getRepository} method
 * returns the name of the repository that the directory resides in.
 */
public abstract class LocalRepositoryLookupValue implements SkyValue {

  static SkyKey key(RootedPath directory) {
    return LegacySkyKey.create(SkyFunctions.LOCAL_REPOSITORY_LOOKUP, directory);
  }

  private static final LocalRepositoryLookupValue MAIN_REPO_VALUE = new MainRepositoryLookupValue();
  private static final LocalRepositoryLookupValue NOT_FOUND_VALUE =
      new NotFoundLocalRepositoryLookupValue();

  public static LocalRepositoryLookupValue mainRepository() {
    return MAIN_REPO_VALUE;
  }

  public static LocalRepositoryLookupValue success(RepositoryName repositoryName) {
    return new SuccessfulLocalRepositoryLookupValue(repositoryName);
  }

  public static LocalRepositoryLookupValue notFound() {
    return NOT_FOUND_VALUE;
  }

  /**
   * Returns {@code true} if the local repository lookup succeeded and the {@link #getRepository}
   * method will return a useful result.
   */
  public abstract boolean exists();

  /**
   * Returns the {@link RepositoryName} of the local repository contained in the directory which was
   * looked up, {@link RepositoryName#MAIN} if the directory is part of the main repository, or
   * throws a {@link IllegalStateException} if there was no repository found.
   */
  public abstract RepositoryName getRepository();

  /** Represents a successful lookup of the main repository. */
  public static final class MainRepositoryLookupValue extends LocalRepositoryLookupValue {

    // This should be a singleton value.
    private MainRepositoryLookupValue() {}

    @Override
    public boolean exists() {
      return true;
    }

    @Override
    public RepositoryName getRepository() {
      return RepositoryName.MAIN;
    }

    @Override
    public String toString() {
      return "MainRepositoryLookupValue";
    }

    @Override
    public boolean equals(Object obj) {
      // All MainRepositoryLookupValue instances are equivalent.
      return obj instanceof MainRepositoryLookupValue;
    }

    @Override
    public int hashCode() {
      return MainRepositoryLookupValue.class.getSimpleName().hashCode();
    }
  }

  /** Represents a successful lookup of a local repository. */
  public static final class SuccessfulLocalRepositoryLookupValue
      extends LocalRepositoryLookupValue {
    private final RepositoryName repositoryName;

    public SuccessfulLocalRepositoryLookupValue(RepositoryName repositoryName) {
      this.repositoryName = repositoryName;
    }

    @Override
    public boolean exists() {
      return true;
    }

    @Override
    public RepositoryName getRepository() {
      return repositoryName;
    }

    @Override
    public String toString() {
      return "SuccessfulLocalRepositoryLookupValue(" + repositoryName + ")";
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof SuccessfulLocalRepositoryLookupValue)) {
        return false;
      }
      SuccessfulLocalRepositoryLookupValue other = (SuccessfulLocalRepositoryLookupValue) obj;
      return repositoryName.equals(other.repositoryName);
    }

    @Override
    public int hashCode() {
      return repositoryName.hashCode();
    }
  }

  /** Represents the state where no repository was found, either local or the main repository. */
  public static final class NotFoundLocalRepositoryLookupValue extends LocalRepositoryLookupValue {

    // This should be a singleton value.
    private NotFoundLocalRepositoryLookupValue() {}

    @Override
    public boolean exists() {
      return false;
    }

    @Override
    public RepositoryName getRepository() {
      throw new IllegalStateException("Repository was not found");
    }

    @Override
    public String toString() {
      return "NotFoundLocalRepositoryLookupValue";
    }

    @Override
    public boolean equals(Object obj) {
      // All NotFoundLocalRepositoryLookupValue instances are equivalent.
      return obj instanceof NotFoundLocalRepositoryLookupValue;
    }

    @Override
    public int hashCode() {
      return NotFoundLocalRepositoryLookupValue.class.getSimpleName().hashCode();
    }
  }
}
