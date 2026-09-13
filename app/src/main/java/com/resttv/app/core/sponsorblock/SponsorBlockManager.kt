package com.resttv.app.core.sponsorblock

import android.content.Context
import android.webkit.WebView

class SponsorBlockManager(private val context: Context) {

    private val sponsorBlockScript = """
        (function() {
            const categories = ['sponsor', 'intro', 'outro', 'interaction', 'selfpromo', 'music_offtopic'];
            
            let currentVideoId = null;
            let segments = [];
            
            function getCurrentVideoId() {
                const urlParams = new URLSearchParams(window.location.search);
                return urlParams.get('v') || null;
            }
            
            async function fetchSegments(videoId) {
                try {
                    const response = await fetch(`https://sponsor.ajay.app/api/skipSegments?videoID=${'$'}{videoId}&categories=${'$'}{categories.join(',')}`);
                    if (response.ok) {
                        segments = await response.json();
                    }
                } catch (e) {
                    console.log('SponsorBlock fetch error:', e);
                }
            }
            
            function setupSkipping() {
                const video = document.querySelector('video');
                if (!video) return;
                
                video.addEventListener('timeupdate', () => {
                    const time = video.currentTime;
                    segments.forEach(segment => {
                        if (time >= segment.segment[0] && time < segment.segment[1]) {
                            video.currentTime = segment.segment[1];
                        }
                    });
                });
            }
            
            // Monitor video changes
            setInterval(() => {
                const videoId = getCurrentVideoId();
                if (videoId && videoId !== currentVideoId) {
                    currentVideoId = videoId;
                    fetchSegments(videoId).then(() => setupSkipping());
                }
            }, 500);
            
            // Initial setup
            const videoId = getCurrentVideoId();
            if (videoId) {
                currentVideoId = videoId;
                fetchSegments(videoId).then(() => setupSkipping());
            }
        })();
    """.trimIndent()

    fun injectSponsorBlockScript(webView: WebView) {
        val prefs = context.getSharedPreferences("rest_tv_settings", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("sponsorblock_enabled", true)

        if (!isEnabled) return

        webView.evaluateJavascript("javascript:$sponsorBlockScript") { }
    }

    fun isEnabled(): Boolean {
        val prefs = context.getSharedPreferences("rest_tv_settings", Context.MODE_PRIVATE)
        return prefs.getBoolean("sponsorblock_enabled", true)
    }
}
