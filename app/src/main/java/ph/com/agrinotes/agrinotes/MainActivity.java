package ph.com.agrinotes.agrinotes;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ph.com.agrinotes.agrinotes.utils.PermissionsUtil;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "MainActivity";

    private FirebaseAuth auth;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private FragmentManager fragmentManager;
    private DrawerLayout drawerLayout = null;

    // Fragments
    private NotesFragment notesFragment = new NotesFragment();
    private MapFragment mapFragment = new MapFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //check the permission for the Access Fine Location
        checkAndRequestLocationBasePermissions( this,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                R.string.location_permission_concise_explanation,
                4000,
                PermissionsUtil.ACCESS_FINE_LOCATION);

        // initialize firebase auth instance
        auth = FirebaseAuth.getInstance();

        // get the user
        FirebaseUser currentUser = auth.getCurrentUser();

        //set initially the findYourJeepFragment
        fragmentManager = getSupportFragmentManager();

        //set the first item in navigation view
        MenuItem firstItem = navigationView.getMenu().getItem(0);
        setCurrentFragment(firstItem.getItemId());
        firstItem.setChecked(true);
        setTitle(firstItem.getTitle());
    }

    private void setCurrentFragment(int itemId) {
        Fragment fragment = null;
        Intent intent = null;
        int requestCode = 0;

        switch (itemId){
            case R.id.notes:

                fragment = notesFragment;

                break;

            case R.id.map:

                fragment = mapFragment;

                break;

            default:
                Toast.makeText(this, "Unknown Item!", Toast.LENGTH_SHORT).show();
        }

        if(fragment != null){
            if(!isFragmentActive(fragment)){
                Bundle args = new Bundle();
                args.putInt("item_id", itemId);
                fragment.setArguments(args);

                // Insert the fragment by replacing any existing fragment
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment)
                        .commit();
            }
        }
    }

    public boolean isFragmentActive(Fragment fragment) {
        return fragment.isAdded() && !fragment.isDetached() && !fragment.isRemoving();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        setCurrentFragment(id);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG,"onRequestPermissionsResult");

        switch (requestCode){
            case PermissionsUtil.ACCESS_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission was granted and executed immediately the features.
                    Log.i(TAG, permissions[0]+" granted!");


                } else {
                    //showPermissionExplanationDialog(R.string.permission_denied,R.string.permission_denied_cant_use_the_feature);
                    PermissionsUtil.showPermissionExplanationDialog(
                            this,
                            R.string.permission_denied,
                            R.string.permission_denied_cant_use_the_feature,
                            locationPermissionDialogClickListener,
                            locationPermissionDialogClickListener,
                            false);
                }
                break;

            case PermissionsUtil.ACCESS_COARSE_LOCATION:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PermissionsUtil.ACCESS_FINE_LOCATION_SETTINGS){
            // Check if user granted the permission from the settings
            int permissionState = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);

            if(permissionState == PackageManager.PERMISSION_GRANTED){
                // do nothing
            } else {
                // showPermissionExplanationDialog(R.string.permission_not_granted,R.string.permission_not_granted_cant_use_the_feature);
                PermissionsUtil.showPermissionExplanationDialog(
                        this,
                        R.string.permission_not_granted,
                        R.string.permission_not_granted_cant_use_the_feature,
                        locationPermissionDialogClickListener,
                        locationPermissionDialogClickListener,
                        false);
            }
        }
    }

    /**
     * This will check the runtime permission
     * @param activity Current activity or parent activity.
     * @param permission The permission to be checked.
     * @param conciseExplanationResource The resource id of the message string.
     * @param snackBarDuration The duration of snackbar.
     * @param permissionResultCode The result code for the callback.
     */
    public void checkAndRequestLocationBasePermissions(Activity activity,
                                                       String permission,
                                                       int conciseExplanationResource,
                                                       int snackBarDuration,
                                                       int permissionResultCode){

        //int accessFineLocation = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION);
        //int accessCoarseLocation = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionState = ContextCompat.checkSelfPermission(activity,permission);

        if(permissionState != PackageManager.PERMISSION_GRANTED){

            Log.i(TAG,"Permission not granted!");

            //should we show an explanation?
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity,permission)) {

                // Show an explanation to the user *asynchronously* -- don't block this thread waiting for the user's response! After the user sees the explanation, try again to request the permission.
                Log.i(TAG,"SHOULD SHOW REQUEST");

                Snackbar snack = Snackbar.make(drawerLayout, conciseExplanationResource, Snackbar.LENGTH_LONG);
                View view = snack.getView();
                TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                snack.setDuration(snackBarDuration);
                snack.show();

                ActivityCompat.requestPermissions(this, new String[]{permission},permissionResultCode);
                // requestPermissions(new String[]{permission},permissionResultCode);

            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission},permissionResultCode);
                // no explanation needed, we can request the permission.
                // requestPermissions(new String[]{permission},permissionResultCode);
            }
        }
    }

    DialogInterface.OnClickListener locationPermissionDialogClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case AlertDialog.BUTTON_POSITIVE:
                    //open the settings activity for the application
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, PermissionsUtil.ACCESS_FINE_LOCATION_SETTINGS);
                    break;

                case AlertDialog.BUTTON_NEGATIVE:
                    break;
            }
        }
    };
}
