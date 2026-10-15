package com.floatube.app

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.annotation.TargetApi

class MainActivity : Activity() {
    private lateinit var root: LinearLayout
    private lateinit var urlInput: EditText
    private lateinit var playButton: Button
    private lateinit var pipButton: Button
    private lateinit var webView: WebView
    private lateinit var emptyHint: TextView
    private lateinit var playerContainer: FrameLayout
    private var hasLoadedVideo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(16, 17, 22)
        window.navigationBarColor = Color.rgb(16, 17, 22)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setContentView(R.layout.activity_main)

        root = findViewById(R.id.rootContainer)
        urlInput = findViewById(R.id.videoUrlInput)
        playButton = findViewById(R.id.playButton)
        pipButton = findViewById(R.id.pipButton)
        webView = findViewById(R.id.playerWebView)
        emptyHint = findViewById(R.id.emptyHint)
        playerContainer = findViewById(R.id.playerContainer)

        configureWebView()
        playButton.setOnClickListener { loadVideoFromInput() }
        pipButton.setOnClickListener { enterFloatingMode() }
    }

    private fun configureWebView() {
        webView.setBackgroundColor(Color.BLACK)
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return url?.startsWith("https://www.youtube.com/embed/") != true &&
                    url?.startsWith("https://www.youtube.com/iframe_api") != true &&
                    url?.startsWith("https://www.youtube.com/s/player/") != true &&
                    url?.startsWith("https://www.youtube-nocookie.com/") != true
            }
        }
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = true
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
        }
    }

    private fun loadVideoFromInput() {
        val videoId = extractYouTubeId(urlInput.text.toString().trim())
        if (videoId == null) {
            Toast.makeText(this, "Enter a valid YouTube video URL.", Toast.LENGTH_LONG).show()
            return
        }
        hasLoadedVideo = true
        emptyHint.visibility = View.GONE
        val html = """
            <!doctype html><html><head><meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
            <style>html,body{margin:0;padding:0;width:100%;height:100%;background:#000;overflow:hidden}#player{width:100%;height:100%}</style>
            </head><body><div id="player"></div>
            <script>
              var tag=document.createElement('script'); tag.src='https://www.youtube.com/iframe_api';
              document.head.appendChild(tag);
              var player;
              function onYouTubeIframeAPIReady(){
                player=new YT.Player('player',{
                  width:'100%',height:'100%',videoId:'$videoId',
                  playerVars:{playsinline:1,controls:1,rel:0,fs:1,enablejsapi:1},
                  events:{onReady:function(e){},onError:function(e){}}
                });
              }
            </script></body></html>
        """.trimIndent()
        webView.loadDataWithBaseURL("https://www.youtube.com/", html, "text/html", "UTF-8", null)
    }

    private fun extractYouTubeId(input: String): String? {
        if (input.isBlank()) return null
        val patterns = listOf(
            Regex("(?:https?://)?(?:www\\.)?youtube\\.com/watch\\?(?:[^#]*&)?v=([A-Za-z0-9_-]{11})"),
            Regex("(?:https?://)?youtu\\.be/([A-Za-z0-9_-]{11})"),
            Regex("(?:https?://)?(?:www\\.)?youtube\\.com/(?:embed|shorts|live)/([A-Za-z0-9_-]{11})")
        )
        return patterns.firstNotNullOfOrNull { it.find(input)?.groupValues?.getOrNull(1) }
    }

    private fun enterFloatingMode() {
        if (!hasLoadedVideo) {
            Toast.makeText(this, "Load a video first.", Toast.LENGTH_SHORT).show()
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            !packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            Toast.makeText(this, "Picture-in-Picture is not supported on this device.", Toast.LENGTH_LONG).show()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        val topViews = listOf<View>(
            findViewById(R.id.brandHeader), findViewById(R.id.linkLabel), urlInput,
            playButton, pipButton, findViewById(R.id.policyNote)
        )
        topViews.forEach { it.visibility = if (isInPictureInPictureMode) View.GONE else View.VISIBLE }
        root.setPadding(if (isInPictureInPictureMode) 0 else dp(18), if (isInPictureInPictureMode) 0 else dp(18), if (isInPictureInPictureMode) 0 else dp(18), if (isInPictureInPictureMode) 0 else dp(18))
        val params = playerContainer.layoutParams
        params.height = if (isInPictureInPictureMode) ViewGroup.LayoutParams.MATCH_PARENT else 0
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        playerContainer.layoutParams = params
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        webView.stopLoading()
        webView.loadUrl("about:blank")
        webView.destroy()
        super.onDestroy()
    }
}
