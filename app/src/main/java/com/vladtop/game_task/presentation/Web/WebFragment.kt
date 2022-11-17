package com.vladtop.game_task.presentation.Web

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.vladtop.game_task.R
import com.vladtop.game_task.data.WEB_LINK_KEY
import java.io.File
import java.util.*


class WebFragment : Fragment(R.layout.fragment_web) {

    private lateinit var webView: WebView
    private lateinit var webLink: String

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

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            Toast.makeText(requireContext(), "onShowFileChooser", Toast.LENGTH_SHORT).show()
            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
        }
    }

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