package com.tyz.permissionwalker;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class PermissionActivity extends ListActivity {

	private static final String endl = "\n";
	private static String[] mSPermissions = null;  			//	Why do I use
	Hashtable<String, StringBuffer> mPermissions = null;	//   both of these?
	ListAdapter mListAdapter = null;
	String TAG = "PermissionWalker.PermissionActivity";
	int mSelection = 0; 
	Parcelable state = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.permissions);

	    AutoCompleteTextView autoTextView = (AutoCompleteTextView) findViewById(R.id.autocomplete_permission);
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, getPermissions());
	    autoTextView.setAdapter(adapter);

	    autoTextView.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP) showAlert(v);
			    return true;
			}
		});
	    
	    setListAdapter(new ArrayAdapter<String>(this, R.layout.auto_list_item, R.id.text, getPermissions()));
		  
	    
	    StringBuilder sb = new StringBuilder(
	    		"" );
	    TextView header = (TextView) findViewById(R.id.heading);
	    header.setText(sb);

	    ListView lv = getListView();
	    registerForContextMenu(lv);

	    lv.setOnItemClickListener(new OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view,
	          int position, long id) {
	        showAlert(view);
	      }
	    });

	}
	// flipPath takes a string in the format "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS"
	//  and returns "ACCESS_LOCATION_EXTRA_COMMANDS\n\tandroid.permission"
	private String flipPath(String orig){
		String delimWord = "permission.";
		
		int permIndex = orig.lastIndexOf(delimWord) + delimWord.length();
		
		permIndex = (-1 == orig.lastIndexOf(delimWord)) ?
												orig.lastIndexOf(".")+1:
												orig.lastIndexOf(delimWord) + delimWord.length();
		String firstPart = orig.substring(permIndex);
		String secondPart = orig.substring(0,permIndex-1);

		return firstPart + "\n\t" + secondPart;
	}
	
	private String unflipPath(String orig){
		String delims = "['\n''\t']+";
		String[] tokens = orig.split(delims);
		String retString = tokens[1] + "." + tokens[0];
		return retString;
	}
	
	private String [] getPermissions(){
		if(mSPermissions == null){
			Hashtable<String, StringBuffer> myHash = getPackages(); //We'll need this for size
			Iterator<String> iterator = myHash.keySet().iterator();
			String[] retStrArr = new String[myHash.size()];
			int i=0;
			while ( iterator.hasNext()) {
				String s = flipPath((String) iterator.next());			
				retStrArr[i++]= s;
			}
			Arrays.sort(retStrArr);
			mSPermissions = retStrArr;
		}
		return mSPermissions;
	}
	
	private void showAlert(View view ){

        String item = ((TextView)view).getText().toString();

        StringBuffer parsedItem = mPermissions.get(unflipPath(item));
        parsedItem = new StringBuffer (parsedItem.toString().replace("|", endl + "-------" + endl)); 
        String postString = "You have granted " + endl + "[" + item + "] to:" + parsedItem;
        
        AlertDialog alert = new AlertDialog.Builder(PermissionActivity.this).create();
        alert.setTitle("Packages");
        alert.setMessage(postString);
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { 
            } 
        });
        
        alert.setIcon(R.drawable.small_eye);
        alert.show();
		
	}
		
	private Hashtable<String, StringBuffer> getPackages(){	
		//return buildDictionary(getPackageManager().getInstalledApplications(0));
		return buildDictionary(getPackageManager().getInstalledPackages(0));
	}
	
	private Hashtable<String, StringBuffer> buildDictionary(List<PackageInfo> pinfo){
		if(null==mPermissions){
			mPermissions = new Hashtable<String, StringBuffer>();
			PackageManager pm = getPackageManager();
//			for (ApplicationInfo item : ainfo)
			for (PackageInfo item : pinfo)
			{
				PackageInfo pi = null;
				try {
					pi = pm.getPackageInfo(item.packageName, PackageManager.GET_PERMISSIONS);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
	
				if(null == pi.requestedPermissions) continue;
				for(String perm : pi.requestedPermissions){
					//If this permission is not in the Hashtable...
					if(!mPermissions.containsKey(perm.toString())){
						//Add a key "perm" and add the value preceeded by a pipe delimiter
						//mPermissions.put(perm, new StringBuffer("|" + item.processName));
						mPermissions.put(perm, new StringBuffer("|" + item.packageName));
					}else{
						//If the key for this permission is already in the Hashtable... ie this is
						// not the first package using this key... 
						//mPermissions.put(perm, new StringBuffer(mPermissions.get(perm).toString() +  "|" + (item.processName)));
						mPermissions.put(perm, new StringBuffer(mPermissions.get(perm).toString() +  "|" + (item.packageName)));
					}
				}
			}
		}
		return mPermissions;
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
		menu.add(0, v.getId(), 0, "Show Permitted Packages");
		menu.add(0, v.getId(), 0, "Permission Details");
        
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
    	//Intent intent = null;
    	AdapterView.AdapterContextMenuInfo info= null; 
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
		}     	
    	if(item.getTitle()=="Show Permitted Packages"){
    		showAlert(info.targetView);
       	}
    	else if(item.getTitle()=="Permission Details"){
    		showDetails(info.targetView);
    	}	
    	else {return false;}

       	return true;
	}
	
	public void showDetails(View view) {
		//TODO: Look into getPermissionInfo

        String item = ((TextView)view).getText().toString();
		String perm = unflipPath(item);
		new AlertDialog.Builder(this)
			.setTitle(item)
			.setIcon(R.drawable.ic_tab_perm_grey)
			.setMessage("Details:\n" + getDetailString(view))
			.setNeutralButton("What else?", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dlg, int sumthin) {
					
				}
			})
			.show();
	}
	
	private String getDetailString(View view) {
	    String item = ((TextView)view).getText().toString();
	    String perm = unflipPath(item);
		//String desc = mapPermissions.get(perm);
		String retStr  = perm + " " + "\n"; //+ (desc == null? " - No Description Available": desc) + "\n";
		PackageManager pm = getPackageManager();
		try {
			PermissionInfo pi = pm.getPermissionInfo(perm, PackageManager.GET_META_DATA);
			retStr += "Description: "+ pi.loadDescription(pm) + "\n";
			retStr += "------------" + "\n" + "protectionLevel: "+ pi.protectionLevel + "\n";
			retStr += "------------" + "\n" + "group: "+ pi.group + "\n";
			
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retStr;
	}
}
