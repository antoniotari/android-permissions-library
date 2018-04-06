package ca.bellmedia.permissionshelper;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Build;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Helper class to handle permission requests.
 * Created by Alvaro Ibarguen on 2018-03-08.
 */
public final class PermissionsHelper {
    private static final String TAG = "PermissionsHelper";

    private static Set<String> permissions = new CopyOnWriteArraySet<>();

    /**
     * Class cannot be instantiated.
     */
    private PermissionsHelper() {
        // No-op
    }

    /**
     * Requests a list of permissions and posts the result to an {@link OnPermissionsResultListener}. This function creates a empty fragment to request the
     * permissions and destroys it once the request returns. The listener will receive a list of {@link Permission}s. The permission object contains the
     * permission name, the granted status, and the {@link android.app.Fragment#shouldShowRequestPermissionRationale(String)} result.
     *
     * @param activity                    Context to request the permission on.
     * @param permissions                 Array of permissions to request.
     * @param shouldEnforceOncePerSession Flag used to check if the permission should once be requested once per session.
     * @param onPermissionsResultListener Listener to post the results to.
     */
    @AnyThread
    public static void requestPermissions(@NonNull final Activity activity, @NonNull final String[] permissions, final boolean shouldEnforceOncePerSession,
            @NonNull final OnPermissionsResultListener onPermissionsResultListener) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final PermissionsFragment permissionsFragment = new PermissionsFragment();
                final FragmentManager fragmentManager = activity.getFragmentManager();
                fragmentManager.beginTransaction()
                        .add(permissionsFragment, TAG)
                        .commitAllowingStateLoss();
                fragmentManager.executePendingTransactions();

                permissionsFragment.requestPermissions(permissions, shouldEnforceOncePerSession, new OnPermissionsResultListener() {
                    @UiThread
                    @Override
                    public final void onPermissionResult(@NonNull final Permission[] requestedPermissions) {
                        onPermissionsResultListener.onPermissionResult(requestedPermissions);

                        //Remove and destroy fragment
                        fragmentManager.beginTransaction()
                                .remove(permissionsFragment)
                                .commitAllowingStateLoss();
                        fragmentManager.executePendingTransactions();

                        PermissionsHelper.permissions.addAll(Arrays.asList(permissions));
                    }
                });
            }
        });
    }

    /**
     * Checks if a permission has already been requested in this session.
     *
     * @param permission Permission to check.
     *
     * @return True if the permission has already been requested on this session; false otherwise.
     */
    /* package */
    static boolean alreadyRequestedInSession(@NonNull final String permission) {
        return permissions.contains(permission);
    }

    /**
     * @return Returns true if the SDK_INT is higher than 23; false otherwise.
     */
    @AnyThread
    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Callback interface for permission request results.
     */
    public interface OnPermissionsResultListener {
        @UiThread
        void onPermissionResult(@NonNull final Permission[] requestedPermissions);
    }
}
