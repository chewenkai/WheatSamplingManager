package com.aj.collection.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.aj.Constant
import com.aj.collection.R
import com.aj.collection.tools.MediaManager
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import kotlinx.android.synthetic.main.activity_gallery.*
import java.io.File

/**
 * Created by kevin on 17-4-5.
 * Mail: chewenkaich@gmail.com
 */

class GalleryActivity : AppCompatActivity() {
    internal var default_position = 0
    private var mediaFolderString = ""
    var imagesFiles: List<File>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        default_position = intent.getIntExtra(Constant.GALLERY_CLICK_POSITION, 0)
        mediaFolderString = intent.getStringExtra(Constant.PHOTO_MEDIA_FOLDER)
        imagesFiles = MediaManager().getLatestCameraFiles(File(mediaFolderString))
        var screenSlidePagerAdapter: ScreenSlidePagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        pager.adapter = screenSlidePagerAdapter
        pager.currentItem = default_position
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            val fragment = ViewPagerFragment()
            fragment.setAsset(imagesFiles!!.get(position).path)
            return fragment
        }

        override fun getCount(): Int {
            return imagesFiles!!.size
        }

    }

    class ViewPagerFragment : Fragment() {

        private var asset: String? = null

        fun setAsset(asset: String) {
            this.asset = asset
        }

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView = inflater!!.inflate(R.layout.view_pager_page, container, false)

            if (savedInstanceState != null) {
                if (asset == null && savedInstanceState.containsKey(BUNDLE_ASSET)) {
                    asset = savedInstanceState.getString(BUNDLE_ASSET)
                }
            }
            if (asset != null) {
                val imageView = rootView.findViewById(R.id.imageView) as SubsamplingScaleImageView
                imageView.setImage(ImageSource.uri(asset!!))
            }

            return rootView
        }

        override fun onSaveInstanceState(outState: Bundle?) {
            super.onSaveInstanceState(outState)
            val rootView = view
            if (rootView != null) {
                outState!!.putString(BUNDLE_ASSET, asset)
            }
        }

        companion object {
            private val BUNDLE_ASSET = "asset"
        }

    }
}
