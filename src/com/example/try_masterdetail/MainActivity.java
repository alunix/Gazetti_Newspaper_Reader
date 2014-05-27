package com.example.try_masterdetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.try_masterdetail.AddCellDialogFragment.AddCellDialogListener;
import com.example.try_masterdetail.EditCellDialogFragment.EditCellDialogListener;
import com.example.try_masterdetail.news_activities.WebsiteListActivity;
import com.nhaarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

public class MainActivity extends FragmentActivity implements AddCellDialogListener, EditCellDialogListener {

	List<GridCellModel> cellList;
	ImageAdapter adapter;
	FragmentManager fm = getSupportFragmentManager();
	private final static String TAG = "HomeScreen";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.homescreen_activity);
		GridView gridview = (GridView) findViewById(R.id.gridview);

		if (findViewById(R.id.homescreen_background_kenburns) == null) {
			loadImageForBackground();
		}

		cellList = new ArrayList<GridCellModel>();

		cellList.add(new GridCellModel("th", "Sports"));
		cellList.add(new GridCellModel("th", "National"));
		cellList.add(new GridCellModel("th", "International"));
		cellList.add(new GridCellModel("toi", "National"));
		cellList.add(new GridCellModel("toi", "Science"));
		cellList.add(new GridCellModel("toi", "Entertainment"));
		cellList.add(new GridCellModel("add_new", "Add New"));

		adapter = new ImageAdapter(this, cellList);

		SwingBottomInAnimationAdapter animAdapter = new SwingBottomInAnimationAdapter(adapter);
		AlphaInAnimationAdapter animAdapterMultiple = new AlphaInAnimationAdapter(animAdapter);
		animAdapterMultiple.setAbsListView(gridview);

		gridview.setAdapter(animAdapterMultiple);

		// gridview.setAdapter(adapter);

		registerForContextMenu(gridview);

		if (isFirstTime()) {

			new AlertDialog.Builder(MainActivity.this).setTitle("Updated")
					.setMessage(R.string.first_message)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					}).show();

		}

		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

				if (position == (cellList.size() - 1)) {
					showAddNewCellDialog();
				} else {
					GridCellModel clickedObject = cellList.get(position);
					String npImage = clickedObject.getNewspaperImage();
					String catName = clickedObject.getTitleCategory();

					ReadCSV readCsv = new ReadCSV(MainActivity.this);
					CSVObject csvObject = readCsv.getObjectByNPImage(npImage, catName);

					String npId = csvObject.getNpId();
					String catId = csvObject.getCatId();
					String npName = csvObject.getNpName();

					npId = String.valueOf(Integer.parseInt(npId) + 1);
					catId = String.valueOf(Integer.parseInt(catId) + 1);

					Intent detailIntent = new Intent(MainActivity.this, WebsiteListActivity.class);
					detailIntent.putExtra("npId", npId);
					detailIntent.putExtra("catId", catId);
					detailIntent.putExtra("npName", npName);
					detailIntent.putExtra("catName", catName);
					startActivity(detailIntent);

				}

			}
		});

	}

	private void loadImageForBackground() {
		// get a random image, if null then get image_0
		Random rand = new Random();
		int n = rand.nextInt(4) + 1;
		String backgroundImageUri = "image_" + n;
		int resID = getResources().getIdentifier(backgroundImageUri, "drawable", getPackageName());
		System.out.println(n + ", " + resID);
		if (resID == 0) {
			resID = getResources().getIdentifier("image_0", "drawable", getPackageName());
		}

		// Bitmap Options
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(getResources(), resID, options);

		// Raw height and width of image
		int imageHeight = options.outHeight;
		int imageWidth = options.outWidth;
		int inSampleSize = 1;

		// height and width of screen
		int reqHeight = getResources().getDisplayMetrics().heightPixels;
		int reqWidth = getResources().getDisplayMetrics().widthPixels;

		Log.d(TAG, "image - " + imageHeight + " x " + imageWidth + " , screen - " + reqHeight + " x " + reqWidth);

		// SampleSize Calculations
		if (imageHeight > reqHeight || imageWidth > reqWidth) {

			final int halfHeight = imageHeight / 2;
			final int halfWidth = imageWidth / 2;

			// Calculate the largest inSampleSize value that is a power of 2
			// and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}

			// This offers some additional logic in case the image has a strange
			// aspect ratio. Anything more than 2x the requested pixels we'll
			// sample down
			// further
			long totalPixels = imageWidth * imageHeight / inSampleSize;
			final long totalReqPixelsCap = reqWidth * reqHeight * 2;

			while (totalPixels > totalReqPixelsCap) {
				inSampleSize *= 2;
				totalPixels /= 2;
			}
		}

		Log.d(TAG, "inSampleSize - " + inSampleSize);

		// TODO: Check what is inBitmap

		options.inJustDecodeBounds = false;
		Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), resID, options);
		ImageView homeScreenBackground = (ImageView) findViewById(R.id.homescreen_background);
		homeScreenBackground.setImageBitmap(mBitmap);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.gridview_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		int position = info.position;
		String newspaper = cellList.get(position).getNewspaperImage();
		String category = cellList.get(position).getTitleCategory();

		switch (item.getItemId()) {
		case R.id.edit:
			if (position == (cellList.size() - 1)) {
				Toast.makeText(this, "Cannot Edit", Toast.LENGTH_SHORT).show();
				return true;
			}
			showEditCellDialog(position, newspaper, category);
			return true;
		case R.id.delete:
			if (position == (cellList.size() - 1)) {
				Toast.makeText(this, "Cannot Delete", Toast.LENGTH_SHORT).show();
				return true;
			}
			cellList.remove(position);
			adapter.notifyDataSetChanged();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	protected void showAddNewCellDialog() {
		AddCellDialogFragment addCellDialog = new AddCellDialogFragment();
		addCellDialog.show(fm, "addCell");
	}

	protected void showEditCellDialog(int position, String newspaper, String category) {
		if (newspaper.length() > 7 && newspaper.substring(newspaper.length() - 7).equals("_custom")) {
			newspaper = newspaper.substring(0, newspaper.length() - 7);
		}
		int newspaperId = cellList.get(position).getDefaultNewspaperId(newspaper);

		EditCellDialogFragment editCellDialog = EditCellDialogFragment.newInstance(position, newspaperId, category);
		editCellDialog.show(fm, "editCell");
	}

	@Override
	public void onFinishEditingListener(int editPosition, String npName, String cat, boolean edited) {

		if (edited) {

			ReadCSV readCsv = new ReadCSV(this);
			CSVObject csvObject = readCsv.getObjectByNPName(npName, cat);

			GridCellModel newCell = new GridCellModel(csvObject.getNpImage(), csvObject.getCatName());
			cellList.set(editPosition, newCell);
			adapter.notifyDataSetChanged();
		}

	}

	@Override
	public void onFinishAddingListener(String npName, String cat) {

		ReadCSV readCsv = new ReadCSV(this);
		CSVObject csvObject = readCsv.getObjectByNPName(npName, cat);

		GridCellModel newCell = new GridCellModel(csvObject.getNpImage(), csvObject.getCatName());
		cellList.add(cellList.size() - 1, newCell);
		adapter.notifyDataSetChanged();

		readCsv.close();

	}

	/***
	 * Checks that application runs first time and write flag at
	 * SharedPreferences
	 * 
	 * @return true if 1st time
	 */
	private boolean isFirstTime() {
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		boolean ranBefore = preferences.getBoolean("RanBefore", false);
		if (!ranBefore) {
			// first time
			SharedPreferences.Editor editor = preferences.edit();
			editor.putBoolean("RanBefore", true);
			editor.commit();
		}
		return !ranBefore;
	}

}
