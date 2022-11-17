package com.vladtop.game_task.presentation.Web

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.vladtop.game_task.R
import com.vladtop.game_task.data.WEB_LINK_KEY
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val PICK_IMAGE = 1

class WebFragment : Fragment(R.layout.fragment_web) {

    private lateinit var webView: WebView
    private lateinit var webLink: String
    private val TAG: String = "WebFragment"
    private var mCM: String? = null
    private var mUM: ValueCallback<Uri>? = null
    private var mUMA: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            webLink = it.getString(WEB_LINK_KEY, "")
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById(R.id.web_view)
        webView = configureWebView()
        onBackPressed()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun configureWebView(): WebView = webView.apply {
        setWebSettings()
        disableHorizontalScroll()
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webViewClient = WebViewClient()
        webChromeClient = getMyWebChromeClient()
        loadUrl(webLink)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun disableHorizontalScroll(): Unit = webView.run {
        isHorizontalScrollBarEnabled = false
        setOnTouchListener(WebViewTouchListener())
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebSettings() {
        webView.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
        }
    }

    private fun getMyWebChromeClient(): WebChromeClient = object : WebChromeClient() {

        @SuppressLint("QueryPermissionsNeeded")
        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {

            if (hasNoPermissions()) {
                requestPermissions()
            }
            if (mUMA != null) {
                mUMA!!.onReceiveValue(null)
            }
            mUMA = filePathCallback
            var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent!!.resolveActivity(requireActivity().packageManager) != null) {
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                    takePictureIntent.putExtra("PhotoPath", mCM)
                } catch (ex: IOException) {
                    Log.e(TAG, "Image file creation failed", ex)
                }
                if (photoFile != null) {
                    mCM = "file:" + photoFile.absolutePath
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                } else {
                    takePictureIntent = null
                }
            }
            val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
            contentSelectionIntent.type = "*/*"
            val intentArray: Array<Intent?> =
                takePictureIntent?.let { arrayOf(it) } ?: arrayOfNulls(0)

            val chooserIntent = Intent(Intent.ACTION_CHOOSER)
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
            startActivityForResult(chooserIntent, PICK_IMAGE)

            return true
        }
    }

    private fun createImageFile(): File? {
        @SuppressLint("SimpleDateFormat") val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(
                Date()
            )
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
            PICK_IMAGE
        )
    }

    private fun hasNoPermissions(): Boolean =
        !(hasReadExternalPermission() && hasCameraPermission())

    private fun hasReadExternalPermission(): Boolean = ActivityCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) != PackageManager.PERMISSION_GRANTED

    private fun hasCameraPermission(): Boolean = ActivityCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.CAMERA
    ) != PackageManager.PERMISSION_GRANTED

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    try {
                        if (webView.canGoBack()) webView.goBack()
                        else showBackPressedMessage()
                    } catch (e: UninitializedPropertyAccessException) {
                        showBackPressedMessage()
                    }
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Build.VERSION.SDK_INT >= 21) {
            var results: Array<Uri>? = null
            //Check if response is positive
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == PICK_IMAGE) {
                    if (null == mUMA) {
                        return
                    }
                    if (intent == null) {
                        //Capture Photo if no image available
                        if (mCM != null) {
                            results = arrayOf(Uri.parse(mCM))
                        }
                    } else {
                        val dataString = intent.dataString
                        if (dataString != null) {
                            results = arrayOf(Uri.parse(dataString))
                        }
                    }
                }
            }
            mUMA?.onReceiveValue(results)
            mUMA = null
        }
        if (requestCode == PICK_IMAGE) {
            if (mUM == null) return
            val result =
                if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
            mUM?.onReceiveValue(result)
            mUM = null

        }
    }


    private fun showBackPressedMessage() {
        Toast.makeText(requireContext(), "Back Pressed", Toast.LENGTH_SHORT).show()
    }

    private inner class WebViewTouchListener : View.OnTouchListener {
        private var downX = 0f

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            if (event.pointerCount > 1) {
                //multi touch
                return true
            }
            when (event.action) {
                MotionEvent.ACTION_DOWN -> downX = event.x
                MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> event.setLocation(
                    downX,
                    event.y
                )
            }
            return false
        }
    }
}