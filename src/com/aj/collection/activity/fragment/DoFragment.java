package com.aj.collection.activity.fragment;

import java.io.File;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.aj.Constant;
import com.aj.collection.activity.GatherActivity;
import com.aj.collection.activity.MainActivity;
import com.aj.collection.R;
import com.aj.collection.activity.tools.FileStream;

public class DoFragment extends BaseFragment
{
	private FileStream  mFileStream;
	private File taskFile, templetFile;
	private ListView listView=null;
	private AlertDialog alertDialog;
	private AlertDialog.Builder builder;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.do_content,container,false);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		MainActivity.currFragTag=Constant.FRAGMENT_FLAG_DO;
		mFileStream = new FileStream(getActivity().getApplicationContext());
		taskFile = mFileStream.getTaskFile();
		templetFile = mFileStream.getTempletFile();
		SimpleAdapter adapter = new SimpleAdapter(this.getActivity(),mFileStream.getFileSet(templetFile, com.aj.collection.R.drawable.file)
				,R.layout.listview_adapter,
				new String[]{"title_img","title","img"},new int[]{R.id.title_img,R.id.title,R.id.img});
		listView = (ListView) this.getActivity().findViewById(R.id.lv_do_content);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				// TODO Auto-generated method stub
				HashMap item = (HashMap)parent.getItemAtPosition(position);
				final String templetName = (String) item.get("title");
//				T.showShort(getMContext(), templetName);
				File templetItem = new File(templetFile.getPath(),templetName);	//TempletItem, one templet file name
				final String[] fileList = templetItem.list();
//				builder = new AlertDialog.Builder(getMContext(),R.style.AlertDialogTheme);
				builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(templetName);
				SimpleAdapter sa = new SimpleAdapter(getActivity(),
						mFileStream.getDoTask(templetItem,getActivity().getApplicationContext()),
						R.layout.listview_adapter,
						new String[]{"title_img","title","img","describe"},
					new int[]{R.id.title_img,R.id.title,R.id.img,R.id.describe});
				builder.setAdapter(sa, new OnClickListener()
				{
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						// TODO Auto-generated method stub
//						T.showShort(getMContext(), fileList[which]+"is on click");
						String name = templetFile.getPath()+File.separator+templetName+File.separator+fileList[which];
						String res=mFileStream.readFileSdcard(name);
						Intent startForm = new Intent(DoFragment.this.getActivity(), GatherActivity.class);
						startForm.putExtra("res", res);	//字符串
						startForm.putExtra("label", templetName); //文件名不包含后缀
						startForm.putExtra("whichTask", fileList[which]);	//哪个任务被点击了
						startForm.putExtra("filepath", name);	
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
////						T.showShort(getMContext(), fileList[which]+"is on click");
//						String name = templetFile.getPath()+File.separator+templetName+File.separator+fileList[which];
//						String res=mFileStream.readFileSdcard(name);
//						Intent startForm = new Intent(DoFragment.this.getMContext(), GatherActivity.class);
//						startForm.putExtra("res", res);	//字符串
//						startForm.putExtra("label", templetName); //文件名不包含后缀
//						startActivity(startForm);
//					}
//				});
				alertDialog = builder.create();
				alertDialog.show();
			}
		});
	}
	
}
