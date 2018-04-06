package ca.bellmedia.permissionshelper;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import ca.bellmedia.permissionshelper.PermissionsHelper.OnPermissionsResultListener;

/**
 * Fragment with no view created to handle permission requests. This fragment will be destroyed once it emits its result to the listener.
 * Only permissions that are not granted already will be requested.
 * <p>
 * Created by Alvaro Ibarguen on 2018-03-08.
 */
public final class PermissionsFragment extends Fragment {
    public static final String TAG = PermissionsFragment.class.getSimpleName();
    public static final int PERMISSIONS_REQUEST_CODE = 322;

    private OnPermissionsResultListener onPermissionsResultListener;

    @NonNull private final List<Permission> requestedPermissions = new LinkedList<>();

    /**
     * Requests a list of permissions. If the user's device is not running API 23 or greater, OR if the app is targeting an API level lower
     * than 22, then it will automatically return all permissions as granted. It will also filter all permissions that have been granted already
     * or if the flag shouldEnforceOncePerSession is set to true and that permission has been requested in the session.
     *
     * @param permissions                 Array of permissions to request.
     * @param shouldEnforceOncePerSession Flag used to check if the permission should once be requested once per session.
     * @param onPermissionsResultListener Listener to post the results to.
     */
    @TargetApi (VERSION_CODES.M)
    @UiThread
    public final void requestPermissions(@NonNull final String[] permissions, final boolean shouldEnforceOncePerSession,
            @NonNull final OnPermissionsResultListener onPermissionsResultListener) {
        this.onPermissionsResultListener = onPermissionsResultListener;

        int targetSdkVersion;
        try {
            final PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            targetSdkVersion = packageInfo.applicationInfo.targetSdkVersion;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Error parsing target sdk version.", e);
            targetSdkVersion = -1;
        }

        if (!PermissionsHelper.isMarshmallow() || (targetSdkVersion < Build.VERSION_CODES.M && targetSdkVersion != -1)) {
            for (final String permission : permissions) {
                requestedPermissions.add(new Permission(permission, true, false));
            }
            onPermissionsResultListener.onPermissionResult(requestedPermissions.toArray(new Permission[requestedPermissions.size()]));
        } else {
            final String[] permissionsToRequest = filterGrantedPermissions(permissions, shouldEnforceOncePerSession);
            if (permissionsToRequest.length > 0) {
                requestPermissions(permissionsToRequest, PERMISSIONS_REQUEST_CODE);
            } else {
                onPermissionsResultListener.onPermissionResult(requestedPermissions.toArray(new Permission[requestedPermissions.size()]));
            }
        }
    }

    @TargetApi (VERSION_CODES.M)
    @UiThread
    @Override
    public final void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                for (int i = 0, len = permissions.length; i < len; i++) {
                    final String permission = permissions[i];
                    final boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                    requestedPermissions.add(new Permission(permission, granted, shouldShowRequestPermissionRationale(permission)));
                }

                onPermissionsResultListener.onPermissionResult(requestedPermissions.toArray(new Permission[requestedPermissions.size()]));
                break;
        }
    }

    /**
     * Accepts a list of permissions and filters out all the permissions that have already been granted. Permissions that have
     * already been requested this session, permissions that are already granted, OR permissions that have been denied and the user
     * clicked "do not show again" are added to the requestedPermissions list.
     *
     * @param permissions                 Permissions to request.
     * @param shouldEnforceOncePerSession Should check if the permission has already been requested on this session.
     *
     * @return A list of permissions that have not been granted already.
     */
    @TargetApi (VERSION_CODES.M)
    @AnyThread
    @NonNull
    private String[] filterGrantedPermissions(@NonNull final String[] permissions, final boolean shouldEnforceOncePerSession) {
        final List<String> permissionToRequest = new LinkedList<>();
        for (final String permission : permissions) {
            boolean granted = ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED;
            if (granted || (shouldEnforceOncePerSession && PermissionsHelper.alreadyRequestedInSession(permission))) {
                //Adds permission to the already requested list
                requestedPermissions.add(new Permission(permission, granted, false));
            } else {
                permissionToRequest.add(permission);
            }
        }

        return permissionToRequest.toArray(new String[permissionToRequest.size()]);
    }
}
