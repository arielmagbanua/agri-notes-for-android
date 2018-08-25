package ph.com.agrinotes.agrinotes.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import ph.com.agrinotes.agrinotes.R;

public final class PermissionsUtil {
    public static final int ACCESS_FINE_LOCATION = 11;
    public static final int ACCESS_FINE_LOCATION_SETTINGS = 22;
    public static final int ACCESS_COARSE_LOCATION = 12;

    private PermissionsUtil(){}

    /**
     * This will show permission explanation dialog.
     *
     * @param dialogTitleResource int title resource.
     * @param dialogMessageResource int dialog resource.
     * @param positivePermissionDialogClickListener Dialog click listener for positive response.
     * @param negativePermissionDialogClickListener1 Dialog click listener for negative response.
     * @param cancelable True if dialog can be cancelled.
     */
    public static void showPermissionExplanationDialog(
            Context context,
            int dialogTitleResource,
            int dialogMessageResource,
            DialogInterface.OnClickListener positivePermissionDialogClickListener,
            DialogInterface.OnClickListener negativePermissionDialogClickListener1,
            boolean cancelable) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(dialogTitleResource);
        builder.setMessage(dialogMessageResource);
        builder.setPositiveButton(R.string.open_settings, positivePermissionDialogClickListener);
        builder.setNegativeButton(R.string.no_exit_this_feature, negativePermissionDialogClickListener1);
        builder.setCancelable(cancelable);
        builder.show();
    }

    /**
     * Check Permission
     *
     * @param context Context
     * @param permissionCodeString Permission Code String
     * @return boolean
     */
    public static boolean checkPermission(Context context, String permissionCodeString){
        int permissionState = ContextCompat.checkSelfPermission(context, permissionCodeString);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }
}
