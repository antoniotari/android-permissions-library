package ca.bellmedia.permissionshelper;

import android.support.annotation.NonNull;

/**
 * Class to encapsulate the state of a permission that was requested.
 * Created by Alvaro Ibarguen on 2018-03-08.
 */
public final class Permission {

    @NonNull private final String permission;
    private final boolean granted;
    private final boolean shouldShowPermissionRationale;

    /* package */ Permission(@NonNull final String permission, final boolean granted, final boolean shouldShowPermissionRationale) {
        this.permission = permission;
        this.granted = granted;
        this.shouldShowPermissionRationale = shouldShowPermissionRationale;
    }

    @NonNull
    public String getPermission() {
        return permission;
    }

    public boolean isGranted() {
        return granted;
    }

    public boolean shouldShowPermissionRationale() {
        return shouldShowPermissionRationale;
    }
}
