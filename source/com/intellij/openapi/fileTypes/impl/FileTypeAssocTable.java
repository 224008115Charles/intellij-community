/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.openapi.fileTypes.impl;

import com.intellij.openapi.fileTypes.ExtensionFileNameMatcher;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author max
 */
public class FileTypeAssocTable {
  private Map<String, FileType> myExtensionMappings = new THashMap<String, FileType>();
  private List<Pair<FileNameMatcher, FileType>> myMatchingMappings = new ArrayList<Pair<FileNameMatcher, FileType>>();

  public boolean isAssociatedWith(FileType type, FileNameMatcher matcher) {
    if (matcher instanceof ExtensionFileNameMatcher) {
      return myExtensionMappings.get(((ExtensionFileNameMatcher)matcher).getExtension()) == type;
    }

    for (Pair<FileNameMatcher, FileType> mapping : myMatchingMappings) {
      if (matcher.equals(mapping.getFirst()) && type == mapping.getSecond()) return true;
    }

    return false;
  }

  public void addAssociation(FileNameMatcher matcher, FileType type) {
    if (matcher instanceof ExtensionFileNameMatcher) {
      myExtensionMappings.put(((ExtensionFileNameMatcher)matcher).getExtension(), type);
    }
    else {
      myMatchingMappings.add(new Pair<FileNameMatcher, FileType>(matcher, type));
    }
  }

  public boolean removeAssociation(FileNameMatcher matcher, FileType type) {
    if (matcher instanceof ExtensionFileNameMatcher) {
      String extension = ((ExtensionFileNameMatcher)matcher).getExtension();
      if (myExtensionMappings.get(extension) == type) {
        myExtensionMappings.remove(extension);
        return true;
      }
      return false;
    }

    List<Pair<FileNameMatcher, FileType>> copy = new ArrayList<Pair<FileNameMatcher, FileType>>(myMatchingMappings);
    for (Pair<FileNameMatcher, FileType> assoc : copy) {
      if (matcher.equals(assoc.getFirst())) {
        myMatchingMappings.remove(assoc);
        return true;
      }
    }

    return false;
  }

  public boolean removeAllAssociations(FileType type) {
    boolean changed = false;
    Set<String> exts = myExtensionMappings.keySet();
    String[] extsStrings = exts.toArray(new String[exts.size()]);
    for (String s : extsStrings) {
      if (myExtensionMappings.get(s) == type) {
        myExtensionMappings.remove(s);
        changed = true;
      }
    }

    List<Pair<FileNameMatcher, FileType>> copy = new ArrayList<Pair<FileNameMatcher, FileType>>(myMatchingMappings);
    for (Pair<FileNameMatcher, FileType> assoc : copy) {
      if (assoc.getSecond() == type) {
        myMatchingMappings.remove(assoc);
        changed = true;
      }
    }

    return changed;
  }

  @Nullable
  public FileType findAssociatedFileType(@NotNull @NonNls String fileName) {
    for (Pair<FileNameMatcher, FileType> mapping : myMatchingMappings) {
      if (mapping.getFirst().accept(fileName)) return mapping.getSecond();
    }

    return myExtensionMappings.get(FileUtil.getExtension(fileName));
  }

  @Nullable
  public FileType findAssociatedFileType(final FileNameMatcher matcher) {
    if (matcher instanceof ExtensionFileNameMatcher) {
      return myExtensionMappings.get(((ExtensionFileNameMatcher)matcher).getExtension());
    }

    for (Pair<FileNameMatcher, FileType> mapping : myMatchingMappings) {
      if (matcher.equals(mapping.getFirst())) return mapping.getSecond();
    }

    return null;
  }

  @Deprecated
  @NotNull
  public String[] getAssociatedExtensions(FileType type) {
    Map<String, FileType> extMap = myExtensionMappings;
    return getAssociatedExtensions(extMap, type);
  }

  @NotNull
  private static String[] getAssociatedExtensions(Map<String, FileType> extMap, FileType type) {
    List<String> exts = new ArrayList<String>();
    for (String ext : extMap.keySet()) {
      if (extMap.get(ext) == type) {
        exts.add(ext);
      }
    }
    return exts.toArray(new String[exts.size()]);
  }

  @NotNull
  public FileTypeAssocTable copy() {
    FileTypeAssocTable c = new FileTypeAssocTable();
    c.myExtensionMappings = new HashMap<String, FileType>(myExtensionMappings);
    c.myMatchingMappings = new ArrayList<Pair<FileNameMatcher, FileType>>(myMatchingMappings);
    return c;
  }

  @NotNull
  public List<FileNameMatcher> getAssociations(final FileType type) {
    List<FileNameMatcher> result = new ArrayList<FileNameMatcher>();
    for (Pair<FileNameMatcher, FileType> mapping : myMatchingMappings) {
      if (mapping.getSecond() == type) {
        result.add(mapping.getFirst());
      }
    }

    for (Map.Entry<String,FileType> entries : myExtensionMappings.entrySet()) {
      if (entries.getValue() == type) {
        result.add(new ExtensionFileNameMatcher(entries.getKey()));
      }
    }

    return result;
  }

  public boolean hasAssociationsFor(final FileType fileType) {
    if (myExtensionMappings.values().contains(fileType)) return true;
    for (Pair<FileNameMatcher, FileType> mapping : myMatchingMappings) {
      if (mapping.getSecond() == fileType) return true;
    }
    return false;
  }
}
