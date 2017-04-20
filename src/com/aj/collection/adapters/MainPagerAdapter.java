package com.aj.collection.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.aj.collection.R;
import com.aj.collection.activity.fragments.DoingFragment;
import com.aj.collection.activity.fragments.DoneFragment;

/**
 * Created by Gordon Wong on 7/17/2015.
 *
 * Pager adapter for main activity.
 */
public class MainPagerAdapter extends FragmentPagerAdapter {

	public static final int NUM_ITEMS = 2;
	public static final int ALL_POS = 0;
	public static final int SHARED_POS = 1;
//	public static final int FAVORITES_POS = 2;

	private Context context;

	public MainPagerAdapter(Context context, FragmentManager fm) {
		super(fm);
		this.context = context;
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
		case ALL_POS:
			return new DoingFragment();
		case SHARED_POS:
			return new DoneFragment();
//		case FAVORITES_POS:
//			return FavoritesFragment.newInstance();
		default:
			return null;
		}
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case ALL_POS:
			return context.getString(R.string.template);
		case SHARED_POS:
			return context.getString(R.string.task);
//		case FAVORITES_POS:
//			return context.getString(R.string.favorites);
		default:
			return "";
		}
	}

	@Override
	public int getCount() {
		return NUM_ITEMS;
	}
}
