package com.aj.collection.activity.fragment;


import android.app.Fragment;
import android.text.TextUtils;

import com.aj.Constant;

public class BaseFragment extends Fragment
{
//原来界面时的代码
//	public static BaseFragment newInstance(String tag) {
//		BaseFragment fragment=null;
//		if (TextUtils.equals(tag,Constant.FRAGMENT_FLAG_DO)) {
//			fragment=new DoFragment();
//		}else if (TextUtils.equals(tag,Constant.FRAGMENT_FLAG_SEE)) {
//			fragment=new SeeFragment();
//		}else if (TextUtils.equals(tag,Constant.FRAGMENT_FLAG_SETTING)) {
//			fragment=new SettingFragment();
//		}
//		return fragment;
//	}

	public static BaseFragment newInstance(String tag) {
		BaseFragment fragment=null;
		if (TextUtils.equals(tag,Constant.FRAGMENT_FLAG_DO)) {
			fragment=new DoFragment();
		}else if (TextUtils.equals(tag,Constant.FRAGMENT_FLAG_SEE)) {
			fragment=new SeeFragment();
		}else if (TextUtils.equals(tag,Constant.FRAGMENT_FLAG_SETTING)) {
			fragment=new SettingFragment();
		}
		return fragment;
	}

//	@Override
//	public void onAttach(Activity activity) {
//		super.onAttach(activity);
//		Log.v(TAG, ">>onAttach");
//	}
//	
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//		Log.v(TAG, ">>onCreateView");
//		return super.onCreateView(inflater, container, savedInstanceState);
//	}
//	
//	@Override
//	public void onActivityCreated(Bundle savedInstanceState) {
//		super.onActivityCreated(savedInstanceState);
//		Log.v(TAG, ">>onActivityCreated");
//	}
//	
//	@Override
//	public void onStart() {
//		super.onStart();
//		Log.v(TAG, ">>onStart");
//	}
//	
//	@Override
//	public void onResume() {
//		super.onResume();
//		Log.v(TAG, ">>onResume");
//	}
//	
//	@Override
//	public void onPause() {
//		super.onPause();
//		Log.v(TAG, ">>onPause");
//	}
//	
//	@Override
//	public void onStop() {
//		super.onStop();
//		Log.v(TAG, ">>onStop");
//	}
//	
//	@Override
//	public void onDestroyView() {
//		super.onDestroyView();
//		Log.v(TAG, ">>onDestroyView");
//	}
//	
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		Log.v(TAG, ">>onDestroy");
//	}
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		Log.v(TAG, ">>onCreate");
//	}
//
//	@Override
//	public void onDetach() {
//		super.onDetach();
//		Log.v(TAG, ">>onDetach");
//	}
}
