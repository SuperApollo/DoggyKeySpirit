package com.apollo.keyspirit.listener;

import java.util.List;

public interface PermissionListener {
    void granted();
    void denied(List<String> deniedList);
}
