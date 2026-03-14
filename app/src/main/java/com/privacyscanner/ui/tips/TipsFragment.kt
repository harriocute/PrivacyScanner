package com.privacyscanner.ui.tips

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.privacyscanner.databinding.FragmentTipsBinding
import com.privacyscanner.ui.adapters.TipsAdapter

/**
 * Tips tab fragment — embedded in the bottom navigation.
 * Displays expandable privacy tip cards with step-by-step guidance.
 */
class TipsFragment : Fragment() {

    private var _binding: FragmentTipsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTipsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tips = PrivacyTipsActivity().let {
            // Re-use tip data from PrivacyTipsActivity
            buildTipsList()
        }

        val adapter = TipsAdapter(tips)
        binding.recyclerTips.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
            clipToPadding = false
            setPadding(0, 0, 0, resources.getDimensionPixelSize(
                com.google.android.material.R.dimen.m3_bottom_nav_min_height
            ))
        }
    }

    private fun buildTipsList(): List<PrivacyTip> = listOf(
        PrivacyTip(
            icon = "🔍",
            title = "How to Remove a Risky App",
            description = "Uninstalling suspicious apps is the most effective way to protect your privacy.",
            steps = listOf(
                "Go to Settings → Apps (or Application Manager)",
                "Find the app you want to remove",
                "Tap 'Uninstall' and confirm",
                "Alternatively, long-press the app icon and select 'Uninstall'",
                "Restart your phone after removing multiple apps"
            )
        ),
        PrivacyTip(
            icon = "🔒",
            title = "How to Revoke Dangerous Permissions",
            description = "You can remove access to sensitive features without uninstalling the app.",
            steps = listOf(
                "Open Settings → Apps",
                "Tap the app name",
                "Tap 'Permissions'",
                "Toggle off Camera, Microphone, Location, or other sensitive permissions",
                "If the app demands permissions it doesn't need, consider uninstalling it"
            )
        ),
        PrivacyTip(
            icon = "📍",
            title = "Control Location Access",
            description = "Location data is one of the most sensitive types of personal information.",
            steps = listOf(
                "Go to Settings → Location",
                "Review which apps have 'Always' location access",
                "Change most apps to 'Only while using the app'",
                "Remove location access entirely from apps that don't need it",
                "Turn off 'Improve Location Accuracy' to reduce data sharing with Google"
            )
        ),
        PrivacyTip(
            icon = "🎤",
            title = "Protect Your Microphone",
            description = "Apps with microphone access can potentially record audio without your knowledge.",
            steps = listOf(
                "Go to Settings → Privacy → Microphone",
                "Review all apps that have microphone access",
                "Remove access from apps that don't need audio (e.g., shopping apps)",
                "Check for the orange dot indicator (Android 12+) — it shows mic is active",
                "Consider physical privacy covers if you're highly concerned"
            )
        ),
        PrivacyTip(
            icon = "📱",
            title = "Enable Privacy Dashboard",
            description = "Android 12+ includes a Privacy Dashboard to see recent permission usage.",
            steps = listOf(
                "Go to Settings → Privacy → Privacy Dashboard",
                "See which apps accessed Location, Camera, and Microphone recently",
                "Tap any entry to review that app's permissions",
                "Revoke permissions for apps with unexpected access patterns",
                "Check this dashboard weekly as a privacy health habit"
            )
        ),
        PrivacyTip(
            icon = "🛡️",
            title = "Review Permissions Before Installing",
            description = "Prevention is better than cure — check permissions before you install.",
            steps = listOf(
                "On the Google Play Store, scroll to 'About this app'",
                "Tap 'App permissions' to see what the app will request",
                "Ask: Does a flashlight app need microphone access? Probably not.",
                "Check app reviews and look for privacy-related complaints",
                "Consider alternatives with fewer permission requirements"
            )
        ),
        PrivacyTip(
            icon = "🔔",
            title = "Manage Notification Access",
            description = "Apps with notification access can read content from all your notifications.",
            steps = listOf(
                "Go to Settings → Apps → Special App Access",
                "Tap 'Notification Access'",
                "Review which apps can read all your notifications",
                "Revoke access from apps that don't genuinely need it",
                "Only fitness or assistant apps typically need this"
            )
        ),
        PrivacyTip(
            icon = "♿",
            title = "Accessibility Services — A Major Risk",
            description = "Accessibility services have the highest device access. Malware often abuses them.",
            steps = listOf(
                "Go to Settings → Accessibility → Downloaded Apps",
                "Review every app listed here carefully",
                "Remove accessibility access from any app you don't recognize",
                "Only grant this to apps specifically designed as accessibility tools",
                "Banking apps should never need accessibility access"
            )
        ),
        PrivacyTip(
            icon = "🔐",
            title = "Keep Your Phone Updated",
            description = "Software updates patch security vulnerabilities that spyware exploits.",
            steps = listOf(
                "Go to Settings → System → System Update",
                "Enable automatic updates for both Android OS and apps",
                "Update apps via Google Play Store → Manage Apps & Device",
                "Check that your device still receives security patches",
                "Consider upgrading older devices that no longer receive updates"
            )
        ),
        PrivacyTip(
            icon = "⚙️",
            title = "Use a VPN for Network Privacy",
            description = "A VPN can protect your network traffic from surveillance and tracking.",
            steps = listOf(
                "Choose a reputable VPN provider with a clear no-logs policy",
                "Avoid free VPNs — they often monetize your data",
                "Enable the VPN especially on public Wi-Fi networks",
                "Check that your VPN has a kill switch to prevent data leaks",
                "Remember: VPNs protect network traffic, not device-level spyware"
            )
        )
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
