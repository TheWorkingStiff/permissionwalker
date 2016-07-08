//For Detail look into 
//getInstallerPackageName
// getPackageInfo
package com.tyz.permissionwalker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

public class PackageActivity extends ListActivity {
	private static final String endl = "\n";
	
	ListAdapter mListAdapter = null;
	String TAG = "permissionwalker.PackageActivity";
	int mSelection = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.permissions);

	    AutoCompleteTextView autoTextView = (AutoCompleteTextView) findViewById(R.id.autocomplete_permission);
	    String[] packages = parsePackages(getPackageManager().getInstalledApplications(0));
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, packages);
	    autoTextView.setAdapter(adapter);

	    autoTextView.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP) showAlert(v);
			    return true;
			}
		});
	    
	    setListAdapter(new ArrayAdapter<String>(this, 
	    										R.layout.auto_list_item, 
	    										R.id.text, packages));
		  
	    StringBuilder sb = new StringBuilder(
	    		"" );
	    TextView header = (TextView) findViewById(R.id.heading);
	    header.setText(sb);

	    ListView lv = getListView();
	    /*	    if (state != null){
	    	    	lv.onRestoreInstanceState(state);
	    	    }*/
	    lv.setOnItemClickListener(new OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view,
	          int position, long id) {
	        showAlert(view);
	      }
	    });	    
	    registerForContextMenu(lv);
	    	    
	}

	
	private void showAlert(View view ){
		//Look at the strings in mPermissions and if they begin with andPath
		// then remove andpath from the output string.
        String item = ((TextView)view).getText().toString();
        StringBuilder postString = new StringBuilder("You have granted [" + item + 
        								"] the following permissions:" + endl);
        String[] perms = getPermissions(item);
        if(null == perms){
        	postString.append("none");
        }else{
	        for (String perm : perms){
	       		postString.append("-------" + endl + perm + endl + "-------" );  
	        }        
        }
        AlertDialog alert = new AlertDialog.Builder(PackageActivity.this).create();
        alert.setTitle("Show Permissions");
        alert.setMessage(postString);
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            } 
        });
        
        alert.setIcon(R.drawable.small_eye);
        alert.show();
		
	}

	
	/*
	 * parsePackages 
	 * returns a String[] 
	 * This list is intended to be used for display purposes.
	 * It is sorted and contains a compination of short and long names 
	 */
	String[] parsePackages(List<ApplicationInfo> ainfo){
		/*
		 * Assumptions. 
		 * There will be packages
		 * They will all yield a third token
		 */
		ArrayList<String> AInfos = new ArrayList<String>();
		
		for (ApplicationInfo item : ainfo)
		{
			String delims = "[ }{]+";
			String[] tokens = item.toString().split(delims);

			String name = null;
			if(tokens.length > 2){
				name = tokens[2];
	   			AInfos.add(flipPath(name));
			}else {
	    		Log.d(TAG,"Error, "+ flipPath(name));				
			}
 		}
		Collections.sort(AInfos);
		return AInfos.toArray(new String[0]);
		
	}
	
	// flipPath takes a string in the format com.tyz.BadIntent
	//  and returns BadIntent \n (com.tyz)
	private String flipPath(String orig){
		String delims = "[.]";
		String[] tokens = orig.split(delims);
		String firstPart = tokens[tokens.length-1] ;
		String secondPart = null;
		if(orig.lastIndexOf(".")>0)
			secondPart = orig.substring(0,orig.lastIndexOf('.'));
		if (null!=firstPart && null != secondPart){
			return firstPart+ "\n\t" + secondPart;
		}else{
			return firstPart;
		}
			
	}
	
	private String unflipPath(String orig){
		String delims = "['\n''\t']+";
		String[] tokens = orig.split(delims);
		String retString = null;
		if(tokens.length > 1){
			retString = tokens[1] + "." + tokens[0];					
		}else{
			retString = tokens[0];
		}
		
		return retString;
	}

	protected boolean onLongListItemClick(View v, int pos, long id) {
        Log.i(TAG, "onLongListItemClick id=" + id);
        mSelection = pos;
        return true;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	      
    	menu.setHeaderTitle("Interrogate this Activity");
		menu.add(0, v.getId(), 0, "Show Permissions");
		menu.add(0, v.getId(), 0, "Package Details");
        
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        } 		
        
		mSelection = (int) getListAdapter().getItemId(info.position); 		
    }
    
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info= null;    	
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
		}     	
		if(item.getTitle()=="Show Permissions"){ 
   	
    		showAlert(info.targetView);      	}
    	else if(item.getTitle()=="Package Details"){
    		showDetails(info.targetView);
    	}	
    	else {return false;}

       	return true;
	}
	
	private String[] getPermissions(String item){
		PackageManager pm = getPackageManager();
		PackageInfo pi = null;

		try {
			//pi = pm.getPackageInfo(item, PackageManager.GET_PERMISSIONS);
			pi = pm.getPackageInfo(unflipPath(item), PackageManager.GET_PERMISSIONS);
			
		} catch (NameNotFoundException e) {
			e.printStackTrace();

		}

		return pi.requestedPermissions;
	}
	
	private void showDetails(View view) {
	    String item = ((TextView)view).getText().toString();
		String myPackage = unflipPath(item); 
		try {
			new AlertDialog.Builder(this)
				.setTitle(item)
				.setMessage("Details:\n" +
						getDetailStr(view))
				.setNeutralButton("What else?", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int sumthin) {
						
					}
				})
				.setIcon(getPackageManager().getApplicationIcon(myPackage))
				.show();
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getDetailStr(View view){
		String retStr = "";
	    String item = ((TextView)view).getText().toString();
	    Log.d(TAG,"looking at: " + item);
		String myPackage = unflipPath(item); 
		PackageManager pm = getPackageManager();
		ApplicationInfo ai = null; 
		try {
			ai = pm.getApplicationInfo(myPackage, 0);
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
		e1.printStackTrace(); 
		}
		PackageInfo pi = null;
		String installedBy = pm.getInstallerPackageName(myPackage);
		retStr += "------------" + "\n" + "descriptionRes: "+ ai.descriptionRes + "\n";
		retStr += "------------" + "\n" + "loadDescription: "+ ai.loadDescription(pm) + "\n";
		retStr += "------------" + "\n" + "name: "+ ai.name + "\n";
		retStr += "------------" + "\n" + "packageName: "+ ai.packageName + "\n";
		retStr += "------------" + "\n" + "processName: "+ ai.processName + "\n";
		retStr += "------------" + "\n" + "targetSdkVersion(min): "+ ai.targetSdkVersion + "\n";
		retStr += "------------" + "\n" + "InstalledBy: "+ installedBy + "\n";
		retStr += "------------" + "\n" + "dataDir: "+ ai.dataDir + "\n";
		retStr += "------------" + "\n" + "publicSourceDir: "+ ai.publicSourceDir + "\n";
		retStr += getFlagSettings(ai);
		return retStr;
	}
	
	private String getFlagSettings(ApplicationInfo ai){
		StringBuilder retStr = new StringBuilder("------------\nFLAGS:\n");
		String[] flagStrings = {"SYSTEM", "DEBUGGABLE", "HAS_CODE", 
								"PERSISTENT", "FACTORY_TEST", 
								"ALLOW_TASK_REPARENTING", "ALLOW_CLEAR_USER_DATA", 
								"UPDATED_SYSTEM_APP", "TEST_ONLY", "SUPPORTS_SMALL_SCREENS", 
								"SUPPORTS_NORMAL_SCREENS", "SUPPORTS_LARGE_SCREENS", 
								"RESIZEABLE_FOR_SCREENS", 
								"SUPPORTS_SCREEN_DENSITIES", "VM_SAFE_MODE"};
		
		int[] flagInts = {ApplicationInfo.FLAG_SYSTEM, ApplicationInfo.FLAG_DEBUGGABLE, ApplicationInfo.FLAG_HAS_CODE, 
							 ApplicationInfo.FLAG_PERSISTENT, ApplicationInfo.FLAG_FACTORY_TEST, 
							 ApplicationInfo.FLAG_ALLOW_TASK_REPARENTING, ApplicationInfo.FLAG_ALLOW_CLEAR_USER_DATA, 
							 ApplicationInfo.FLAG_UPDATED_SYSTEM_APP, ApplicationInfo.FLAG_TEST_ONLY, ApplicationInfo.FLAG_SUPPORTS_SMALL_SCREENS, 
							 ApplicationInfo.FLAG_SUPPORTS_NORMAL_SCREENS, ApplicationInfo.FLAG_SUPPORTS_LARGE_SCREENS, 
							 ApplicationInfo.FLAG_RESIZEABLE_FOR_SCREENS, 
							 ApplicationInfo.FLAG_SUPPORTS_SCREEN_DENSITIES, ApplicationInfo.FLAG_VM_SAFE_MODE};
		for(int i=0;i<flagInts.length-1;i++){
			retStr.append(flagStrings[i] + ":");
			retStr.append((((ai.flags & flagInts[i]) != 0) ?  " true": " false") + "\n");
		}	
		
		return retStr.toString();
		
	}
}
