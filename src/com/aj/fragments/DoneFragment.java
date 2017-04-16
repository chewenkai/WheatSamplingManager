package com.aj.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.aj.Constant;
import com.aj.activity.MainActivity;
import com.aj.activity.WatchActivity;
import com.aj.collection.R;
import com.aj.tools.FileStream;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;

/**
 * Created by Gordon Wong on 7/17/2015.
 *
 * Shared items fragment.
 */
public class DoneFragment extends Fragment {


//	@Override
//	protected int getLayoutResId() {
//		return R.layout.see_content;
//	}
//
//	@Override
//	protected int getNumColumns() {
//		return 1;
//	}
//
//	@Override
//	protected int getNumItems() {
//		return count;
//	}

	private FileStream mFileStream;
	private File taskFile;
	private ListView listView;
	private AlertDialog alertDialog;
	private AlertDialog.Builder builder;
	private int count=0;
//	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.see_content,container,false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onResume() {
		super.onResume();
		MainActivity.currFragTag= Constant.FRAGMENT_FLAG_SEE;
		mFileStream = new FileStream(getActivity().getApplicationContext());
		taskFile = mFileStream.getTaskFile();

		count=taskFile.listFiles().length;
//		getNumItems();

		SimpleAdapter adapter = new SimpleAdapter(this.getActivity(),mFileStream.getFileSet(taskFile,com.aj.collection.R.drawable.seefile_img)
				,R.layout.listview_adapter,
				new String[]{"title_img","title","img"},new int[]{R.id.title_img,R.id.title,R.id.img});
		listView = (ListView) this.getActivity().findViewById(R.id.lv_see_content);
		listView.setAdapter(adapter);
		listView.setDivider(getResources().getDrawable(R.drawable.transfer));
		listView.setDividerHeight(10);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id)
			{
				// TODO Auto-generated method stub
				HashMap item = (HashMap)parent.getItemAtPosition(position);
				final String taskName = (String) item.get("title");
//				T.showShort(getActivity(), templetName);
				File taskItem = new File(taskFile.getPath(),taskName);	//TaskItem, one task file name
				final String[] fileList = taskItem.list(new MyFilenameFilter());
				builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(taskName);
				SimpleAdapter sa = new SimpleAdapter(getActivity(),
						mFileStream.getSeeTask(taskItem,getActivity().getApplicationContext()),
						R.layout.listview_adapter,
						new String[]{"title_img","title","img","describe"},
						new int[]{R.id.title_img,R.id.title,R.id.img,R.id.describe});
				builder.setAdapter(sa, new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						// TODO Auto-generated method stub
						String name = taskFile.getPath()+File.separator+taskName+File.separator+fileList[which];
						String res=mFileStream.readFileSdcard(name);
						Intent startForm = new Intent(DoneFragment.this.getActivity(), WatchActivity.class);
						startForm.putExtra("res", res);	//字符串
						startForm.putExtra("label", taskName); //文件名不包含后缀
						startForm.putExtra("file_name", fileList[which]); //文件路径
						startActivity(startForm);
					}
				});
//				builder.setItems(fileList, new OnClickListener()
//				{
//
//					@Override
//					public void onClick(DialogInterface dialog, int which)
//					{
//						// TODO Auto-generated method stub
////						T.showShort(getActivity(), fileList[which]+"is on click");
//						String name = taskFile.getPath()+File.separator+taskName+File.separator+fileList[which];
//						String res=mFileStream.readFileSdcard(name);
//						Intent startForm = new Intent(SeeFragment.this.getActivity(), WatchActivity.class);
//						startForm.putExtra("res", res);	//字符串
//						startForm.putExtra("label", taskName); //文件名不包含后缀
//						startForm.putExtra("file_name", fileList[which]); //文件路径
//						startActivity(startForm);
//					}
//				});
				alertDialog = builder.create();
				alertDialog.show();
			}
		});
	}
	class MyFilenameFilter implements FilenameFilter
	{

		@Override
		public boolean accept(File dir, String filename)
		{
			return filename.endsWith(".spms");
		}
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		//RefWatcher refWatcher = ExampleApplication.getRefWatcher(getActivity());
		//refWatcher.watch(this);
	}

	@Override
	public void onDetach() {

		super.onDetach();
	}
}
