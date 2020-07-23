package com.merge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ItemFile {

    public final String name;
    public final boolean isDir;
    public final List<ItemFile> children;

    public ItemFile(String name) {
        this(name, true);
    }

    public ItemFile(String name, boolean isDir) {
        this.name = name;
        this.isDir = isDir;
        if (isDir) {
            children = new ArrayList<>();
        } else {
            children = Collections.emptyList();
        }
    }

    public ItemFile addItemFile(ItemFile... itemFiles) {
        children.addAll(Arrays.asList(itemFiles));
        return this;
    }

    public ItemFile hasChild(String name) {
        if (this.name.equals(name)) {
            return this;
        }
        for (ItemFile child : children) {
            if (child.name.equalsIgnoreCase(name)) {
                return child;
            }
        }
        return null;
    }
}