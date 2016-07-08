package com.tyz.permissionwalker;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;

public class PermissionWalker extends TabActivity {
	private static final String endl = "\n";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab
 
        // Do the same for the other tabs
        intent = new Intent().setClass(this, PackageActivity.class);
        spec = tabHost.newTabSpec("Packages").setIndicator("Packages",
                          res.getDrawable(R.drawable.ic_tab_store))
                      .setContent(intent);
        tabHost.addTab(spec);

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, PermissionActivity.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("Permissions").setIndicator("Permissions",
                          res.getDrawable(R.drawable.ic_tab_permit))
                      .setContent(intent);
        tabHost.addTab(spec);
        
        tabHost.setCurrentTab(2);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {

		new MenuInflater(this).inflate(R.menu.menu, menu);


		return(super.onCreateOptionsMenu(menu));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.help:
				showHelp(this.getCurrentFocus());
				return(true);
			case R.id.about:
				//Toast.makeText(this, "About", Toast.LENGTH_LONG).show();
				showAbout(this.getCurrentFocus());
				return(true);

		}
		
		return(super.onOptionsItemSelected(item));
	}

	public void showAbout(View view) {
		new AlertDialog.Builder(this)
			.setTitle("About PermissionWalker")
			.setMessage("PermissionWalker is a tool for examining the permissions you have granted to your installed packages. " +
					"\nCreated by Daniel Kahn. Internal icons by Joseph Wain / glyphish.com.")
			.setNeutralButton("Thank you for using it", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dlg, int sumthin) {
					
				}
			})
			.show();
	}
	
	public void showHelp(View view) {
		new AlertDialog.Builder(this)
			.setTitle("PermissionWalker Help")
			.setMessage("- Long click on the lists or type into the auto-complete drop-down" +  endl +
				"  Click to show permissions and the packages that have them." + endl +
	    		"- All packages and permissions used on this phone are listed. " +  endl )
			.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dlg, int sumthin) {
					
				}
			})
			.show();
	}
}